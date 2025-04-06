import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.md_5.bungee.api.ChatColor;

public class GamemodeGui {
	private KinectSandbox plugin;
	public GamemodeGui(KinectSandbox plugin)
	{
		this.plugin = plugin;
	}
	
	public InventoryGui createGui()
	{
		String[] guiSetup = {
				"         ",
	            " abcdefg ",
	            "    z    "
	        };
		InventoryGui gui = new InventoryGui(plugin, null, "Gamemode Menu", guiSetup);
		gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
		
		gui.addElement(new StaticGuiElement('z',
			    new ItemStack(Material.REDSTONE_BLOCK),
			    1,
			    click -> {

			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Stopping gamemode");
					}
			    	return true;
			    },
			    "§aEnd current gamemode"
			));

		gui.addElement(new StaticGuiElement('a',
			    new ItemStack(Material.DIAMOND_PICKAXE),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to find_ore..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aFind Ore!",
			    "§7A short description about how to play this game, you only have 30 seconds!."
			));
		gui.addElement(new StaticGuiElement('b',
			    new ItemStack(Material.GOLDEN_PICKAXE),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to find_ore..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aFind Ore (2 Players)!",
			    "§7A short description about how to play this game, you only have 30 seconds!."
			));
		gui.addElement(new StaticGuiElement('c',
			    new ItemStack(Material.ZOMBIE_HEAD),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aZombie Rush!",
			    "§7A short description about how to play this game, you only have 30 seconds!."
			));
		gui.addElement(new StaticGuiElement('d',
			    new ItemStack(Material.TNT),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aMinesweeper!",
			    "§7Get as many points as possible without hitting tnt."
			));
		gui.addElement(new StaticGuiElement('e',
			    new ItemStack(Material.OBSIDIAN),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aVolcano Simulator!",
			    "§7A short description about how to play this game, you only have 30 seconds!."
			));
		gui.addElement(new StaticGuiElement('f',
			    new ItemStack(Material.SNOW_BLOCK),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aTallest Mountain!",
			    "§7A short description about how to play this game, you only have 30 seconds!."
			));
		gui.addElement(new StaticGuiElement('g',
			    new ItemStack(Material.WATER),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aAquaduct!",
			    "§7Redirect water from the source to the end."
			));
		
		
		return gui;
	}
}
