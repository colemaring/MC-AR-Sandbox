import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.themoep.inventorygui.InventoryGui;

import org.bukkit.event.Listener;

// ctrl + ] to build

public class KinectSandbox extends JavaPlugin implements Listener {
	public int rawKinectHeight = 424;
	public int rawKinectWidth = 512;
	public int rawKinectMaxDepth = 255;
    public World world;
    public static String biome = "grass";
    public KinectSettings settings;
    private WebsocketsHandler wsHandler;
    TerrainGenerator terrainGenerator = new TerrainGenerator(this);
    private String prevSettingsHash = "";
    @Override
    public void onEnable() {
    	Bukkit.getPluginManager().registerEvents(terrainGenerator, this);
    	// Read settings_config.json for values
    	String path = "";
		try {
			path = new File("../settings_config.json").getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Create inventory and assign listeners for it
		InventoryHelper ih = new InventoryHelper(this);
		Bukkit.getPluginManager().registerEvents(ih, this);
		
		settings = new KinectSettings(new File(path));
		
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        world = Bukkit.getWorlds().get(0);
        
        // Connect to Websocker server, passing in instance of KinectSandbox (plugin)
        // wsHandler instance handles passing data to terrainGenerator instance
        wsHandler = new WebsocketsHandler(this, terrainGenerator);
        // need some kind of mechanism to handle the block placement on load and disable
		getLogger().info(prevSettingsHash + " prev");
		
		if (!prevSettingsHash.equals(settings.settingsHash))
		{	
			getLogger().info("Settings changed, resetting blocks");
			terrainGenerator.tgHelper.resetBlocks();
			prevSettingsHash = settings.settingsHash;
		}
        terrainGenerator.tgHelper.resetBlocks();
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


}
