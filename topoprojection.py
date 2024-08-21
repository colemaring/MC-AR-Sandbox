import json
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors

plt.ion()
fig, ax = plt.subplots(figsize=(8, 6))

cmap = plt.get_cmap('gist_rainbow')
levels = np.linspace(2300, 2575, 25)
colors = plt.cm.viridis(np.linspace(0, 1, len(levels)))

while True:
    try:
        with open(r'C:\Users\colem\OneDrive\Desktop\mcserver\output.txt', 'r') as f:
            data = json.load(f)
            depth_array = np.array(data)
            
            ax.clear()
            ax.imshow(depth_array, cmap=cmap, interpolation='nearest', vmin=levels[0], vmax=levels[-1])

            contours = ax.contourf(depth_array.T, levels=levels, colors=colors)
            ax.set_xlim(60, 450)
            ax.set_ylim(10, 460)

            ax.set_xticks([])
            ax.set_yticks([])

            plt.draw()
            plt.pause(0.1)

            if plt.waitforbuttonpress(timeout=0.1):
                break

    except json.JSONDecodeError:
        print("Error: File is not yet written or is being written.")

plt.close()
