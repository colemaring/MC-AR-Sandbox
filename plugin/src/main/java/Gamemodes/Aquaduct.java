package Gamemodes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import Guis.BiomeGui;
import Main.KinectSandbox;
import Terrain.TerrainGenerator;
import Terrain.TerrainGeneratorHelper;
import net.md_5.bungee.api.ChatColor;


// TODO
// remove calls to resetBlocks(), replace with more efficient method
public class Aquaduct {
	private static int taskID = -1;
	private static List<Location> placedBlocks = new ArrayList<>();
	public static void startCountdown() {
		// First stop any games if they exist
    	GamemodeHelper.stopCurrentGamemodeIfRunning();
        GamemodeHelper.setCurrentGameStopper(() -> {
            cleanUp();
            GamemodeHelper.cancelAllTasks(taskID);
            Bukkit.broadcastMessage(ChatColor.GOLD + "Aquaduct has ended!");
            // reset terrain here
            return;
        });
        
        // Remove water if it is enabled
		if (KinectSandbox.getInstance().waterEnabled == true)
		{
			KinectSandbox.getInstance().waterEnabled = false;
			BiomeGui.waterElement.setState("waterDisabled");
			Bukkit.broadcastMessage(ChatColor.RED + "Disabling terrain water before starting Aquaduct");
			TerrainGeneratorHelper.removeWater(20, 20, null);
		}
        
        int gameTaskID = Bukkit.getScheduler().runTaskLater(KinectSandbox.getInstance(), () -> {
        	GamemodeHelper.gamemodeRunning = false;
        	Bukkit.broadcastMessage(ChatColor.DARK_RED + "Time's up!");
            cleanUp();
        }, 10 * 60 * 20L).getTaskId();
        GamemodeHelper.scheduledTaskIDs.add(gameTaskID);
        
        // Schedule 10-second warning and startCountdown, and store their IDs.
        int warningTaskID = Bukkit.getScheduler().runTaskLater(KinectSandbox.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Warning: 1 minute remaining to redirect the water!");
        }, 9 * 60 * 20L).getTaskId();
        GamemodeHelper.scheduledTaskIDs.add(warningTaskID);
        
        GamemodeHelper.countdown("Aquaduct", 3, () -> {
            // Runs after countdown finishes
        	KinectSandbox.allowWaterFlow = true;
        	createSourceAndSink();
        	// Allow water to flow for the duration of the gamemode
        	// Ensure the source (water source) cannot be deleted
        	
        });
    }
	
	public static void createSourceAndSink()
	{
		// Scan through all blocks to find the highest
		// We'll reference this when placing the source
		int maxX = -1;
		int maxZ = -1;
		int max = -1;
		for (int x = 0; x < TerrainGeneratorHelper.findXEnd(); x++)
		{
			for (int z = 0; z < TerrainGeneratorHelper.findZEnd(); z++)
			{
				if (KinectSandbox.getInstance().world.getHighestBlockYAt(x, z) > max)
				{
					// find highest block
					int count = 0;
					for (int i = 0; i < 300; i++)
					{
						if (!KinectSandbox.getInstance().world.getBlockAt(x, i, z).getType().equals(Material.AIR))
							count++;
					}
					if (count > max)
					{
						max = count;
						maxX = x;
						maxZ = z;
					}
				}
			}
		}
		
		// Create the source 2x3x3, centered at maxX, maxZ
		for (int i = 0; i < 2; i ++)
			for (int j = -1; j < 2; j++)
				for (int k = -1; k < 2; k++)
					KinectSandbox.getInstance().world.getBlockAt(j + maxX, max + i, k + maxZ).setType(Material.WATER);
		
		// Emit a firework centered at the source, a few blocks up
		Location fireworkLoc = new Location(
		    KinectSandbox.getInstance().world,
		    maxX + 1.5,
		    max + 4,
		    maxZ + 1.5
		);

		Firework firework = (Firework) KinectSandbox.getInstance().world.spawn(fireworkLoc, Firework.class);

		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(1);
		meta.addEffect(FireworkEffect.builder()
		    .withColor(Color.YELLOW)
		    .withFade(Color.ORANGE)
		    .with(FireworkEffect.Type.BALL_LARGE)
		    .trail(true)
		    .flicker(true)
		    .build());
		firework.setFireworkMeta(meta);
		
		// --------------------------------------------------------
		 // SINK
		int bestY = Integer.MAX_VALUE;
		int bestX = -1;
		int bestZ = -1;

		for (int x = 5; x < TerrainGeneratorHelper.findXEnd() - 5; x+= 15) {
		    for (int z = 5; z < TerrainGeneratorHelper.findZEnd() - 5; z+=15) {
		        for (int y = 0; y < max + 10; y+=2) {
		            boolean allAir = true;
		            outer:
		            for (int i = 0; i < 2; i++) {
		                for (int dx = 0; dx < 8; dx++) {
		                    for (int dz = 0; dz < 8; dz++) {
		                        if (KinectSandbox.getInstance().world
		                                .getBlockAt(x + dx, y + i, z + dz)
		                                .getType() != Material.AIR) {
		                            allAir = false;
		                            break outer;
		                        }
		                    }
		                }
		            }

		            if (allAir && y - 5 < bestY) {
		                bestY = y - 5; // Bucket sits just below the air pocket
		                bestX = x;
		                bestZ = z;
		            }
		        }
		    }
		}

	    if (bestY == Integer.MAX_VALUE) {
	        Bukkit.broadcastMessage(ChatColor.RED + "Could not find a valid spot for the sink.");
	        GamemodeHelper.stopCurrentGamemodeIfRunning();
	        return;
	    }

	    // Build the 8×8×3 “bucket” of GOLD_BLOCK
	    for (int i = 0; i < 6; i++) {
	        for (int dx = 0; dx < 8; dx++) {
	            for (int dz = 0; dz < 8; dz++) {
	                // only rim on layers 1 & 2
	                if ((i == 1 || i == 2) && dx != 0 && dx != 7 && dz != 0 && dz != 7)
	                {
	                	KinectSandbox.getInstance()
	                    .world
	                    .getBlockAt(bestX + dx, bestY + i, bestZ + dz)
	                    .setType(Material.AIR);
	                	placedBlocks.add(new Location(
	                            KinectSandbox.getInstance().world,
	                            bestX + dx, bestY + i, bestZ + dz
	                        ));
	                	continue;
	                }
	                
	                if (i==3 || i == 4 || i == 5)
	                {
	                	KinectSandbox.getInstance()
	                    .world
	                    .getBlockAt(bestX + dx, bestY + i, bestZ + dz)
	                    .setType(Material.AIR);
	                	continue;
	                }
	                    
	                KinectSandbox.getInstance()
	                    .world
	                    .getBlockAt(bestX + dx, bestY + i, bestZ + dz)
	                    .setType(Material.GOLD_BLOCK);
	                
	                placedBlocks.add(new Location(
                            KinectSandbox.getInstance().world,
                            bestX + dx, bestY + i, bestZ + dz
                        ));
	                
	                KinectSandbox.getInstance().world.getBlockAt(bestX + dx, bestY + i, bestZ + dz).getState().update(true, true);
	                
	            }
	        }
	    }
	    
	    int sinkX = bestX;
	    int sinkY = bestY;
	    int sinkZ = bestZ;

	    // Firework above the sink
	    fireworkLoc = new Location(
	        KinectSandbox.getInstance().world,
	        bestX + 3.5,  // center of 8 wide
	        bestY + 4,
	        bestZ + 3.5
	    );
	    firework = (Firework)
	        KinectSandbox.getInstance().world.spawn(fireworkLoc, Firework.class);
	    meta = firework.getFireworkMeta();
	    meta.setPower(1);
	    meta.addEffect(FireworkEffect.builder()
	        .withColor(Color.YELLOW)
	        .withFade(Color.ORANGE)
	        .with(FireworkEffect.Type.BALL_LARGE)
	        .trail(true)
	        .flicker(true)
	        .build()
	    );
	    firework.setFireworkMeta(meta);

	    Bukkit.broadcastMessage(
	        ChatColor.GREEN + "Aquaduct source and sink have been placed."
	    );
	    Bukkit.broadcastMessage(
	        ChatColor.GREEN + "You have 10 minutes to redirect the water."
	    );

	    final int finalSinkX = sinkX;
	    final int finalSinkY = sinkY;
	    final int finalSinkZ = sinkZ;

	    final long startTime = System.currentTimeMillis();
	    taskID = new BukkitRunnable() {
	        @Override
	        public void run() {
	            // scan the 6×6 interior at y = sinkY + 1
	            for (int dx = 1; dx <= 6; dx++) {
	                for (int dz = 1; dz <= 6; dz++) {
	                    for (int dy = 1; dy <= 5; dy++) { // Loop vertically from sinkY + 1 to sinkY + 5
	                        Block block = KinectSandbox.getInstance().world.getBlockAt(finalSinkX + dx, finalSinkY + dy, finalSinkZ + dz);
	                        Material type = block.getType();
	                        
	                        if ((dy == 1 || dy == 2) && type == Material.WATER) { 
	                            // Only check for water at sinkY + 1
	                            int elapsedSec = (int)((System.currentTimeMillis() - startTime) / 1000);
	                            Bukkit.broadcastMessage(
	                                ChatColor.GOLD +
	                                "You completed the aquaduct in " +
	                                elapsedSec +
	                                " seconds!"
	                            );
	                            this.cancel();
	                            new BukkitRunnable() {
	                                @Override
	                                public void run() {
	                                    GamemodeHelper.stopCurrentGamemodeIfRunning();
	                                }
	                            }.runTaskLater(KinectSandbox.getInstance(), 2 * 20L);
	                            return;
	                        }

	                        if (type != Material.AIR) {
	                            block.setType(Material.AIR);
	                        }
	                    }
	                }
	            }
	        }
	    }
	    .runTaskTimer(KinectSandbox.getInstance(), 20L, 20L)
	    .getTaskId();
	    GamemodeHelper.scheduledTaskIDs.add(taskID);
	}
	
	public static void cleanUp()
	{
		// copy array
		int [][] prevDepthMinusBucket = new int[TerrainGenerator.prevDepth.length][TerrainGenerator.prevDepth[0].length];
		for (int i = 0; i < prevDepthMinusBucket.length; i++)
			for (int j = 0; j < prevDepthMinusBucket[0].length; j++)
				prevDepthMinusBucket[i][j] = TerrainGenerator.prevDepth[i][j];
		
		
		for (Location veinCorner : placedBlocks)
		{
			TerrainGeneratorHelper.placeAsBiome(veinCorner.getBlockX(), veinCorner.getBlockY(), veinCorner.getBlockZ(), KinectSandbox.biome, true, true);
			prevDepthMinusBucket[veinCorner.getBlockX()][veinCorner.getBlockZ()] = 0; // this will force and update on these x, z coords 
		}
		GamemodeHelper.currentGameStopper = null;
		GamemodeHelper.gamemodeRunning = false;
		KinectSandbox.allowWaterFlow = false;
		TerrainGeneratorHelper.removeWater(20, 20, null);
		
		TerrainGenerator.prevDepth = prevDepthMinusBucket;
//		TerrainGeneratorHelper.resetBlocks();
	}
}
