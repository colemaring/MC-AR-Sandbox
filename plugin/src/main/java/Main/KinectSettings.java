package Main;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KinectSettings {
    public int x1;
    public int x2;
    public int y1;
    public int y2;
    public int kinectDistance;
    public int elevationMultiplier;
    public String settingsHash;
    private final File settingsFile;
    private final AtomicReference<KinectSettings> settingsReference;

    public KinectSettings(File file) {
        this.settingsFile = file;
        this.settingsReference = new AtomicReference<>(this);

        // Load initial settings
        loadSettings();

        // Start monitoring the file for changes
        startFileWatcher();
    }

    private void loadSettings() {
        try (FileReader reader = new FileReader(settingsFile)) {
            // Parse JSON file
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject crop = root.getAsJsonObject("kinect_view_crop");

            this.x1 = crop.get("x1").getAsInt();
            this.y1 = crop.get("y1").getAsInt();
            this.x2 = crop.get("x2").getAsInt();
            this.y2 = crop.get("y2").getAsInt();
            this.kinectDistance = root.get("kinect_surface_distance_cm").getAsInt();
            this.elevationMultiplier = root.get("minecraft_elevation").getAsInt();
            this.settingsHash = this.x1+""+this.x2+""+this.y1+""+this.y2+""+this.kinectDistance+""+elevationMultiplier;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read settings from file: " + settingsFile.getAbsolutePath());
        }
    }

    private void startFileWatcher() {
        // Run the file watcher asynchronously to avoid blocking the main thread
        new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path pathToWatch = settingsFile.getParentFile().toPath();
                pathToWatch.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    // Wait for a file modification event
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path modifiedFile = (Path) event.context();
                            if (modifiedFile.equals(settingsFile.toPath().getFileName())) {
                                // File has changed, reload settings
                                loadSettings();
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.sendMessage(ChatColor.GREEN + "Loading new changes...");
                                }

                                // Update the reference to the latest settings
                                settingsReference.set(this);
                            }
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break; // Exit if the key is no longer valid
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public KinectSettings getSettings() {
        return settingsReference.get();
    }
}
