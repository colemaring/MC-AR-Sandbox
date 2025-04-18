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
import Terrain.TerrainGenerator;
import Terrain.TerrainGeneratorHelper;
import net.md_5.bungee.api.ChatColor;

public class OreHunt2p {
	
	private static List<Location> placedVeins = new ArrayList<>();
	private static int leftFoundCount = 0;
	private static int rightFoundCount = 0;
	private static int taskID = -1;
	private static int leftPoints = 0;
	private static int rightPoints = 0;
	private static int DIFFICULTY = 1000; // changes how common ores are to spawn
	
	public static void initOreHunt()
	{
		//Bukkit.broadcastMessage(TerrainGeneratorHelper.findXEnd() + " " + TerrainGeneratorHelper.findZEnd());
		placeWall();
		placedVeins = new ArrayList<>();
    	leftFoundCount = 0;
    	rightFoundCount = 0;
    	leftPoints = 0;
    	rightPoints = 0;
		GamemodeHelper.stopCurrentGamemodeIfRunning();
        GamemodeHelper.setCurrentGameStopper(() -> {
            cleanUp();
            GamemodeHelper.cancelAllTasks(taskID);
            Bukkit.broadcastMessage(ChatColor.GOLD + "Ore Hunt (2P) has ended!");
            return;
        });
        
        startCountdown();
	}
	
	public static void placeWall()
	{
		int mid = TerrainGeneratorHelper.findZEnd()/2;
		for (int i = 0; i <= TerrainGeneratorHelper.findXEnd(); i++)
		{
			for (int j = 0; j <= 64; j++)
			{
				KinectSandbox.getInstance().world.getBlockAt(i, j, mid).setType(Material.BEDROCK);
			}
		}
		
	}
	
	public static void startCountdown() {
        GamemodeHelper.countdown("Ore Hunt (2P)", 3, () -> {
            // Runs after countdown finishes
        	startOreHunt();
        });
    }
	
	public static void startOreHunt()
	{
		
		GamemodeHelper.gamemodeRunning = true;
		Bukkit.broadcastMessage(ChatColor.GOLD + "Ore Hunt (2P) has begun, 30 second remain!");
		Bukkit.broadcastMessage(ChatColor.GREEN + "Remember, the vein must be completely uncovered for it to count");
		placeOres();
		
		// start the “uncovered” checker: runs once per second
		new BukkitRunnable() {
		    @Override
		    public void run() {
		        if (!GamemodeHelper.gamemodeRunning) {
		            this.cancel(); // Stop the task if the game has ended
		            return;
		        }

		        Iterator<Location> it = placedVeins.iterator();
		        while (it.hasNext()) {
		            Location center = it.next();
		            if (isVeinExposed(center)) {
		            	if (center.getBlockZ() > TerrainGeneratorHelper.findZEnd() / 2)
		            	{
		            		leftFoundCount++;
		            		if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.COAL_BLOCK)) {
			                	leftPoints += 5;
			                }
			                else if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.IRON_BLOCK)) {
			                	leftPoints += 10;
			                }
			                else if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.DIAMOND_BLOCK)) {
			                	leftPoints += 15;
			                }
			                else if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.EMERALD_BLOCK)) {
			                	leftPoints += 15;
			                }
		            	}
		            	else
		            	{
		            		rightFoundCount++;
		            		if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.COAL_BLOCK)) {
			                	rightPoints += 5;
			                }
			                else if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.IRON_BLOCK)) {
			                	rightPoints += 10;
			                }
			                else if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.DIAMOND_BLOCK)) {
			                	rightPoints += 15;
			                }
			                else if (KinectSandbox.getInstance().world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ()).getType().equals(Material.EMERALD_BLOCK)) {
			                	rightPoints += 15;
			                }
		            	}
		            		

		                // Launch a firework at the center
		                Location fireworkLoc = center.clone().add(1, 1, 1); // Centered above the vein
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
        	Bukkit.broadcastMessage(ChatColor.RED + "Time's up");
            Bukkit.broadcastMessage(ChatColor.RED + "Left player found " + ChatColor.AQUA + leftFoundCount + ChatColor.RED + " ore veins, for a total of " + ChatColor.AQUA + leftPoints + ChatColor.RED + " points.");
            Bukkit.broadcastMessage(ChatColor.RED + "Right player found " + ChatColor.AQUA + rightFoundCount + ChatColor.RED + " ore veins, for a total of " + ChatColor.AQUA + rightPoints + ChatColor.RED + " points.");
            cleanUp();
        }, 30 * 20L).getTaskId();
        GamemodeHelper.scheduledTaskIDs.add(gameTaskID);
	}
	
	public static void placeOres() {
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

	                                    if (KinectSandbox.getInstance().world.getBlockAt(x, y, z).getType().equals(Material.AIR) || KinectSandbox.getInstance().world.getBlockAt(x, y, z).getType().equals(Material.BEDROCK)) {
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
	                                // Place the 2x2x2 Glowstone vein
	                                for (int x = startX; x < startX + 2; x++) {
	                                    for (int y = startY; y < startY + 2; y++) {
	                                        for (int z = startZ; z < startZ + 2; z++) {
	                                        	
	                                        	if (num < 10) // 10 % chance
	                                        		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.EMERALD_BLOCK);
	                                        	else if (num < 50) // 40 % chance
	                                        		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.COAL_BLOCK);
	                                        	else if (num < 70) // 20 % chance
	                                        		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.DIAMOND_BLOCK);
	                                        	else // 30 % chance
	                                        		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.IRON_BLOCK);
	                                        }
	                                    }
	                                }
	                                
	                             // remember where we put it
	                                placedVeins.add(new Location(
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
                    if (surrounding != Material.AIR && surrounding != Material.IRON_BLOCK && surrounding != Material.EMERALD_BLOCK && surrounding != Material.COAL_BLOCK && surrounding != Material.DIAMOND_BLOCK && surrounding != Material.BEDROCK) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
	
	public static void cleanUp()
	{
		GamemodeHelper.currentGameStopper = null;
		GamemodeHelper.gamemodeRunning = false;
		TerrainGenerator.prevDepth = new int[TerrainGenerator.prevDepth.length][TerrainGenerator.prevDepth[0].length];
        TerrainGeneratorHelper.resetBlocks();
		leftPoints = 0;
		rightPoints = 0;
	}
	

}
