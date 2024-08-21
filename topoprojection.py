import json
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors

plt.rcParams['toolbar'] = 'None' # removes default toolbar
plt.ion()

fig, ax = plt.subplots(figsize=(8, 6))
fig.canvas.manager.set_window_title("Topographic Projection")

plt.toolbar = None

# fig.canvas.manager.window.wm_overrideredirect(True) # removes windows controls
cmap = plt.get_cmap('gist_rainbow')
levels = np.linspace(2300, 2575, 25)
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

            ax.clear()
            ax.imshow(depth_array, cmap=cmap, interpolation='nearest', vmin=levels[0], vmax=levels[-1])

            contours = ax.contourf(depth_array.T, levels=levels, colors=colors)
            ax.set_xlim(18, 450)
            ax.set_ylim(200, 600)

            ax.set_xticks([])
            ax.set_yticks([])

            plt.draw()
            plt.pause(0.1)

    except json.JSONDecodeError:
        print("Error: File is not yet written or is being written.")

plt.close()
