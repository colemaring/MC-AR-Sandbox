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

# Default configuration values
config = {
    "topographic_auto_launch_projector": False,
    "topographic_display_assignment": "Display 2",
    "topographic_smoothing": 20,
    "topographic_color_mode": "Rainbow",
    "topographic_interpolation": "None"
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
                
                print("Configuration loaded:")
                print(f"  Auto Launch: {config['topographic_auto_launch_projector']}")
                print(f"  Display: {config['topographic_display_assignment']}")
                print(f"  Smoothing: {config['topographic_smoothing']}")
                print(f"  Color Mode: {config['topographic_color_mode']}")
                print(f"  Interpolation: {config['topographic_interpolation']}")
                
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
    fig.canvas.manager.set_window_title("Topographic Projection: q to quit")
    qt_window = fig.canvas.manager.window
    qt_window.setGeometry(0, 1080, 3840, 2160)
    qt_window.setWindowFlags(QtCore.Qt.WindowFlags(QtCore.Qt.Tool))
    qt_window.showFullScreen()

    # Choose colormap based on config
    if config["topographic_color_mode"] == "Rainbow":
        cmap = plt.get_cmap('gist_rainbow')
    elif config["topographic_color_mode"] == "Earthchromic":
        cmap = plt.get_cmap('terrain')
    else:  # Default
        cmap = plt.get_cmap('viridis')
        
    levels = np.linspace(0, 255, 25)
    fig.subplots_adjust(left=0.01, right=0.99, top=0.99, bottom=0.01)

    img_artist = None
    print("Starting live plot. Press 'q' in the window to quit.")

    try:
        while True:
            if not plt.fignum_exists(fig.number):
                break

            if latest_data is not None:
                try:
                    # Process incoming data
                    arr = np.array(latest_data)
                    
                    # Apply smoothing based on current config
                    current_smoothing = config["topographic_smoothing"]
                    if config["topographic_interpolation"] == "Median Filter" and current_smoothing > 0:
                        arr = median_filter(arr, size=current_smoothing)
                    elif config["topographic_interpolation"] == "Gaussian Blur" and current_smoothing > 0:
                        # Gaussian blur could be implemented, but for now just print it
                        print(f"Gaussian Blur would be applied with sigma={current_smoothing/10}")
                        arr = median_filter(arr, size=current_smoothing)  # Fallback to median filter
                        
                    arr = np.fliplr(arr)

                    # Initialize or update the image artist
                    if img_artist is None:
                        img_artist = ax.imshow(
                            arr,
                            cmap=cmap,
                            interpolation='nearest' if config["topographic_interpolation"] == "None" else 'bilinear',
                            vmin=levels[0],
                            vmax=levels[-1]
                        )
                        ax.set_xticks([])
                        ax.set_yticks([])
                    else:
                        img_artist.set_data(arr)

                    # Efficient redraw
                    fig.canvas.draw_idle()
                    fig.canvas.flush_events()

                except Exception as e:
                    print(f"Error updating plot: {e}")

            # Small pause for event loop
            if plt.waitforbuttonpress(0.05) == 'q':
                break
    finally:
        config_observer.stop()
        config_observer.join()
        plt.close()


if __name__ == "__main__":
    main()