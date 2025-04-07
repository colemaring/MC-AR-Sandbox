package Gamemodes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;
import net.md_5.bungee.api.ChatColor;

public class GamemodeHelper {
	 public static void countdown(String name, int time, KinectSandbox plugin, Runnable onFinish) {
	        new BukkitRunnable() {
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

	                    if (onFinish != null) {
	                        onFinish.run();
	                    }

	                    cancel();
	                }
	            }
	        }.runTaskTimer(plugin, 0L, 20L);
	    }
}
