package Gamemodes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Main.KinectSandbox;
import net.md_5.bungee.api.ChatColor;

public class DigRouletteEasy {

	public static void startCountdown() {
        GamemodeHelper.countdown("Dig Roulette (easy)", 5, () -> {
            // Runs after countdown finishes
        	startOreHunt();
        });
    }
	
	public static void startOreHunt()
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Dig Roulette (easy) has begun, 30 second remain!");
	}
}
