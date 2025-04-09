package Terrain;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;

public class TerrainGenerator implements Listener{
    public TerrainGeneratorHelper tgHelper;
    public static int[][] prevDepth;
    private final int meanPoolSize = 2; // adjusts size of world
    private final int smoothingSize = 2; // adjusts smoothing
    private final int kinectDistanceScalar = 4; // need to tune to find good scalars 
    private String prevSettingsHash;
    private String prevBiome;
    private boolean prevWaterEnabled;
    public static volatile boolean resetCalled = false;
    // Constructor with reference to plugin instance
    public TerrainGenerator() {
        this.tgHelper= new TerrainGeneratorHelper();
        this.prevSettingsHash = "";
    }
    
	public void updateTerrain(int[][] currDepth)
	{		
		if (prevDepth == null)
			prevDepth = currDepth;

			
		int[][] newDepth = tgHelper.cropArray(currDepth, KinectSandbox.getInstance().settings.x1, KinectSandbox.getInstance().settings.x2, KinectSandbox.getInstance().settings.y1 ,KinectSandbox.getInstance().settings.y2);
		newDepth = tgHelper.meanPool(newDepth, meanPoolSize); 
		newDepth = tgHelper.meanFilter(newDepth, smoothingSize); 
		newDepth = tgHelper.mirrorXYAxis(newDepth);
		newDepth = tgHelper.convertToCoordinates(newDepth, KinectSandbox.getInstance().settings.elevationMultiplier/300.0, KinectSandbox.getInstance().settings.kinectDistance/kinectDistanceScalar); 
		
		if (!prevSettingsHash.equals(KinectSandbox.getInstance().settings.settingsHash))
		{
			 prevDepth = new int[newDepth.length][newDepth[0].length];
		}
    	   
	
		// find the difference array
		// diffDepth[i][k][0] = y coord of top block in range to modify
		// diffDepth[i][k][1] = y coord of bottom block in range to modify
		// diffDepth[i][k][2] = 1 if set range to air, 0 otherwise
		int [][][] diffDepth = tgHelper.findDifference(prevDepth, newDepth);

		// Use BukkitRunnable to safely update blocks on the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
            	
				// exists for threading issues where run statement from previous update call is still processing while next call is initiated.
            	// conditions for which to re-render all terrain
            	if (!prevSettingsHash.equals(KinectSandbox.getInstance().settings.settingsHash) || !prevBiome.equals(KinectSandbox.getInstance().biome) || prevWaterEnabled != KinectSandbox.getInstance().waterEnabled)
            	{
            	    prevSettingsHash = KinectSandbox.getInstance().settings.settingsHash;
            	    prevBiome = KinectSandbox.getInstance().biome;
            	    prevWaterEnabled = KinectSandbox.getInstance().waterEnabled;
            	    prevDepth = new int[KinectSandbox.getInstance().settings.y2 - KinectSandbox.getInstance().settings.y1 + 1][KinectSandbox.getInstance().settings.x2 - KinectSandbox.getInstance().settings.x1 + 1];
            	    tgHelper.resetBlocks();
            	    // read by main thread bukkit api to check if current updateTerrain called needs to cancel current block placements
            	    resetCalled = true;
            	}
            	if (KinectSandbox.biome.equals("nether"))
            		Bukkit.getWorld("world").setTime(20000L);
            	else
            		Bukkit.getWorld("world").setTime(1000L);

            	for (int i = 0; i < diffDepth.length; i++)
            	{
            		for (int j = 0; j < diffDepth[0].length; j++)
            		{
            			// Scheduled block place is canceled, caused by another call to 
            			// or terrainPaused
                        if (resetCalled) {
                        	KinectSandbox.getInstance().getLogger().info("CANCELED BLOCK PLACES");
                        	resetCalled = false;
                            return;
                        }
                        
                        // Still calculate block placements but dont do them
                        if (TerrainGeneratorHelper.terrainPaused)
                        {
//                        	prevDepth = new int[plugin.settings.y2 - plugin.settings.y1 + 1][plugin.settings.x2 - plugin.settings.x1 + 1];
                        	
                        	continue;
                        }
                        	
            			
            			int upperRange = diffDepth[i][j][0];
            			int lowerRange = diffDepth[i][j][1];
            			int addOrRemove = diffDepth[i][j][2];
            			
            			// no changes to be made 
            			if (addOrRemove == -1)
            				continue;
            			
            			// adding blocks in range upper to lower
            			if (addOrRemove == 0)
            				for (int k = lowerRange; k < upperRange; k++)
            					tgHelper.placeAsBiome(i, k, j, KinectSandbox.biome, true);
            					
            			
            			// removing blocks in range upper to lower
            			else if (addOrRemove == 1)
            				for (int k = lowerRange; k < upperRange; k++)
            					tgHelper.placeAsBiome(i, k, j, KinectSandbox.biome, false);
            			
            			// add random veins, blocks, or etc to make biomes look more natural
            			tgHelper.touchUpBiome(KinectSandbox.biome);
            					
            		}
            	}
            }
        }.runTask(KinectSandbox.getInstance()); // Ensures synchronous execution on the main thread
        
        prevDepth = newDepth;
	}
}

