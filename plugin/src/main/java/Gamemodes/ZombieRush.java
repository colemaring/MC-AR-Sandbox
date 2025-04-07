package Gamemodes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Main.KinectSandbox;
import net.md_5.bungee.api.ChatColor;

public class ZombieRush {

	public static void startCountdown(KinectSandbox plugin) {
        GamemodeHelper.countdown("Zombie Rush", 5, plugin, () -> {
            // Runs after countdown finishes
        	startOreHunt(plugin);
        });
    }
	
	public static void startOreHunt(KinectSandbox plugin)
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Zombie Rush has begun, 30 second remain!");
	}
}
