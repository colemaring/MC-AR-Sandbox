# MC-AR-Sandbox
A real-time Minecraft terrain visualizer for augmented reality sandboxes using Xbox Kinect <br>
![20240828_101312](https://github.com/user-attachments/assets/b9f4e164-4c18-49c0-8d6c-39b1a20dfc3b) <br>
video: https://github.com/user-attachments/assets/ce1f2003-3ed6-4056-a1da-17e1319ca550 <br>

# First-Time Setup
download the release <br>
download the dependencies <br>
configure settings <br><br>

Dependencies: <br>
Java 21+ <br>
PrismLauncher <br>
Kinect SDK?? <br>

# Usage
The blaze rod (gold stick) is used to manually update the terrain. This is useful if autoupdate is off and you want to update the terrain. <br>
The other blocks in the inventory represet different biomes that you can choose from. When you select a biome, all subsequent terrain updates will reflect that chosen biome. You can go back by selecting a different biome <br>
If the terrain is bugged or doesnt match the sandbox at all, left click a biome block to update the terrain back to normal. <br>

# Commands
/waterlevel <y level> - allows you to choose the water level of the world. eg. /waterlevel 10 <br>
/autoupdate <on or off> - enable or disable auto update. When this is enabled the terrain will be automatically update to match the kinect sensor data. Uses the timer variable. eg. /autoupdate on <br>
/timer <seconds> - used to define the amount of seconds in between each auto terrain update. Only used when autoupdate is set to on. <br>
/default - resets the variables to the default values <br><br>

# Notes
All settings persist across server start and stop. You can reset the setting with /default <br>
The default settings are as follows: <br>
waterlevel = 10 <br> 
autoupdate = false <br> 
timer = 1 <br> 
biome = "mountains" <br> 

# Configuration / Tuning 
In topoprojection.py, experiment with the aspect_ratio and smoothing values to match the projection to the sand <br>

# Development
This program uses the Kinect SDK to take depth data from an Xbox Kinect Sensor. That data is then parsed and scaled down to be rendered real-time in a minecraft server. <br><br>

I am using the DepthFrame class from the Kinect SDK, morso the Node version of the sdk: https://github.com/wouterverweirder/kinect2, and writing this data to a txt file many times per second. (Not the most elegant solution). This output file is then read asynchronously in the plugin java file on a seperate thread from the Bukkit thread. The Bukkit API is what I am using to place, remove, and change blocks in the minecraft world. Bukkit API operations like setting and removing blocks are very expensive, so many optimizations were made to reduce the amount of blocks changes. My solution is to keep track of values that have changed from the last DepthFrame object. I stored those changes in a ConcurrentHashMap and iterated through the columns that have changes and only touched the blocks that need to be modified.

topoprojeciton.py is very straightforward. I am reading in that same output.txt file every x ms and displaying the data using matplotlib. The value of the height will determine the color of that point. I'm using a median filter to smooth out the edges of the different topographical levels. This smoothing value can be adjusted at the top of the file. The projector will be projecting the plot displayed by this program. The amount of topographical levels, the distance between each level, and the color of that level can all be changed. <br><br>

Example with smoothing 10 and the following color space and level distance of 25mm: <br>
cmap = plt.get_cmap('gist_rainbow')  <br>
levels = np.linspace(2300, 2575, 25)  <br>
colors = plt.cm.viridis(np.linspace(0, 1, len(levels)))  <br>

<br>
![image1](https://github.com/user-attachments/assets/6df0f985-8911-4ad4-a94b-05c6f641a71e)
<br>
Example with smoothing 1 <br>
![image2](https://github.com/user-attachments/assets/d051908b-e35e-4a3a-b9ec-27b9201285ef)


