package Guis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.md_5.bungee.api.ChatColor;

public class BiomeGui {

	public InventoryGui createGui()
	{
		String[] guiSetup = {
				"         ",
	            " abcdefg ",
	            "    w    "
	        };
		InventoryGui gui = new InventoryGui(KinectSandbox.getInstance(), null, "Biome Menu & Water Toggle", guiSetup);
		//gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
		GuiStateElement element = new GuiStateElement('w', 
		        new GuiStateElement.State(
		                change -> {
		                	Player player = (Player) change.getWhoClicked();
					        player.closeInventory();
		                	for (Player p : Bukkit.getOnlinePlayers()) {
		            			p.sendMessage(ChatColor.GREEN + "Disabling water..");
		            		}
		                	KinectSandbox.getInstance().waterEnabled = false;
		                },
		                "waterDisabled", // a key to identify this state by
		                new ItemStack(Material.BUCKET), // the item to display as an icon
		                ChatColor.GREEN + "Enable water!", // explanation text what this element does
		                "Water is currently disabled"
		        ),
		        new GuiStateElement.State(
		                change -> {
		            	    if (KinectSandbox.biome.equals("sand") || KinectSandbox.biome.equals("mesa") || KinectSandbox.biome.equals("rainbow"))
		            	    {
		            	    	for (Player player : Bukkit.getOnlinePlayers())
		            	    		player.sendMessage(ChatColor.GREEN + "Water enabled, but this biome doesn't have water.");
		            	    	KinectSandbox.getInstance().waterEnabled = true;
		            	    	return;
		            	    }
		                	Player player = (Player) change.getWhoClicked();
					        player.closeInventory();
		                	for (Player p : Bukkit.getOnlinePlayers()) {
		            			p.sendMessage(ChatColor.GREEN + "Enabling water..");
		            		}
		                	KinectSandbox.getInstance().waterEnabled = true;
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
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	KinectSandbox.biome = "grass";
			    	for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(ChatColor.GREEN + "Changing biome to grass..");
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
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	KinectSandbox.biome = "sand";
			    	for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(ChatColor.GREEN + "Changing biome to sand..");
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
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	KinectSandbox.biome = "snow";
			    	for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(ChatColor.GREEN + "Changing biome to snow..");
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
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	KinectSandbox.biome = "mesa";
			    	for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(ChatColor.GREEN + "Changing biome to badlands..");
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
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	KinectSandbox.biome = "stone";
			    	for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(ChatColor.GREEN + "Changing biome to stony peaks..");
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
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			    	KinectSandbox.biome = "nether";
			    	for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(ChatColor.GREEN + "Changing biome to nether..");
					}
			    	return true;
			    },
			    "§aNether Biome",
			    "§7Click to change."
			));

		// Cycle "rainbow" blocks every 0.3 seconds
		Material[] oreBlocks = {Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.EMERALD_BLOCK, Material.LAPIS_BLOCK};
		final int[] currentOreIndex = {0}; // Use an array to allow modification within lambda

		new BukkitRunnable() {
		    @Override
		    public void run() {
		        currentOreIndex[0] = (currentOreIndex[0] + 1) % oreBlocks.length;
		        gui.draw();
		    }
		}.runTaskTimer(KinectSandbox.getInstance(), 0L, 6L); // Run every 6 ticks (0.3 seconds)

		gui.addElement(new DynamicGuiElement('g', (viewer) -> {
		    ItemStack oreItem = new ItemStack(oreBlocks[currentOreIndex[0]]);

		    return new StaticGuiElement('g', oreItem, 1, click -> {
		    	Player player = (Player) click.getWhoClicked();
		        player.closeInventory();
		    	Bukkit.getWorld("world").setTime(1000L);
		        KinectSandbox.biome = "rainbow";
		        for (Player p : Bukkit.getOnlinePlayers()) {
		            p.sendMessage(ChatColor.GREEN + "Changing biome to placeholder..");
		        }
		        return true;
		    }, 
		    	"§aRainbow Biome",
		       "§7Click to change.");
		}));


		
		return gui;
	}
}
