import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;

public class WebsocketsHandler {
    private WebSocketClient wsClient;
    private Gson gson = new Gson();
    private boolean connected = false;
    private KinectSandbox plugin;
    private TerrainGenerator terrainGenerator;
    

    // Constructor with reference to plugin instance
    public WebsocketsHandler(KinectSandbox plugin, TerrainGenerator terrainGenerator) {
        this.plugin = plugin;
        this.terrainGenerator = terrainGenerator;
    }

    public void connectToWebSocket() {
        try {
            URI serverUri = new URI("ws://localhost:8080");
            wsClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    connected = true;
                    plugin.getLogger().info("Connected to WebSocket server!");
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(ChatColor.GREEN + "Connected to Kinect sensor!");
                        }
                    });
                }

                // As messages come in, convert to int[][] and send to terrainGenerator.updateTerrain(int[][])
                @Override
                public void onMessage(String message) {
                    try {
                    	 // Parse the message
                        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
                        JsonArray depthArray = jsonObject.getAsJsonArray("data");

                        // Convert JsonArray into int[][]
                        int rows = depthArray.size();  // Number of rows
                        int cols = depthArray.get(0).getAsJsonArray().size();  // Number of columns (assuming all rows have the same number of elements)

                        int[][] depthData = new int[rows][cols];

                        // Populate the int[][] array with the values from the JsonArray
                        for (int i = 0; i < rows; i++) {
                            JsonArray row = depthArray.get(i).getAsJsonArray();
                            for (int j = 0; j < cols; j++)
                                depthData[i][j] = row.get(j).getAsInt();                            
                        }

                        terrainGenerator.updateTerrain(depthData);
                        
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error processing WebSocket message: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    plugin.getLogger().info("WebSocket connection closed: " + reason);

                    // Notify players on the main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(ChatColor.RED + "Lost connection to Kinect sensor!");
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    connected = false;
                    plugin.getLogger().warning("WebSocket error: " + ex.getMessage());
                }
            };

            // Connect asynchronously
            wsClient.connect();
        } catch (URISyntaxException e) {
            plugin.getLogger().severe("Invalid WebSocket URI: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void closeConnection() {
        if (wsClient != null) {
            wsClient.close();
        }
    }
}
