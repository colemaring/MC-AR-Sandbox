package Terrain;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;

public class TerrainGenerator implements Listener{
    public static int[][] prevDepth;
    private final static int poolSize = 2; // adjusts size of world
    private final static int kinectDistanceScalar = 4; // need to tune to find good scalars 
    private static String prevSettingsHash;
    private static String prevBiome;
    private static boolean prevWaterEnabled;
    public static volatile boolean resetCalled = false;
    // Constructor with reference to plugin instance
    public TerrainGenerator() {
        TerrainGenerator.prevSettingsHash = "";
    }
    
	public static void updateTerrain(int[][] currDepth)
	{		
		if (prevDepth == null)
			prevDepth = currDepth;
			
		//int[][] newDepth = TerrainGeneratorHelper.cropArray(currDepth, KinectSandbox.getInstance().settings.x1, KinectSandbox.getInstance().settings.x2, KinectSandbox.getInstance().settings.y1 ,KinectSandbox.getInstance().settings.y2);
		//currDepth = TerrainGeneratorHelper.modePool(currDepth, poolSize);
		currDepth = TerrainGeneratorHelper.mirrorXYAxis(currDepth);
		currDepth = TerrainGeneratorHelper.convertToCoordinates(currDepth, KinectSandbox.getInstance().settings.yCoordOffset/kinectDistanceScalar); 
		
		for (int i = 0; i < currDepth.length; i++) {
		    for (int j = 0; j < currDepth[0].length; j++) {
		        if (Math.abs(currDepth[i][j] - prevDepth[i][j]) <= 1) {
		            // undo any ±1 spike
		            currDepth[i][j] = prevDepth[i][j];
		        }
		    }
		}
		
		if (!prevSettingsHash.equals(KinectSandbox.getInstance().settings.settingsHash))
			 prevDepth = new int[currDepth.length][currDepth[0].length];
    	   
		// find the difference array
		// diffDepth[i][k][0] = y coord of top block in range to modify
		// diffDepth[i][k][1] = y coord of bottom block in range to modify
		// diffDepth[i][k][2] = 1 if set range to air, 0 otherwise
		int [][][] diffDepth = TerrainGeneratorHelper.findDifference(prevDepth, currDepth);

		// Use BukkitRunnable to safely update blocks on the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
            	
				// exists for threading issues where run statement from previous update call is still processing while next call is initiated.
            	// conditions for which to re-render all terrain
            	if (!prevSettingsHash.equals(KinectSandbox.getInstance().settings.settingsHash) || !prevBiome.equals(KinectSandbox.biome) || prevWaterEnabled != KinectSandbox.getInstance().waterEnabled)
            	{
            	    prevSettingsHash = KinectSandbox.getInstance().settings.settingsHash;
					prevBiome = KinectSandbox.biome;
            	    prevWaterEnabled = KinectSandbox.getInstance().waterEnabled;
            	    prevDepth = new int[KinectSandbox.getInstance().settings.y2 - KinectSandbox.getInstance().settings.y1 + 1][KinectSandbox.getInstance().settings.x2 - KinectSandbox.getInstance().settings.x1 + 1];
            	    TerrainGeneratorHelper.resetBlocks();
            	    // read by main thread bukkit api to check if current updateTerrain called needs to cancel current block placements
            	    resetCalled = true;
            	}
//            	if (KinectSandbox.biome.equals("nether"))
//            		Bukkit.getWorld("world").setTime(20000L);
//            	else
//            		Bukkit.getWorld("world").setTime(1000L);

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
                        	continue;
			
            			int upperRange = diffDepth[i][j][0];
            			int lowerRange = diffDepth[i][j][1];
            			int addOrRemove = diffDepth[i][j][2];
            			
            			// no changes to be made 
            			if (addOrRemove == -1)
            				continue;
            			
            			// adding blocks in range upper to lower
            			if (addOrRemove == 0)
            				for (int k = lowerRange; k < upperRange; k++)
            					TerrainGeneratorHelper.placeAsBiome(i, k, j, KinectSandbox.biome, true);
            					
            			
            			// removing blocks in range upper to lower
            			else if (addOrRemove == 1)
            				for (int k = lowerRange; k < upperRange; k++)
            					TerrainGeneratorHelper.placeAsBiome(i, k, j, KinectSandbox.biome, false);
            			
            			// add random veins, blocks, or etc to make biomes look more natural
            			TerrainGeneratorHelper.touchUpBiome(KinectSandbox.biome);
            					
            		}
            	}
            }
        }.runTask(KinectSandbox.getInstance()); // Ensures synchronous execution on the main thread

        
        prevDepth = currDepth;
	}
}

