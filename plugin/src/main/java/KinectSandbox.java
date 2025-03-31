import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

// ctrl + ] to build

public class KinectSandbox extends JavaPlugin implements Listener {
	public int rawKinectHeight = 424;
	public int rawKinectWidth = 512;
	public int rawKinectMaxDepth = 255;
    public World world;
    private WebsocketsHandler wsHandler;
    TerrainGenerator terrainGenerator = new TerrainGenerator(this);
    
    @Override
    public void onEnable() {
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        world = Bukkit.getWorlds().get(0);
        
        // Connect to Websocker server, passing in instance of KinectSandbox (plugin)
        // wsHandler instance handles passing data to terrainGenerator instance
        wsHandler = new WebsocketsHandler(this, terrainGenerator);
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
