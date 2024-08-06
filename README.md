# MC-AR-Sandbox
A Minecraft terrain visualizer for augmented reality sandboxes using Xbox Kinect

# Usage
The blaze rod (gold stick) is used to manually update the terrain. This is useful if autoupdate is off and you want to update the terrain. <br>
The other blocks in the inventory represet different biomes that you can choose from. When you select a biome, all subsequent terrain updates will reflect that chosen biome. You can go back by selecting a differnt biome <br><br>

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
timer = 10 <br> 
biome = "mountains" <br> 
scale = 150  <br> 
fastrender = false <br> 
