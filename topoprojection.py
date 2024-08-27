import matplotlib
matplotlib.use('Qt5Agg')

import json
import numpy as np
import matplotlib.pyplot as plt
from scipy.ndimage import median_filter
from PyQt5.QtWidgets import QApplication

app = QApplication([])

aspect_ratio = 0.5  # adjust to match sandbox
smoothing = 10  # adjust to smooth noisy edges

plt.rcParams['toolbar'] = 'None'  # removes default toolbar
plt.ion()

fig, ax = plt.subplots()
fig.patch.set_facecolor('black')
fig.canvas.manager.set_window_title("Topographic Projection: q to quit")

qt_window = fig.canvas.manager.window
qt_window.setGeometry(0, 1080, 3840, 2160)  # x, y, width, height

cmap = plt.get_cmap('gist_rainbow')
levels = np.linspace(2300, 2625, 25)
colors = plt.cm.viridis(np.linspace(0, 1, len(levels)))

fig.subplots_adjust(left=0.01, right=0.99, top=0.99, bottom=0.01)

while True:
    try:
        with open(r'C:\Users\colem\OneDrive\Desktop\mcserver\output.txt', 'r') as f:
            data = json.load(f)
            depth_array = np.array(data)
            
            if not plt.fignum_exists(fig.number):
                break

            depth_array = np.rot90(depth_array, -1)
            depth_array = median_filter(depth_array, size=smoothing)
            depth_array = np.fliplr(depth_array)

            ax.clear()
            ax.imshow(depth_array, cmap=cmap, interpolation='bilinear', vmin=levels[0], vmax=levels[-1], aspect=aspect_ratio)

            contours = ax.contourf(depth_array.T, levels=levels, colors=colors)
            ax.set_xlim(16, 456)
            ax.set_ylim(74, 482)

            ax.set_xticks([])
            ax.set_yticks([])

            plt.draw()
            plt.pause(0.1)

    except json.JSONDecodeError:
        print("Error: File is not yet written or is being written.")

plt.close()
