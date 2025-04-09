package Gamemodes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Main.KinectSandbox;
import net.md_5.bungee.api.ChatColor;

public class DigRouletteHard {

	public static void startCountdown() {
        GamemodeHelper.countdown("Dig Roulette (hard)", 5, () -> {
            // Runs after countdown finishes
        	startOreHunt();
        });
    }
	
	public static void startOreHunt()
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Dig Roulette (hard) has begun, 30 second remain!");
	}
}
