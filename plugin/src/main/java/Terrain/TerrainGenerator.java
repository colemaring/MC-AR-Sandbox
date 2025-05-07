package Terrain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;
import net.md_5.bungee.api.ChatColor;

public class TerrainGenerator implements Listener{
    public static int[][] prevDepth;
    private final static int kinectDistanceScalar = 4; 
    private static String prevSettingsHash;
    private static String prevBiome = "grass";
    private static boolean prevWaterEnabled;
    public static int yCoordThreshold = -1; // anything over this height should not be placed
    public static int initialized = 0;
    public static int findOffset = -1;
    public static int diff = -1;
    public static volatile boolean resetCalled = false;
    public static boolean waterUpdated = true;

    public TerrainGenerator() {
        TerrainGenerator.prevSettingsHash = "";
    }
    
	public static void updateTerrain(int[][] currDepth)
	{		
		if (prevDepth == null)
			prevDepth = currDepth;
		
		

		currDepth = TerrainGeneratorHelper.mirrorXYAxis(currDepth);
		currDepth = TerrainGeneratorHelper.convertToCoordinates(currDepth, KinectSandbox.getInstance().settings.yCoordOffset/kinectDistanceScalar); 
		
//		if (findOffset == -1)
//			currDepth = TerrainGeneratorHelper.convertToCoordinates(currDepth, 0); 
//		else
//			currDepth = TerrainGeneratorHelper.convertToCoordinates(currDepth, diff); 
		
		if (!prevSettingsHash.equals(KinectSandbox.getInstance().settings.settingsHash))
			 prevDepth = new int[currDepth.length][currDepth[0].length];
		
		// ignore +-1 jitter
		for (int i = 0; i < currDepth.length; i++)
		    for (int j = 0; j < currDepth[0].length; j++)
		        if (Math.abs(currDepth[i][j] - prevDepth[i][j]) <= 1)
		            currDepth[i][j] = prevDepth[i][j];
		
		currDepth = TerrainGeneratorHelper.gaussianFilter(currDepth, 7);
    	   
		// find the difference array
		// diffDepth[i][k][0] = y coord of top block in range to modify
		// diffDepth[i][k][1] = y coord of bottom block in range to modify
		// diffDepth[i][k][2] = 1 if set range to air, 0 otherwise
		int [][][] diffDepth = TerrainGeneratorHelper.findDifference(prevDepth, currDepth);
		
		// This works, but I think manually finding the offset once is better than automating it.
//		if (findOffset == -1)
//		{
//			int min = Integer.MAX_VALUE;
//			for (int i = 0; i < currDepth.length; i++)
//			{
//				for (int j = 0; j < currDepth[0].length; j++)
//				{
//					min = Math.min(min,  currDepth[i][j]);
//				}
//			}
//			 
//			// the current minimum depth will be yCoord 6.
//			// this should give enough threshold to dig down.
//			int setLowestYCoord = 6;
//			diff = setLowestYCoord - min;
//			findOffset = 0;
//		}


		// Use BukkitRunnable to safely update blocks on the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
            	
        	    // On second update, find the highest block
            	// Will be used to find where to cut off blocks above that y level
        	    if (initialized == 1)
        	    {
        	    	TerrainGeneratorHelper.setBottom();
        	    	// add more to threshold as it is possible that the sandbox did not have a high enough peak when initialized
        	    	int cutoffThreshold = 15;
        	    	yCoordThreshold = TerrainGeneratorHelper.findHighestBlock() + cutoffThreshold;
        	    }
        	    
        	    if (!prevSettingsHash.equals(KinectSandbox.getInstance().settings.settingsHash))
        	    {
        	    	 initialized = 0;
        	    	 Bukkit.broadcastMessage(ChatColor.GREEN + "Changes Applied");
        	    }

        	    if (!prevBiome.equals(KinectSandbox.biome)) {
        	        prevBiome = KinectSandbox.biome;
        	        // when finished return a callback where you then update terrainPaused to false
        	        waterUpdated = false;
        	        TerrainGeneratorHelper.updateBiome(KinectSandbox.biome, 12, 12, () -> {
        	        	if (KinectSandbox.getInstance().waterEnabled)
        	        	{
        	        		TerrainGeneratorHelper.addWater(
        	                        20,
        	                        20,
        	                        KinectSandbox.biome,
        	                        () -> {
        	                        	waterUpdated = true;
        	                        }
        	                    );
        	        	}
        	        });
        	    }
        	    
        	    if(prevWaterEnabled != KinectSandbox.getInstance().waterEnabled)
        	    {
        	    	prevWaterEnabled = KinectSandbox.getInstance().waterEnabled;
        	    	if (KinectSandbox.getInstance().waterEnabled)
        	    	{
        	    		waterUpdated = false;
    	        		TerrainGeneratorHelper.addWater(
    	                        20,
    	                        20,
    	                        KinectSandbox.biome,
    	                        () -> {
    	                        	waterUpdated = true;
    	                        }
    	                    );
        	    	}
        	    	else
        	    		TerrainGeneratorHelper.removeWater(20, 20);
        	    }
        	    	
				// exists for threading issues where run statement from previous update call is still processing while next call is initiated.
            	// conditions for which to re-render all terrain
            	if (!prevSettingsHash.equals(KinectSandbox.getInstance().settings.settingsHash))
            	{
            	    prevSettingsHash = KinectSandbox.getInstance().settings.settingsHash;
            	    for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(ChatColor.GREEN + "Hard resetting all terrain..");
					}
            	    new BukkitRunnable() {
				        @Override
				        public void run() {
				        	TerrainGenerator.prevDepth = new int[KinectSandbox.getInstance().settings.y2 - KinectSandbox.getInstance().settings.y1 + 1][KinectSandbox.getInstance().settings.x2 - KinectSandbox.getInstance().settings.x1 + 1];
					        TerrainGeneratorHelper.resetBlocks();
				        }
				    }.runTaskLater(KinectSandbox.getInstance(), 1L);
            	    // read by main thread bukkit api to check if current updateTerrain called needs to cancel current block placements
            	    resetCalled = true;
            	}
            	
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
			
            			int upperRange = diffDepth[i][j][0];
            			int lowerRange = diffDepth[i][j][1];
            			int addOrRemove = diffDepth[i][j][2];
            			
            			// no changes to be made 
            			if (addOrRemove == -1)
            				continue;
            			
            			// adding blocks in range upper to lower
            			if (addOrRemove == 0)
            				for (int k = lowerRange; k < upperRange; k++)
            					TerrainGeneratorHelper.placeAsBiome(i, k, j, KinectSandbox.biome, true, false);
            					
            			
            			// removing blocks in range upper to lower
            			else if (addOrRemove == 1)
            				for (int k = lowerRange; k < upperRange; k++)
            					TerrainGeneratorHelper.placeAsBiome(i, k, j, KinectSandbox.biome, false, false);
            			
            			// add random veins, blocks, or etc to make biomes look more natural
            			//TerrainGeneratorHelper.touchUpBiome(KinectSandbox.biome);
            					
            		}
            	}
            	
            	initialized++;
            }
        }.runTask(KinectSandbox.getInstance()); // Ensures synchronous execution on the main thread

        
        prevDepth = currDepth;
	}
}

