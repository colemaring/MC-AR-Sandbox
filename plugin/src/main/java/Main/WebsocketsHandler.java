package Main;
import java.net.URI;
import java.net.URISyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Terrain.TerrainGenerator;
import Terrain.TerrainGeneratorHelper;

public class WebsocketsHandler {
    private WebSocketClient wsClient;
    private boolean connected = false;
    private int messageCounter;

    public void connectToWebSocket() {
        try {
            URI serverUri = new URI("ws://localhost:8080");
            wsClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    connected = true;
                    KinectSandbox.getInstance().getLogger().info("Connected to WebSocket server!");
                    
                    Bukkit.getScheduler().runTask(KinectSandbox.getInstance(), () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(ChatColor.GREEN + "Connected to Kinect sensor!");
                        }
                    });
                }

                // As messages come in, convert to int[][] and send to terrainGenerator.updateTerrain(int[][])
                @Override
                public void onMessage(String message) {
                    try {
                        messageCounter++;
                        
                        // Parse the message
                        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
                        JsonArray depthArray = jsonObject.getAsJsonArray("data");

                        // Convert JsonArray into int[][]
                        int rows = depthArray.size();  // Number of rows
                        int cols = depthArray.get(0).getAsJsonArray().size();  // Number of columns (assuming all rows have the same number of elements)

                        int[][] depthData = new int[rows][cols];

                        // Populate the int[][] array with the values from the JsonArray
                        // read in raw initial
                        for (int i = 0; i < rows; i++) {
                            JsonArray row = depthArray.get(i).getAsJsonArray();
                            for (int j = 0; j < cols; j++)
                                depthData[i][j] = row.get(j).getAsInt();       
                        }
                        
                        
                        int[][] newDepth = TerrainGeneratorHelper.cropArray(depthData, KinectSandbox.getInstance().settings.x1, KinectSandbox.getInstance().settings.x2, KinectSandbox.getInstance().settings.y1 ,KinectSandbox.getInstance().settings.y2);
                        //newDepth = TerrainGeneratorHelper.movingMode(newDepth);
                		newDepth = TerrainGeneratorHelper.modePool(newDepth, 2);
                        // after performing mode or whatever on data, then we can linear scale down
                        for (int i = 0; i < newDepth.length; i++)
                            for (int j = 0; j < newDepth[0].length; j++)
                            	newDepth[i][j] = (int) Math.round(newDepth[i][j] / (KinectSettings.elevationMultiplier * 1.0));


                        // Serves as a throttling mechanism
                        if (messageCounter % KinectSandbox.getInstance().settings.captureSpeed != 0)
                            return;
                        
                        TerrainGenerator.updateTerrain(newDepth);
                        
                    } catch (Exception e) {
                    	KinectSandbox.getInstance().getLogger().warning(e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    KinectSandbox.getInstance().getLogger().info("WebSocket connection closed: " + reason);

                    // Notify players on the main thread
                    Bukkit.getScheduler().runTask(KinectSandbox.getInstance(), () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(ChatColor.RED + "Lost connection to Kinect sensor, is the launcher closed?");
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    connected = false;
                    KinectSandbox.getInstance().getLogger().warning("WebSocket error: " + ex.getMessage());
                }
            };

            // Connect asynchronously
            wsClient.connect();
        } catch (URISyntaxException e) {
        	KinectSandbox.getInstance().getLogger().severe("Invalid WebSocket URI: " + e.getMessage());
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
