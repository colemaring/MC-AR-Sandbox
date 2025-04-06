import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.md_5.bungee.api.ChatColor;

public class BiomeGui {
	private KinectSandbox plugin;
	public BiomeGui(KinectSandbox plugin)
	{
		this.plugin = plugin;
	}
	
	public InventoryGui createGui()
	{
		String[] guiSetup = {
				"         ",
	            " abcdefg ",
	            "    w    "
	        };
		InventoryGui gui = new InventoryGui(plugin, null, "Biome Menu & Water Toggle", guiSetup);
		gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
		GuiStateElement element = new GuiStateElement('w', 
		        new GuiStateElement.State(
		                change -> {
		                	for (Player player : Bukkit.getOnlinePlayers()) {
		            			player.sendMessage(ChatColor.GREEN + "Disabling water..");
		            		}
		                	plugin.waterEnabled = false;
		                },
		                "waterDisabled", // a key to identify this state by
		                new ItemStack(Material.BUCKET), // the item to display as an icon
		                ChatColor.GREEN + "Enable water!", // explanation text what this element does
		                "Water is currently disabled"
		        ),
		        new GuiStateElement.State(
		                change -> {
		                	for (Player player : Bukkit.getOnlinePlayers()) {
		            			player.sendMessage(ChatColor.GREEN + "Enabling water..");
		            		}
		                	plugin.waterEnabled = true;
		                },
		                "waterEnabled",
		                new ItemStack(Material.WATER_BUCKET),
		                ChatColor.RED + "Disable water!",
		                "Water is currently enabled"
		        )
		);
		 
//		# Set the current state
//		if (player.isFlying()) {
//		    element.setState("flyingEnabled");
//		} else {
//		    element.setState("flyingDisabled");
//		}
		 
		gui.addElement(element);
		gui.addElement(new StaticGuiElement('a',
			    new ItemStack(Material.GRASS_BLOCK),
			    1,
			    click -> {
			    	KinectSandbox.biome = "grass";
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing biome to grass..");
					}
			    	return true;
			    },
			    "§aGrassy Plains Biome",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('b',
			    new ItemStack(Material.SAND),
			    1,
			    click -> {
			    	KinectSandbox.biome = "sand";
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing biome to sand..");
					}
			    	return true;
			    },
			    "§aDesert Biome",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('c',
			    new ItemStack(Material.SNOW_BLOCK),
			    1,
			    click -> {
			    	KinectSandbox.biome = "snow";
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing biome to snow..");
					}
			    	return true;
			    },
			    "§aSnowy Biome",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('d',
			    new ItemStack(Material.RED_SAND),
			    1,
			    click -> {
			    	KinectSandbox.biome = "mesa";
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing biome to badlands..");
					}
			    	return true;
			    },
			    "§aBadlands Biome",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('e',
			    new ItemStack(Material.COAL_ORE),
			    1,
			    click -> {
			    	KinectSandbox.biome = "stone";
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing biome to stony peaks..");
					}
			    	return true;
			    },
			    "§aStony Peaks Biome",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('f',
			    new ItemStack(Material.NETHERRACK),
			    1,
			    click -> {
			    	KinectSandbox.biome = "nether";
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing biome to nether..");
					}
			    	return true;
			    },
			    "§aNether Biome",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('g',
			    new ItemStack(Material.DIAMOND_BLOCK),
			    1,
			    click -> {
			    	KinectSandbox.biome = "placeholder";
			    	for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendMessage(ChatColor.GREEN + "Changing biome to placeholder..");
					}
			    	return true;
			    },
			    "§placeholder Biome",
			    "§7Click to change."
			));
		
		return gui;
	}
}
