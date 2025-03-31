import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TerrainGenerator {
    private KinectSandbox plugin;
    public TerrainGeneratorHelper tgHelper;
    private int[][] prevDepth;
    // Constructor with reference to plugin instance
    public TerrainGenerator(KinectSandbox plugin) {
        this.plugin = plugin;
        tgHelper= new TerrainGeneratorHelper(plugin);
        prevDepth = new int[plugin.rawKinectHeight][plugin.rawKinectWidth];
    }
    
	public void updateTerrain(int[][] currDepth)
	{		
		if (prevDepth == null)
			prevDepth = currDepth;
		
		int[][] newDepth = tgHelper.meanPool(currDepth, 8);
		
		// find the difference array
		// diffDepth[i][k][0] = y coord of top block in range to modify
		// diffDepth[i][k][1] = y coord of bottom block in range to modify
		// diffDepth[i][k][2] = 1 if set range to air, 0 otherwise
		int [][][] diffDepth = tgHelper.findDifference(prevDepth, newDepth);
		
		// Use BukkitRunnable to safely update blocks on the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
            	for (int i = 0; i < diffDepth.length; i++)
            	{
            		for (int j = 0; j < diffDepth[0].length; j++)
            		{
            			int upperRange = diffDepth[i][j][0];
            			int lowerRange = diffDepth[i][j][1];
            			int addOrRemove = diffDepth[i][j][2];
            			
            			// no changes to be made 
            			if (addOrRemove == -1)
            				continue;
            			
            			// adding blocks in range upper to lower
            			if (addOrRemove == 0)
            				for (int k = lowerRange; k < upperRange; k++)
            					plugin.world.getBlockAt(i, (int)Math.floor(k * 0.1), j).setType(Material.GRASS_BLOCK);
            			
            			// removing blocks in range upper to lower
            			else if (addOrRemove == 1)
            				for (int k = lowerRange; k < upperRange; k++)
            					plugin.world.getBlockAt(i, (int)Math.floor(k * 0.1), j).setType(Material.AIR);
            		}
            	}
            }
        }.runTask(plugin); // Ensures synchronous execution on the main thread
        
        prevDepth = newDepth;
	}
}

