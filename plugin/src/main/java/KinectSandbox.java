import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class KinectSandbox extends JavaPlugin implements Listener {
    private World world;
    private WebsocketsHandler wsHandler;

    @Override
    public void onEnable() {
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        world = Bukkit.getWorlds().get(0);

        // Connect to Websocker server
        wsHandler = new WebsocketsHandler(this);
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
