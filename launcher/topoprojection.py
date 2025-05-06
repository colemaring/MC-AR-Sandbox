import asyncio
import threading
import websockets
import orjson as json  # Keep using orjson
import os
import time
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

import numpy as np
import matplotlib
matplotlib.use('Qt5Agg')
import matplotlib.pyplot as plt
from scipy.ndimage import median_filter
from PyQt5.QtWidgets import QApplication
from PyQt5 import QtCore

def on_key_press(event):
    if event.key == 'escape':
        plt.close('all')

# Default configuration values
config = {
    "topographic_auto_launch_projector": False,
    "topographic_display_assignment": "Display 2",
    "topographic_smoothing": 20,
    "topographic_color_mode": "Rainbow",
    "kinect_distance_mm": 2000,
    "topographic_interpolation": "None",
    "kinect_view_crop": {
        "x1": 0,
        "y1": 0,
        "x2": 512,
        "y2": 424
    }
}

latest_data = None
config_path = os.path.join(os.environ.get('APPDATA', ''), 'mc-ar-launcher', 'settings_config.json')

def load_config():
    """
    Load configuration from the config file.
    """
    global config
    try:
        if os.path.exists(config_path):
            with open(config_path, 'r') as f:
                loaded_config = json.loads(f.read())
                
                # Extract only the settings we care about
                if "topographic_auto_launch_projector" in loaded_config:
                    config["topographic_auto_launch_projector"] = loaded_config["topographic_auto_launch_projector"]
                if "topographic_display_assignment" in loaded_config:
                    config["topographic_display_assignment"] = loaded_config["topographic_display_assignment"]
                if "topographic_smoothing" in loaded_config:
                    config["topographic_smoothing"] = loaded_config["topographic_smoothing"]
                if "topographic_color_mode" in loaded_config:
                    config["topographic_color_mode"] = loaded_config["topographic_color_mode"]
                if "topographic_interpolation" in loaded_config:
                    config["topographic_interpolation"] = loaded_config["topographic_interpolation"]
                if "kinect_distance_mm" in loaded_config:
                    config["kinect_distance_mm"] = loaded_config["kinect_distance_mm"]
                if "kinect_view_crop" in loaded_config:
                    config["kinect_view_crop"] = {
                        "x1": loaded_config["kinect_view_crop"]["x1"],
                        "y1": loaded_config["kinect_view_crop"]["y1"],
                        "x2": loaded_config["kinect_view_crop"]["x2"],
                        "y2": loaded_config["kinect_view_crop"]["y2"]
                    }
                
                print("Configuration loaded:")
                print(f"  Auto Launch: {config['topographic_auto_launch_projector']}")
                print(f"  Display: {config['topographic_display_assignment']}")
                print(f"  Smoothing: {config['topographic_smoothing']}")
                print(f"  Color Mode: {config['topographic_color_mode']}")
                print(f"  Interpolation: {config['topographic_interpolation']}")
                print(f"  Kinect Distance (mm): {config['kinect_distance_mm']}")
                print(f"  Kinect View Crop: x1={config['kinect_view_crop']['x1']}, y1={config['kinect_view_crop']['y1']}, x2={config['kinect_view_crop']['x2']}, y2={config['kinect_view_crop']['y2']}")
                
    except Exception as e:
        print(f"Error loading configuration: {e}")

class ConfigFileHandler(FileSystemEventHandler):
    """
    Handler for file system events on the config file.
    """
    def on_modified(self, event):
        if event.src_path == config_path:
            print("Configuration file modified, reloading...")
            load_config()

def start_config_watcher():
    """
    Start watching the config file for changes.
    """
    observer = Observer()
    observer.schedule(ConfigFileHandler(), os.path.dirname(config_path), recursive=False)
    observer.start()
    print(f"Started watching config file: {config_path}")
    return observer

async def listen():
    """
    Connects to the websocket server and listens for depthData messages.
    Updates the global latest_data when new data arrives.
    """
    global latest_data
    url = "ws://localhost:8080"
    while True:
        try:
            async with websockets.connect(url, ping_interval=10, ping_timeout=5) as websocket:
                print("Connected to WebSocket server.")
                async for message in websocket:
                    try:
                        payload = json.loads(message)
                        if payload.get('type') == 'depthData':
                            latest_data = payload.get('data')
                    except Exception as e:
                        print(f"Error processing message: {e}")
        except (websockets.exceptions.ConnectionClosedError,
                websockets.exceptions.ConnectionClosedOK) as e:
            print(f"Connection closed: {e}. Reconnecting in 5s...")
            await asyncio.sleep(5)
        except Exception as e:
            print(f"Connection error: {e}. Reconnecting in 5s...")
            await asyncio.sleep(5)


def start_listener():
    """
    Helper to run the asyncio listener in a new thread.
    """
    asyncio.run(listen())


def main():
    # Load configuration first
    print("Loading initial configuration...")
    load_config()
    
    # Start config file watcher
    config_observer = start_config_watcher()
    
    # Start the websocket listener in a background thread
    threading.Thread(target=start_listener, daemon=True).start()

    # Qt Application & Plot setup
    app = QApplication([])
    smoothing = config["topographic_smoothing"]  # Use the loaded smoothing value

    plt.rcParams['toolbar'] = 'None'
    plt.ion()

    fig, ax = plt.subplots()
    fig.patch.set_facecolor('black')
    fig.canvas.mpl_connect('key_press_event', on_key_press)
    fig.canvas.manager.set_window_title("Topographic Projection: ESC to quit")
    qt_window = fig.canvas.manager.window

    screen = app.screens()[1] if len(app.screens()) > 1 else app.primaryScreen() # Default to second screen if available
    screen_geometry = screen.geometry()
    window_width = screen_geometry.width() // 2
    window_height = screen_geometry.height() // 2
    window_x = screen_geometry.x() + (screen_geometry.width() - window_width) // 2
    window_y = screen_geometry.y() + (screen_geometry.height() - window_height) // 2
    qt_window.setGeometry(window_x, window_y, window_width, window_height)


    # Change window flags to make it a normal, movable window
    qt_window.setWindowFlags(QtCore.Qt.Window)
    
    # Show as a normal window instead of fullscreen
    qt_window.showNormal() 
    qt_window.showFullScreen()

    # Choose colormap based on config
    if config["topographic_color_mode"] == "Rainbow":
        cmap = plt.get_cmap('gist_rainbow')
    else:  # Default
        cmap = plt.get_cmap('viridis')
        
    fig.subplots_adjust(left=0.01, right=0.99, top=0.99, bottom=0.01)

    img_artist = None
    print("Starting live plot. Press ESC in the window to quit.")

    # Store current kinect_distance_mm to detect changes
    last_kinect_distance_mm = config["kinect_distance_mm"] 
    # Define the range for color mapping around kinect_distance_mm
    color_map_range = 200 
    
    # Calculate initial levels
    current_kinect_distance = config["kinect_distance_mm"]
    levels = np.linspace(current_kinect_distance - color_map_range, current_kinect_distance + color_map_range, 100)


    try:
        while True:
            if not plt.fignum_exists(fig.number):
                break

            # --- DYNAMIC KINECT DISTANCE UPDATE ---
            current_kinect_distance = config["kinect_distance_mm"]
            if current_kinect_distance != last_kinect_distance_mm:
                print(f"Kinect distance changed from {last_kinect_distance_mm} to {current_kinect_distance}. Updating color scale.")
                levels = np.linspace(current_kinect_distance - color_map_range, current_kinect_distance + color_map_range, 100)
                if img_artist:
                    img_artist.set_clim(vmin=levels[0], vmax=levels[-1])
                last_kinect_distance_mm = current_kinect_distance
            # --- END DYNAMIC KINECT DISTANCE UPDATE ---

            if latest_data is not None:
                try:
                    # Process incoming data
                    arr = np.array(latest_data)

                    x1 = config["kinect_view_crop"]["x1"]
                    y1 = config["kinect_view_crop"]["y1"]
                    x2 = config["kinect_view_crop"]["x2"]
                    y2 = config["kinect_view_crop"]["y2"]

                    if x1 >= x2 or y1 >= y2:
                        print(f"Invalid crop coordinates: x1={x1}, y1={y1}, x2={x2}, y2={y2}. Using full image.")
                    else:
                        arr = arr[y1:y2, x1:x2]
                    
                    current_smoothing = config["topographic_smoothing"]
                    if config["topographic_interpolation"] == "Median Filter" and current_smoothing > 0:
                        arr = median_filter(arr, size=current_smoothing)
                        
                    arr = np.flipud(arr)

                    current_color_mode = config["topographic_color_mode"]
                    if current_color_mode == "Rainbow":
                        new_cmap = plt.get_cmap('gist_rainbow')
                    elif current_color_mode == "Natural":
                        new_cmap = plt.get_cmap('terrain')
                    else:
                        new_cmap = plt.get_cmap('viridis')

                    if img_artist is None:
                        img_artist = ax.imshow(
                            arr,
                            cmap=new_cmap,
                            interpolation='nearest' if config["topographic_interpolation"] == "None" else 'bilinear',
                            vmin=levels[0], # Use current levels
                            vmax=levels[-1] # Use current levels
                        )
                        ax.set_xticks([])
                        ax.set_yticks([])
                    else:
                        img_artist.set_data(arr)
                        if img_artist.get_cmap().name != new_cmap.name:
                            img_artist.set_cmap(new_cmap)
                        # Ensure clim is also set if levels changed before img_artist was created
                        # This check might be redundant if the above dynamic kinect distance update handles it
                        current_vmin, current_vmax = img_artist.get_clim()
                        if current_vmin != levels[0] or current_vmax != levels[-1]:
                            img_artist.set_clim(vmin=levels[0], vmax=levels[-1])

                    fig.canvas.draw_idle()
                    fig.canvas.flush_events()

                except Exception as e:
                    print(f"Error updating plot: {e}")

            plt.pause(0.05)
    finally:
        config_observer.stop()
        config_observer.join()
        plt.close()


if __name__ == "__main__":
    main()