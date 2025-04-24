package Gamemodes;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class Aquaduct {

	public static void startCountdown() {
        GamemodeHelper.countdown("Aquaduct", 3, () -> {
            // Runs after countdown finishes
        	startOreHunt();
        });
    }
	
	public static void startOreHunt()
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Aquaduct is not yet implemented.");
	}
}
