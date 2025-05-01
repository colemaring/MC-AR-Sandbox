package Gamemodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;
import Terrain.TerrainGeneratorHelper;
import net.md_5.bungee.api.ChatColor;

public class DigRouletteEasy {
	private static List<Location> placedVeins = new ArrayList<>();
	private static List<Location> placedVeins2 = new ArrayList<>();
	private static int foundCount = 0;
	private static int taskID = -1;
	private static int DIFFICULTY = 1000; // 1 in DIFFICULTY chance that if there is a valid spot to put a block, we will put it there (either gold or TNT)
	
	public static void initDigRoulette()
	{
		placedVeins = new ArrayList<>();
    	foundCount = 0;
		GamemodeHelper.stopCurrentGamemodeIfRunning();
        GamemodeHelper.setCurrentGameStopper(() -> {
            cleanUp();
            GamemodeHelper.cancelAllTasks(taskID);
            Bukkit.broadcastMessage(ChatColor.GOLD + "Dig Roulette has ended!");
            return;
        });
        
        startCountdown();
	}
	
	public static void startCountdown() {
        GamemodeHelper.countdown("Dig Roulette", 3, () -> {
        	startDigRoulette();
        });
    }
	
	public static void startDigRoulette()
	{
		
		GamemodeHelper.gamemodeRunning = true;
		Bukkit.broadcastMessage(ChatColor.GOLD + "Dig Roulette (easy) has begun, 30 second remain!");
		Bukkit.broadcastMessage(ChatColor.GREEN + "Remember, the gold must be completely uncovered for it to count");
		placeBlocks();
		
		// start the uncovered checker
		new BukkitRunnable() {
		    @Override
		    public void run() {
		        if (!GamemodeHelper.gamemodeRunning) {
		            this.cancel();
		            return;
		        }

		        Iterator<Location> it = placedVeins.iterator();
		        while (it.hasNext()) {
		            Location center = it.next();
		            if (isVeinExposed(center)) {
		                if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.GOLD_BLOCK))
		                	foundCount++;

		                // Launch a firework at the center
		                Location fireworkLoc = center.clone().add(1, 1, 1);
		                Firework firework = (Firework) center.getWorld().spawn(fireworkLoc, Firework.class);

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

		                it.remove();
		                
		                if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.TNT))
		                    gameOver();
		            }

		        }
		    }
		}.runTaskTimer(KinectSandbox.getInstance(), 20L, 20L);
		

		int warningTaskID = Bukkit.getScheduler().runTaskLater(KinectSandbox.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Warning: 10 seconds remaining!");
        }, 20 * 20L).getTaskId();
        GamemodeHelper.scheduledTaskIDs.add(warningTaskID);


        int gameTaskID = Bukkit.getScheduler().runTaskLater(KinectSandbox.getInstance(), () -> {
        	GamemodeHelper.gamemodeRunning = false;
            Bukkit.broadcastMessage(ChatColor.RED + "Time's up, you found " + foundCount + " gold.");
            cleanUp();
        }, 30 * 20L).getTaskId();
        GamemodeHelper.scheduledTaskIDs.add(gameTaskID);
	}
	
	public static void placeBlocks() {
	    new BukkitRunnable() {
	        @Override
	        public void run() {
	            Random random = new Random();

	            // Iterate through potential starting points for the 2x2x2 vein center
	            for (int i = 0; i < TerrainGeneratorHelper.findXEnd() ; i += 2)
	            {
	                for (int j = 0; j < TerrainGeneratorHelper.findZEnd(); j += 2)
	                {
	                    for (int k = -100; k < 100 - 2; k += 2)
	                    {

	                        // Define the core 2x2x2 placement area's min corner
	                        int startX = i + 1;
	                        int startY = k + 1;
	                        int startZ = j + 1;

	                        boolean touchesAir = false;

	                        // Check a 4x4x4 volume centered around the 2x2x2 placement area
	                        // This checks the placement blocks AND their immediate neighbors
	                        for (int x = startX - 1; x <= startX + 2; x++) {
	                            for (int y = startY - 1; y <= startY + 2; y++) {
	                                for (int z = startZ - 1; z <= startZ + 2; z++) {

	                                    if (KinectSandbox.getInstance().world.getBlockAt(x, y, z).getType().equals(Material.AIR)) {
	                                        touchesAir = true;
	                                        break;
	                                    }
	                                }
	                                if (touchesAir)
	                                	break;
	                            }
	                            if (touchesAir)
	                            	break;
	                        }

	                        // If the 4x4x4 area surrounding the 2x2x2 spot does NOT touch air
	                        if (!touchesAir) {
	                            // Now apply the random chance
	                            if (random.nextInt(DIFFICULTY) == 0) { // 1 in x chance
	                                //Bukkit.broadcastMessage(ChatColor.GOLD + "Placed vein centered near " + startX + " " + startY + " " + startZ);
	                            	int num = random.nextInt(100);
	                                for (int x = startX; x < startX + 2; x++) {
	                                    for (int y = startY; y < startY + 2; y++) {
	                                        for (int z = startZ; z < startZ + 2; z++) {
	                                        	if (num < 5) // 5% chance of TNT. only diff between easy and hard dig roulette.
	                                        		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.TNT);
	                                        	else // 95% chance gold
	                                        		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.GOLD_BLOCK);
	                                        }
	                                    }
	                                }
	                                
	                             // remember where we put it
	                                placedVeins.add(new Location(
	                                    KinectSandbox.getInstance().world,
	                                    startX, startY, startZ
	                                ));
	                                placedVeins2.add(new Location(
	                                        KinectSandbox.getInstance().world,
	                                        startX, startY, startZ
	                                    ));
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }.runTask(KinectSandbox.getInstance());
	}
	
	private static boolean isVeinExposed(Location center) {
        for (int x = center.getBlockX() - 1; x <= center.getBlockX() + 2; x++) {
            for (int y = center.getBlockY() - 1; y <= center.getBlockY() + 2; y++) {
                for (int z = center.getBlockZ() - 1; z <= center.getBlockZ() + 2; z++) {
                    // skip the 2×2×2 core
                    boolean inCoreX = (x >= center.getBlockX() && x < center.getBlockX() + 2);
                    boolean inCoreY = (y >= center.getBlockY() && y < center.getBlockY() + 2);
                    boolean inCoreZ = (z >= center.getBlockZ() && z < center.getBlockZ() + 2);
                    if (inCoreX && inCoreY && inCoreZ) continue;

                    // can be surrounded by air or ore from other veins
                    Material surrounding = center.getWorld().getBlockAt(x, y, z).getType();
                    if (surrounding != Material.AIR && surrounding != Material.WATER && surrounding != Material.TNT) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
	
	public static void gameOver() {
	    Bukkit.broadcastMessage(ChatColor.DARK_RED + "Game over!");
	    Bukkit.broadcastMessage(ChatColor.RED + "You found " + ChatColor.AQUA + foundCount + ChatColor.RED + " gold before hitting TNT.");

	    // Schedule to run 2 secs
	    new BukkitRunnable() {
	        @Override
	        public void run() {
	            GamemodeHelper.stopCurrentGamemodeIfRunning();
	        }
	    }.runTaskLater(KinectSandbox.getInstance(), 2 * 20L);
	}

	
	public static void cleanUp()
	{
		for (Location veinCorner : placedVeins2)
		{
			if (isVeinExposed(veinCorner))
			{
				for (int x = veinCorner.getBlockX(); x < veinCorner.getBlockX() + 2; x++)
				{
					for (int y = veinCorner.getBlockY(); y < veinCorner.getBlockY() + 2; y++)
					{
						for (int z = veinCorner.getBlockZ(); z < veinCorner.getBlockZ() + 2; z++)
						{
								TerrainGeneratorHelper.placeAsBiome(x, y, z, KinectSandbox.biome, false, true);
						}
					}
						
				}
			}
			else
			{
				for (int x = veinCorner.getBlockX(); x < veinCorner.getBlockX() + 2; x++)
				{
					for (int y = veinCorner.getBlockY(); y < veinCorner.getBlockY() + 2; y++)
					{
						for (int z = veinCorner.getBlockZ(); z < veinCorner.getBlockZ() + 2; z++)
						{
								TerrainGeneratorHelper.placeAsBiome(x, y, z, KinectSandbox.biome, true, true);
						}
					}
						
				}
			}			
		}		

		placedVeins2.clear(); // Clear the list after restoring blocks
		placedVeins.clear();
		
		GamemodeHelper.currentGameStopper = null;
		GamemodeHelper.gamemodeRunning = false;
		foundCount = 0;
	}
}
