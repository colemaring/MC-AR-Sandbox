# MC-AR-Sandbox
A real-time Minecraft terrain visualizer for augmented reality sandboxes using Xbox Kinect <br>

# First-Time Setup
download the release <br>
download the dependencies <br>
configure settings <br><br>

Required: <br>
[Java 21+](https://www.oracle.com/java/technologies/downloads/#java21) <br>
[PrismLauncher](https://prismlauncher.org/download/windows/)
 - Log into Microsoft account and add a 1.21.5 instance<br>
[Kinect SDK](https://www.microsoft.com/en-us/download/details.aspx?id=44561) <br>

# V2 Development
This is the second version of the MC-AR Sandbox visualizer, where I've created a desktop application that makes the entire system a one-click launch. The desktop application handles the minecraft server, minecraft launcher, kinect sdk runtime, and topographic projection. I've programmed the MCAR launcher to automatically handle errors and first launches, where it will attempt to build the spigot server jar using the buildtools jar. Instead of writing to a text file and reading that in the plugin, I am now streaming the depth data over websockets. This drastically improved performance. I've also written a faster algorithm to only update blocks that need to be (which I am proud of). I've ran into a lot of interesting threading issues and race conditions while working on this project which is something I've never had to face before. <br>

# V1 Development
This program uses the Kinect SDK to take depth data from an Xbox Kinect Sensor. That data is then parsed and scaled down to be rendered real-time in a minecraft server. <br><br>

I am using the DepthFrame class from the Kinect SDK, morso the Node version of the sdk: https://github.com/wouterverweirder/kinect2, and writing this data to a txt file many times per second. (Not the most elegant solution). This output file is then read asynchronously in the plugin java file on a seperate thread from the Bukkit thread. The Bukkit API is what I am using to place, remove, and change blocks in the minecraft world. Bukkit API operations like setting and removing blocks are very expensive, so many optimizations were made to reduce the amount of blocks changes. My solution is to keep track of values that have changed from the last DepthFrame object. I stored those changes in a ConcurrentHashMap and iterated through the columns that have changes and only touched the blocks that need to be modified.

topoprojeciton.py is very straightforward. I am reading in that same output.txt file every x ms and displaying the data using matplotlib. The value of the height will determine the color of that point. I'm using a median filter to smooth out the edges of the different topographical levels. This smoothing value can be adjusted at the top of the file. The projector will be projecting the plot displayed by this program. The amount of topographical levels, the distance between each level, and the color of that level can all be changed. <br><br>
