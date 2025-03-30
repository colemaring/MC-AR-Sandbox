
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KinectSandbox extends JavaPlugin implements Listener {
    private World world;
    private WebSocketClient wsClient;
    private Gson gson = new Gson();
    private boolean connected = false;

    @Override
    public void onEnable() {
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);

        // Get the default world
        world = Bukkit.getWorlds().get(0);

        // Send a message to all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.GREEN + "Plugin enabled!");
        }

        // Connect to WebSocket server
        connectToWebSocket();
        
        // Schedule reconnect attempts if connection fails
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (!connected) {
                getLogger().info("Attempting to reconnect to WebSocket...");
                connectToWebSocket();
            }
        }, 100, 200); // Try every 10 seconds (200 ticks)
    }

    @Override
    public void onDisable() {
        // Close WebSocket connection
        if (wsClient != null) {
            wsClient.close();
        }
        getLogger().info("Plugin disabled.");
    }

    private void connectToWebSocket() {
        try {
            URI serverUri = new URI("ws://localhost:8080");
            wsClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    connected = true;
                    getLogger().info("Connected to WebSocket server!");
                    
                    // Send message to all players on the main thread
                    Bukkit.getScheduler().runTask(KinectSandbox.this, () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(ChatColor.GREEN + "Connected to Kinect sensor!");
                        }
                    });
                }

                @Override
                public void onMessage(String message) {
                    try {
                        // Parse the message
                        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
                        String type = jsonObject.get("type").getAsString();
                        
                        if ("depthData".equals(type)) {
                            // Send a brief status message to players (not the whole data as it would be huge)
                            Bukkit.getScheduler().runTask(KinectSandbox.this, () -> {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.sendMessage(ChatColor.AQUA + "Received Kinect depth data!");
                                }
                            });
                            
                            // Here you could process the depth data and use it in the game
                            // For example, modify blocks based on the depth data
                            // This would likely be quite complex and depend on your specific requirements
                        }
                    } catch (Exception e) {
                        getLogger().warning("Error processing WebSocket message: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    getLogger().info("WebSocket connection closed: " + reason);
                    
                    // Notify players on the main thread
                    Bukkit.getScheduler().runTask(KinectSandbox.this, () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(ChatColor.RED + "Lost connection to Kinect sensor!");
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    connected = false;
                    getLogger().warning("WebSocket error: " + ex.getMessage());
                }
            };
            
            // Connect asynchronously
            wsClient.connect();
        } catch (URISyntaxException e) {
            getLogger().severe("Invalid WebSocket URI: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World playerWorld = player.getWorld();

        // Send message to all players in the same world
        for (Player p : playerWorld.getPlayers()) {
            p.sendMessage(ChatColor.AQUA + player.getName() + " joined the game!");
        }
        
        // Inform the player about WebSocket connection status
        if (connected) {
            player.sendMessage(ChatColor.GREEN + "Connected to Kinect sensor!");
        } else {
            player.sendMessage(ChatColor.RED + "Not connected to Kinect sensor!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("test")) {
            sender.sendMessage(ChatColor.YELLOW + "Hi there!");
            return true;
        } else if (command.getName().equalsIgnoreCase("kinectstatus")) {
            sender.sendMessage(ChatColor.BLUE + "Kinect WebSocket status: " + 
                              (connected ? ChatColor.GREEN + "Connected" : ChatColor.RED + "Disconnected"));
            return true;
        }
        return false;
    }
}