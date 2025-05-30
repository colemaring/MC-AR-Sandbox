package Gamemodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import Main.KinectSandbox;
import Terrain.TerrainGeneratorHelper;

public class ZombieRush {
    private static int taskID = -1;
    private static int timeLeft = 60;
    public static boolean running = false;
    private static Set<UUID> reachedZombies = new HashSet<>();
    // Map to track each zombie's individual target location.
    private static Map<UUID, Location> zombieTargets = new HashMap<>();
    
    public static void prepareTerrain() {
    	// First stop any games if they exist
    	GamemodeHelper.stopCurrentGamemodeIfRunning();
        GamemodeHelper.setCurrentGameStopper(() -> {
            cleanupZombiesAndTargets();
            GamemodeHelper.cancelAllTasks(taskID);
            Bukkit.broadcastMessage(ChatColor.GOLD + "Zombie Rush has ended!");
            // reset terrain here
            return;
        });

        
        startCountdown();
    }

    public static void startCountdown() {
		 int taskId  =GamemodeHelper.countdown("Zombie Rush", 3, () -> {
			if (!GamemodeHelper.gamemodeRunning)
				return;
			startZombieRush();
       });
       GamemodeHelper.scheduledTaskIDs.add(taskId);
   }
    
    public static void startZombieRush() {
    	running = true;
        Bukkit.broadcastMessage(ChatColor.GOLD + "Zombie Rush has begun. 1 minute remains!");
     // Start the timer
        timeLeft = 60;
        reachedZombies.clear();
        zombieTargets.clear();
        int xEnd = TerrainGeneratorHelper.findXEnd();
        int zEnd = TerrainGeneratorHelper.findZEnd();
        startTimer();
        for (int i = 0; i < xEnd; i++) {
            spawnZombie(i, zEnd);
            spawnZombie(i - 1, zEnd);
        }
        
    }
    
    // Spawn zombie on surface at i, j, attracted to walk right
    public static void spawnZombie(int i, int j) {
        World world = Bukkit.getWorlds().get(0);
        j--;
        int y = world.getHighestBlockYAt(i, j) + 1;
        Location spawnLocation = new Location(world, i, y, j);
        //Bukkit.broadcastMessage(i + " " + y + " " + j);
        Zombie zombie = (Zombie) world.spawnEntity(spawnLocation, EntityType.ZOMBIE);
        zombie.setRemoveWhenFarAway(false);
        zombie.setAge(0); // Ensure the zombie is an adult
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        zombie.leaveVehicle();
        
        // Calculate a unique target location for this zombie.
        // In this example, we set the target to have the same x as the zombie, z fixed to 1,
        // and the y based on the highest block at that location.
        int targetY = world.getHighestBlockYAt(i, 1) + 1;
        Location targetLocation = new Location(world, i, targetY, 1);
        
        // Store this zombie's individual target.
        zombieTargets.put(zombie.getUniqueId(), targetLocation);
        
        // Set the zombie's target using an invisible armor stand
        attractZombieTo(zombie, targetLocation);
    }
    
    // Have zombies be attracted to an invisible target on the right side of terrain
    public static void attractZombieTo(Zombie zombie, Location targetLocation) {
        World world = zombie.getWorld();
        ArmorStand target = world.spawn(targetLocation, ArmorStand.class);

        // Make the armor stand invisible and invulnerable.
        target.setVisible(false);
        target.setInvulnerable(true);
        target.setGravity(false);
        target.setMarker(true); // Tiny hitbox
        target.setCollidable(false);
        target.setCustomName("ZombieAttractor");
        target.setCustomNameVisible(false);

        // Set the zombie's target to the invisible armor stand so that its pathfinder will guide it there.
        zombie.setTarget(target);
    }
    
    public static void startTimer() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID); // Cancel any existing timer
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    this.cancel();
//                    Bukkit.broadcastMessage(ChatColor.GOLD + "Zombie Rush has ended!");
                    Bukkit.broadcastMessage(ChatColor.DARK_RED + "Time's up!");
                    Bukkit.broadcastMessage(ChatColor.AQUA + "" + reachedZombies.size() + "/" + ( TerrainGeneratorHelper.findXEnd()*2) + ChatColor.RED + " zombies reached the end.");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                        	GamemodeHelper.stopCurrentGamemodeIfRunning();
                        }
                    }.runTaskLater(KinectSandbox.getInstance(), 2 * 20L);
                    taskID = -1; // Reset task ID
                    return;
                }

                // Send warnings at 30 and 10 seconds remaining
                if (timeLeft == 30 || timeLeft == 10) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Zombie Rush: " + timeLeft + " seconds remaining!");
                }

                // For each zombie in the world, check if it has reached its individual target.
                World world = Bukkit.getWorlds().get(0);
                for (Entity entity : world.getEntitiesByClass(Zombie.class)) {
                    Zombie zombie = (Zombie) entity;
                    Location target = zombieTargets.get(zombie.getUniqueId());
                    if (target != null && zombie.getLocation().distanceSquared(target) < 5) {
                        reachedZombies.add(zombie.getUniqueId());
                        zombie.remove(); // Remove the zombie once it reached its target
                    }
                    
                }

                timeLeft--;
            }
        }.runTaskTimer(KinectSandbox.getInstance(), 0L, 20L); // Runs every 20 ticks (1 second)

        taskID = task.getTaskId();
    }
    
    public static void cleanupZombiesAndTargets() {
    	running = false;
        World world = Bukkit.getWorlds().get(0);
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Zombie 
                || (entity instanceof ArmorStand 
                    && entity.getCustomName() != null 
                    && entity.getCustomName().equals("ZombieAttractor"))) {
                entity.remove();
            }
        }
    }
}
