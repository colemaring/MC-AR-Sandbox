# MC-AR-Sandbox
A real-time Minecraft terrain visualizer for augmented reality sandboxes using Xbox Kinect <br>

# Features
- Comprehensive one-click launcher that automates almost everything.
- Configurable settings to adapt to any environment.
- BuildTools integration for Mojang [compliance](https://github.com/github/dmca/blob/master/2014/2014-09-05-CraftBukkit.md).
- Plugin consists of 7 biomes and 5 unique gamemodes to choose from.

# Installation
- Download [Python 3.12 from the Microsoft Store](https://apps.microsoft.com/detail/9NCVDN91XZQP?hl=en-us&gl=US&ocid=pdpshare), [PrismLauncher](https://prismlauncher.org/download/windows/), [Kinect SDK](https://www.microsoft.com/en-us/download/details.aspx?id=44561), and [Java 21+](https://www.oracle.com/java/technologies/downloads/#java21). <br>
- Log into PrismLauncher with your Microsoft/Minecraft account. <br>
- Download the latest [MC-AR Launcher release](https://github.com/colemaring/MC-AR-Sandbox/releases). (First-Time setup takes about 5 minutes)<br>
- [Tune the Kinect settings](#tuning-the-settings). <br>
- In Minecraft, navigate to Options > Video Settings > Chunk Builder, and switch to Fully Blocking. <br>

# Hardware
- A Kinect V2 sensor / Xbox One sensor.
- A windows 10/11 PC with a Decent CPU.
- (Optional) A projector.

## [Home Page](https://github.com/user-attachments/assets/a496e483-e3c3-402b-b334-d6574a2d83e5)
- Color coded logs showing MC-AR Launcher status.
- Send commands to the running Minecraft server (eg. op <username>).
- Launch Minecreaft & Topographic Projection.

## [Settings Page](https://github.com/user-attachments/assets/80c60829-81a5-4212-820d-e3af01e85768)
### Kinect Settings:
- Crop Kinect View: Drag the crop edges to correspond with the boundaries of your sandbox.
- Capture Speed: Change the speed at which the Kinect sends updates to Minecraft.
- Kinect to Sandbox Distance: The distance from your Kinect sensor to the sand in milimeters. This value is only used by the topographic projection.

### Topographic Projection Settings:
- Open on Launch: Open the projection when the Launch button on home page is clicked.
- Show On: Choose which display device to launch the projection on.
- Smoothing: Determine how much detail you want on your topographic layer lines.
- Color: Choose which color profile to project onto the sand.
- Interpolation: Choose which type of interpolation to smooth the noise.

### Minecraft Settings:
- Open on Launch: Open Minecraft when the Launch button on home page is clicked.
- Show On: Choose which display device to launch Minecraft on.
- Elevation Multiplier: Choose how much y axis range you'd like in Minecraft.
- Y Coordinate Offset: Needs to be tuned to fit the Kinect/Sandbox environment. Needs to be re-adjusted when elevation multiplier is changed.
- PrismLauncher Filepath: Specify the filepath to the Prismlauncher exe.
  
Save button: Save your configuration to persist between launches and reflect changes in the currently running Minecraft instance.

## [Info Page](https://github.com/user-attachments/assets/ac30290d-91f7-4108-8d40-1fcfa91d41c5)
- Gamemode Information
- In-Game Commands (Outdated, changed to GUIs)

## Tuning the settings
todo

## Troubleshooting
Ensure you do not have python 2.x.x installed <br>

## Developers
```cd launcher```<br>
```npm i```<br>
```npm run dev```<br>
Note: The packaged version of this electron app will reference a different Minecraft server world directory than the dev environment.<br>
Packaged: C:\Users\<user>\AppData\Local\Programs\mc-ar-launcher\resources\world <br>
eg. Dev: C:\Users\<user>\Desktop\mcar\launcher\server\world <br>

Utilize the [plugman](https://www.spigotmc.org/resources/plugmanx.88135/) plugin, the powershell scripts, and the maven pom.xml to hot-reload the KinectSandbox plugin when developing. <br>

### V2 Development
This is the second version of the MC-AR Sandbox visualizer, where I've created a desktop application that makes the entire system a one-click launch. The desktop application handles the minecraft server, minecraft launcher, kinect sdk runtime, and topographic projection. I've programmed the MCAR launcher to automatically handle errors and first launches, where it will attempt to build the spigot server jar using the buildtools jar. Instead of writing to a text file and reading that in the plugin, I am now streaming the depth data over websockets. This drastically improved performance. I've also written a faster algorithm to only update blocks that need to be (which I am proud of). I've ran into a lot of interesting threading issues and race conditions while working on this project which is something I've never had to face before. <br>

### V1 Development
This program uses the Kinect SDK to take depth data from an Xbox Kinect Sensor. That data is then parsed and scaled down to be rendered real-time in a minecraft server. <br><br>

I am using the DepthFrame class from the Kinect SDK, morso the Node version of the sdk: https://github.com/wouterverweirder/kinect2, and writing this data to a txt file many times per second. (Not the most elegant solution). This output file is then read asynchronously in the plugin java file on a seperate thread from the Bukkit thread. The Bukkit API is what I am using to place, remove, and change blocks in the minecraft world. Bukkit API operations like setting and removing blocks are very expensive, so many optimizations were made to reduce the amount of blocks changes. My solution is to keep track of values that have changed from the last DepthFrame object. I stored those changes in a ConcurrentHashMap and iterated through the columns that have changes and only touched the blocks that need to be modified.

topoprojeciton.py is very straightforward. I am reading in that same output.txt file every x ms and displaying the data using matplotlib. The value of the height will determine the color of that point. I'm using a median filter to smooth out the edges of the different topographical levels. This smoothing value can be adjusted at the top of the file. The projector will be projecting the plot displayed by this program. The amount of topographical levels, the distance between each level, and the color of that level can all be changed. <br><br>
