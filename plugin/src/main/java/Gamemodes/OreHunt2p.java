package Gamemodes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import Main.KinectSandbox;
import net.md_5.bungee.api.ChatColor;

public class OreHunt2p {

	public static void startCountdown() {
        GamemodeHelper.countdown("Ore Hunt 2P", 5, () -> {
            // Runs after countdown finishes
        	startOreHunt();
        });
    }
	
	public static void startOreHunt()
	{
		Bukkit.broadcastMessage(ChatColor.GOLD + "Ore Hunt 2P has begun, 30 second remain!");
	}
}
