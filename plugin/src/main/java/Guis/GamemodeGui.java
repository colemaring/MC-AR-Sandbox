package Guis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Main.KinectSandbox;
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

		ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
		ItemMeta meta = diamondPickaxe.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		diamondPickaxe.setItemMeta(meta);
		
		gui.addElement(new StaticGuiElement('a',
			    diamondPickaxe,
			    1,
			    click -> {
			        for (Player player : Bukkit.getOnlinePlayers()) {
			            player.sendMessage(ChatColor.GREEN + "Changing gamemode to find_ore..");
			            player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
			        }
			        return true;
			    },
			    "§aOre Hunt",
			    "§7",
			    "§7Find as many buried ores as possible in 30 seconds.",
			    "§7Coal = 5 pts",
			    "§7Iron = 10 pts",
			    "§7Diamond = 15 pts",
			    "§7Emerald = 15 pts",
			    "§7",
			    "§bClick to start"
			));

		
		ItemStack goldenPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
		meta = goldenPickaxe.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		goldenPickaxe.setItemMeta(meta);
		
		gui.addElement(new StaticGuiElement('b',
				goldenPickaxe,
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to find_ore..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aOre Hunt (2 Player)",
			    "§7",
			    "§7Compete to find as many buried ores as possible in 30 seconds.",
			    "§7Coal = 5 pts",
			    "§7Iron = 10 pts",
			    "§7Diamond = 15 pts",
			    "§7Emerald = 15 pts",
			    "§7",
			    "§bClick to start"
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
			    "§aZombie Rush",
			    "§7",
			    "§7Stop an army of zombies from traversing your terrain.",
			    "§7You have 30 second to prepare your terrain and",
			    "§7zombies have 1 minute to attempt to cross it!",
			    "§7",
			    "§7Zombies will start on the left side and move right.",
			    "§7",
			    "§bClick to start"
			));
		
		gui.addElement(new StaticGuiElement('d',
			    new ItemStack(Material.OBSIDIAN),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aVolcano Simulator",
			    "§7",
			    "§7Build a volcano and watch it erupt after 30 seconds.",
			    "§7",
			    "§bClick to start"
			));

		gui.addElement(new StaticGuiElement('e',
			    new ItemStack(Material.WATER_BUCKET),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aAquaduct",
			    "§7",
			    "§7Redirect water from the source to the end.",
			    "§7",
			    "§bClick to start"
			));
		gui.addElement(new StaticGuiElement('f',
			    new ItemStack(Material.TNT),
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aDig Roulette (easy)",
			    "§7",
			    "§7Dig up as much gold as possible without hitting TNT.",
			    "§7Gold blocks = 5 pts",
			    "§7TNT = game over",
			    "§7",
			    "§7TNT is less common in easy mode",
			    "§7",
			    "§bClick to start"
			));
		ItemStack tnt = new ItemStack(Material.TNT);
		tnt.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);;
		gui.addElement(new StaticGuiElement('g',
				tnt,
			    1,
			    click -> {
			    	
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing gamemode to zombie race..");
						player.sendMessage("Game will start in 5,4,3,2,1 <- change to title countdown");
					}
			    	return true;
			    },
			    "§aDig Roulette (hard)",
			    "§7",
			    "§7Dig up as much gold as possible without hitting TNT.",
			    "§7Gold blocks = 5 pts",
			    "§7TNT = game over",
			    "§7",
			    "§7TNT is more common in hard mode",
			    "§7",
			    "§bClick to start"
			));
		
		
		return gui;
	}
}
