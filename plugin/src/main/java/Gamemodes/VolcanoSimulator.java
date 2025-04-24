package Gamemodes;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class VolcanoSimulator {

	public static void startCountdown() {
        GamemodeHelper.countdown("Volcano Simulator", 3, () -> {
            // Runs after countdown finishes
        	startOreHunt();
        });
    }
	
	public static void startOreHunt()
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Volcano Simulator is not yet implemented");
	}
}
