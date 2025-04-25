package Gamemodes;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import Guis.BiomeGui;
import Main.KinectSandbox;
import Terrain.TerrainGenerator;
import Terrain.TerrainGeneratorHelper;
import net.md_5.bungee.api.ChatColor;

public class Aquaduct {
	private static int taskID = -1;
	public static void startCountdown() {
		// First stop any games if they exist
    	GamemodeHelper.stopCurrentGamemodeIfRunning();
        GamemodeHelper.setCurrentGameStopper(() -> {
            cleanUp();
            GamemodeHelper.cancelAllTasks(taskID);
            TerrainGeneratorHelper.unpauseTerrain();
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
			TerrainGenerator.prevDepth = new int[TerrainGenerator.prevDepth.length][TerrainGenerator.prevDepth[0].length];
	        TerrainGeneratorHelper.resetBlocks();
		}
        
        int gameTaskID = Bukkit.getScheduler().runTaskLater(KinectSandbox.getInstance(), () -> {
        	GamemodeHelper.gamemodeRunning = false;
        	Bukkit.broadcastMessage(ChatColor.DARK_RED + "Time's up!");
            cleanUp();
        }, 63 * 20L).getTaskId();
        GamemodeHelper.scheduledTaskIDs.add(gameTaskID);
        
        // Schedule 10-second warning and startCountdown, and store their IDs.
        int warningTaskID = Bukkit.getScheduler().runTaskLater(KinectSandbox.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Warning: 10 seconds remaining to redirect the water!");
        }, 53 * 20L).getTaskId();
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
		Bukkit.broadcastMessage("Source spawned at x: " + maxX + ", z: " + maxZ + ", y: " + max);
		
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
		                for (int dx = 0; dx < 6; dx++) {
		                    for (int dz = 0; dz < 6; dz++) {
		                        if (KinectSandbox.getInstance().world
		                                .getBlockAt(x + dx, y + i, z + dz)
		                                .getType() != Material.AIR) {
		                            allAir = false;
		                            break outer;
		                        }
		                    }
		                }
		            }

		            if (allAir && y - 1 < bestY) {
		                bestY = y - 1; // Bucket sits just below the air pocket
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
	    for (int i = 0; i < 3; i++) {
	        for (int dx = 0; dx < 8; dx++) {
	            for (int dz = 0; dz < 8; dz++) {
	                // only rim on layers 1 & 2
	                if ((i == 1 || i == 2) && dx != 0 && dx != 7 && dz != 0 && dz != 7)
	                    continue;
	                KinectSandbox.getInstance()
	                    .world
	                    .getBlockAt(bestX + dx, bestY + i, bestZ + dz)
	                    .setType(Material.GOLD_BLOCK);
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
	        ChatColor.GREEN + "Sink spawned at x: " +
	        sinkX + ", z: " + sinkZ + ", y: " + sinkY
	    );
	    Bukkit.broadcastMessage(
	        ChatColor.GREEN + "Aqueduct source and sink have been placed."
	    );
	    Bukkit.broadcastMessage(
	        ChatColor.GREEN + "You have 1 minute to redirect the water."
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
	                    if (KinectSandbox.getInstance().world
	                            .getBlockAt(finalSinkX + dx, finalSinkY + 1, finalSinkZ + dz)
	                            .getType() == Material.WATER) {
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
	                }
	            }
	        }
	    }
	    .runTaskTimer(KinectSandbox.getInstance(), 20L, 20L)
	    .getTaskId();
	    GamemodeHelper.scheduledTaskIDs.add(taskID);
		
		
		Bukkit.broadcastMessage(ChatColor.GREEN + "Aquaduct source and sink have been placed.");
		Bukkit.broadcastMessage(ChatColor.GREEN + "You have 1 minute to redirect the water.");
	}
	
	public static void cleanUp()
	{
		GamemodeHelper.currentGameStopper = null;
		GamemodeHelper.gamemodeRunning = false;
		KinectSandbox.allowWaterFlow = false;
		TerrainGenerator.prevDepth = new int[TerrainGenerator.prevDepth.length][TerrainGenerator.prevDepth[0].length];
		TerrainGeneratorHelper.resetBlocks();
		
	}
}
