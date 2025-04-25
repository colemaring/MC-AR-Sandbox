package Main;
import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import Guis.InventoryHelper;
import Misc.MiscHandlers;
import Terrain.TerrainGenerator;
import Terrain.TerrainGeneratorHelper;

// ctrl + ] to build

public class KinectSandbox extends JavaPlugin implements Listener {
	public int rawKinectHeight = 424;
	public int rawKinectWidth = 512;
	public int rawKinectMaxDepth = 255;
    public World world;
    public static boolean allowWaterFlow = false;
    public static String biome = "grass";
    public KinectSettings settings;
    private WebsocketsHandler wsHandler;
    public boolean waterEnabled = false;
    public TerrainGenerator terrainGenerator = new TerrainGenerator();
    private static KinectSandbox instance;
    
    @Override
    public void onEnable() {
    	instance = this;
    	TerrainGenerator.prevDepth = new int[KinectSandbox.getInstance().rawKinectHeight][KinectSandbox.getInstance().rawKinectWidth];
    	MiscHandlers.killEntities();
    	Bukkit.getPluginManager().registerEvents(terrainGenerator, this);
    	// Read settings_config.json for values
    	String appDataPath = System.getenv("APPDATA"); // This gives C:\Users\<user>\AppData\Roaming
    	File configFile = new File(appDataPath, "mc-ar-launcher/settings_config.json");
    	String path = "";
    	try {
    	    path = configFile.getCanonicalPath();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}

		
		// Create inventory
		InventoryHelper ih = new InventoryHelper();
		MiscHandlers mh = new MiscHandlers();
		// Center op players on join
		
		settings = new KinectSettings(new File(path));
		
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(ih, this);
        Bukkit.getPluginManager().registerEvents(mh, this);
        world = Bukkit.getWorlds().get(0);
        
        // Connect to Websocker server, passing in instance of KinectSandbox
        // wsHandler instance handles passing data to terrainGenerator instance
        wsHandler = new WebsocketsHandler();
        TerrainGeneratorHelper.resetBlocks();
        wsHandler.connectToWebSocket();
        
        
        // If launcher is accidently closed this is nice to have
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (!wsHandler.isConnected()) {
                getLogger().info("Attempting to reconnect to WebSocket...");
                wsHandler.connectToWebSocket();
            }
        }, 100, 200); // Try every 10 seconds (200 ticks)
    }

    @Override
    public void onDisable() {
        // Close WebSocket connection
        if (wsHandler != null) {
            wsHandler.closeConnection();
        }
        getLogger().info("Plugin disabled.");
    }
    
    public static KinectSandbox getInstance() {
        return instance;
    }


}
