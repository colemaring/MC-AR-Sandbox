package Gamemodes;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class OreHunt {

	public static void startCountdown() {
        GamemodeHelper.countdown("Ore Hunt", 3, () -> {
            // Runs after countdown finishes
        	startOreHunt();
        });
    }
	
	public static void startOreHunt()
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Ore Hunt has begun, 30 second remain!");
	}
	public static void startOreHunt2() //testing gha
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Ore Hunt has begun, 30 second remain!");
	}
	public static void startOreHunt1()//testing gha
	
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Ore Hunt has begun, 30 second remain!");
	}
}
