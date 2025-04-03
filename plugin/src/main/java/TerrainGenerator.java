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
    private final int meanPoolSize = 2; // adjusts size of world
    private final int smoothingSize = 2; // adjusts smoothing
    private final double worldHeightScalar = 0.2;
    private final int kinectDistanceScalar = 4; // need to tune to find good scalars 
    private String prevSettingsHash;
    // Constructor with reference to plugin instance
    public TerrainGenerator(KinectSandbox plugin) {
        this.plugin = plugin;
        this.tgHelper= new TerrainGeneratorHelper(plugin);
        this.prevDepth = new int[plugin.rawKinectHeight][plugin.rawKinectWidth];
        this.prevSettingsHash = "";
    }
    
	public void updateTerrain(int[][] currDepth)
	{		
		if (prevDepth == null)
			prevDepth = currDepth;
			
		int[][] newDepth = tgHelper.cropArray(currDepth, plugin.settings.x1, plugin.settings.x2, plugin.settings.y1 ,plugin.settings.y2);
		
		newDepth = tgHelper.meanPool(newDepth, meanPoolSize); 
		newDepth = tgHelper.meanFilter(newDepth, smoothingSize); 
		newDepth = tgHelper.mirrorXYAxis(newDepth);
		newDepth = tgHelper.convertToCoordinates(newDepth, worldHeightScalar, plugin.settings.kinectDistance/kinectDistanceScalar); 
	
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
            					plugin.world.getBlockAt(i, k, j).setType(Material.GRASS_BLOCK);
            			
            			// removing blocks in range upper to lower
            			else if (addOrRemove == 1)
            				for (int k = lowerRange; k < upperRange; k++)
            					plugin.world.getBlockAt(i, k, j).setType(Material.AIR);
            		}
            	}
            }
        }.runTask(plugin); // Ensures synchronous execution on the main thread
        
        prevDepth = newDepth;
	}
}

