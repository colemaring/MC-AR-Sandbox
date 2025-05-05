package Gamemodes;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import Main.KinectSandbox;
import Misc.MiscHandlers;
import net.md_5.bungee.api.ChatColor;

public class GamemodeHelper {
	public static boolean gamemodeRunning = false;
    // This Runnable will hold the cleanup logic for the currently running gamemode.
    public static Runnable currentGameStopper = null;
    public static Set<Integer> scheduledTaskIDs = new HashSet<>();
	 public static int countdown(String name, int time, Runnable onFinish) {
		 BukkitTask task = new BukkitRunnable() {
	            int secondsLeft = time;

	            @Override
	            public void run() {
	                if (secondsLeft > 0) {
	                    String title = ChatColor.YELLOW + "Starting " + name;
	                    String subtitle = ChatColor.RED + "in " + secondsLeft + "...";

	                    for (Player player : Bukkit.getOnlinePlayers()) {
	                        player.sendTitle(title, subtitle, 0, 18, 0);
	                    }

	                    secondsLeft--;
	                } else {
	                    for (Player player : Bukkit.getOnlinePlayers()) {
	                        player.sendTitle(ChatColor.GREEN + name, ChatColor.GRAY + "has started!", 10, 40, 10);
	                    }
	                    
	                    MiscHandlers.killEntities();

	                    if (onFinish != null) {
	                        onFinish.run();
	                    }

	                    cancel();
	                }
	            }
	        }.runTaskTimer(KinectSandbox.getInstance(), 0L, 20L);
	        return task.getTaskId();
	    }

	 
	 public static void setCurrentGameStopper(Runnable stopper) {
	        // If a game is already running, cancel it first
	        stopCurrentGamemodeIfRunning();
	        currentGameStopper = stopper;
	        gamemodeRunning = true;
	    }
	    
    /**
     * Call this to stop any current gamemode.
     */
    public static void stopCurrentGamemodeIfRunning() {
        if (currentGameStopper != null) {
            currentGameStopper.run();
            currentGameStopper = null;
            MiscHandlers.killEntities();
        }
        else
        {
        	//Bukkit.broadcastMessage(ChatColor.YELLOW + "No active gamemodes.");
        }
        
        gamemodeRunning = false;
        scheduledTaskIDs.clear();
    }
    
    public static void cancelAllTasks(int taskID) {
        for (Integer id : scheduledTaskIDs) {
            Bukkit.getScheduler().cancelTask(id);
        }
        scheduledTaskIDs.clear();
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1;
        }
    }

}
