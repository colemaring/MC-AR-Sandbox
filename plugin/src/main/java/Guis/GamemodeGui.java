package Guis;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Gamemodes.Aquaduct;
import Gamemodes.DigRouletteEasy;
import Gamemodes.GamemodeHelper;
import Gamemodes.OreHunt;
import Gamemodes.OreHunt2p;
import Gamemodes.ZombieRush;
import Main.KinectSandbox;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;

public class GamemodeGui {
	
	public InventoryGui createGui()
	{
		String[] guiSetup = {
				"         ",
	            " abcdefg ",
	            "    z    "
	        };
		InventoryGui gui = new InventoryGui(KinectSandbox.getInstance(), null, "Gamemode Menu", guiSetup);
		
		gui.addElement(new StaticGuiElement('z',
			    new ItemStack(Material.REDSTONE_BLOCK),
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			        GamemodeHelper.stopCurrentGamemodeIfRunning();
			    	return true;
			    },
			    "§aEnd current gamemode"
			));

		ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
		ItemMeta meta = diamondPickaxe.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		diamondPickaxe.setItemMeta(meta);
		gui.addElement(new StaticGuiElement('a',
			    new ItemStack(Material.ZOMBIE_HEAD),
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	ZombieRush.prepareTerrain();
			    	return true;
			    },
			    "§aZombie Rush",
			    "§7",
			    "§7Stop an army of zombies from traversing your terrain.",
			    "§7Zombies have 1 minute to attempt to cross your terrain!",
			    "§7",
			    "§7Zombies will start on the left side and move right.",
			    "§7",
			    "§bClick to start"
			));
		gui.addElement(new StaticGuiElement('b',
			    diamondPickaxe,
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	OreHunt.initOreHunt();
			        return true;
			    },
			    "§aOre Hunt",
			    "§7",
			    "§7Find as many buried ores as possible in 2 minutes.",
			    "§7Coal = 5 pts",
			    "§7Iron = 10 pts",
			    "§7Diamond = 15 pts",
			    "§7Emerald = 15 pts",
			    "§7",
			    "§7The ore needs to be completely uncovered for it to count.",
			    "§7",
			    "§bClick to start"
			));

		
		ItemStack goldenPickaxe = new ItemStack(Material.GOLDEN_PICKAXE);
		meta = goldenPickaxe.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		goldenPickaxe.setItemMeta(meta);
		
		gui.addElement(new StaticGuiElement('c',
				goldenPickaxe,
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	OreHunt2p.initOreHunt();
			    	return true;
			    },
			    "§aOre Hunt (2 Player)",
			    "§7",
			    "§7Compete to find as many buried ores as possible in 1 minute.",
			    "§7Coal = 5 pts",
			    "§7Iron = 10 pts",
			    "§7Diamond = 15 pts",
			    "§7Emerald = 15 pts",
			    "§7",
			    "§7The ore needs to be completely uncovered for it to count.",
			    "§7",
			    "§bClick to start"
			));
		
		gui.addElement(new StaticGuiElement('d',
			    new ItemStack(Material.TNT),
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	DigRouletteEasy.initDigRoulette();
			    	return true;
			    },
			    "§aDig Roulette",
			    "§7",
			    "§7Dig up as much gold as possible without hitting TNT.",
			    "§7TNT = game over",
			    "§7",
			    "§bClick to start"
			));
		ItemStack tnt = new ItemStack(Material.TNT);
		tnt.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);;

		gui.addElement(new StaticGuiElement('e',
			    new ItemStack(Material.WATER_BUCKET),
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	Aquaduct.startCountdown();
			    	return true;
			    },
			    "§aAquaduct",
			    "§7",
			    "§7Redirect water from the source to the sink.",
			    "§7",
			    "§7The water source will spawn at the highest elevation point,",
			    "§7and the sink (gold block) will spawn randomly above surface.",
			    "§7",
			    "§bClick to start"
			));
		gui.addElement(new StaticGuiElement('f',
			    new ItemStack(Material.BARRIER),
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	return true;
			    },
			    "§aTo be developed.."
			));
		
		gui.addElement(new StaticGuiElement('g',
			    new ItemStack(Material.BARRIER),
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	return true;
			    },
			    "§aTo be developed.."
			));

		
		
		return gui;
	}
}
