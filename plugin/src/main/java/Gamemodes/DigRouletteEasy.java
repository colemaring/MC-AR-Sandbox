package Gamemodes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Main.KinectSandbox;
import net.md_5.bungee.api.ChatColor;

public class DigRouletteEasy {

	public static void startCountdown(KinectSandbox plugin) {
        GamemodeHelper.countdown("Dig Roulette (easy)", 5, plugin, () -> {
            // Runs after countdown finishes
        	startOreHunt(plugin);
        });
    }
	
	public static void startOreHunt(KinectSandbox plugin)
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Dig Roulette (easy) has begun, 30 second remain!");
	}
}
