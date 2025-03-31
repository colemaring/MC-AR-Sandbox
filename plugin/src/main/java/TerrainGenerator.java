import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TerrainGenerator {
    private KinectSandbox plugin;
    private TerrainGeneratorHelper tgHelper;
    private int[][] prevDepth;
    // Constructor with reference to plugin instance
    public TerrainGenerator(KinectSandbox plugin) {
        this.plugin = plugin;
        tgHelper= new TerrainGeneratorHelper(plugin);
        prevDepth = new int[plugin.rawKinectHeight][plugin.rawKinectWidth];
    }
    
	public void updateTerrain(int[][] currDepth)
	{
		Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("test9: " + currDepth[200][200]);
            }
        });
	}
}

