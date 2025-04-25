package Guis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import Gamemodes.GamemodeHelper;
import Main.KinectSandbox;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.md_5.bungee.api.ChatColor;

public class BiomeGui {
	public static GuiStateElement waterElement;
	public InventoryGui createGui()
	{
		String[] guiSetup = {
				"         ",
	            " abcdefg ",
	            "    w    "
	        };
		InventoryGui gui = new InventoryGui(KinectSandbox.getInstance(), null, "Biome Menu & Water Toggle", guiSetup);
		//gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
		// Assuming 'gui' is already defined somewhere above

		// Create the GuiStateElement instance
		  waterElement = new GuiStateElement(
		    'w', // Slot char or identifier
		    false, // Maybe 'locked'? Needs context from GuiStateElement docs
		    // Set the INITIAL state key based on the current waterEnabled value
		    KinectSandbox.getInstance().waterEnabled ? "waterEnabled" : "waterDisabled",
		    

		    // --- STATE: waterDisabled ---
		    // This state represents the condition when water IS currently disabled.
		    // Clicking it should attempt to ENABLE water.
		    new GuiStateElement.State(
		    		
		        change -> { // Action executed when clicked in this state
		            Player player = (Player) change.getWhoClicked();
		            player.closeInventory(); // Close inventory first

		            // --- Pre-conditions ---
		            if (GamemodeHelper.gamemodeRunning) {
		                Bukkit.broadcastMessage(ChatColor.RED + "Can't enable water while gamemode in progress.");
		                return; // Stop execution
		            }
		            if (KinectSandbox.biome.equals("sand") || KinectSandbox.biome.equals("mesa") || KinectSandbox.biome.equals("rainbow")) {
		                Bukkit.broadcastMessage(ChatColor.RED + "This biome doesn't allow water."); // Use RED for errors/restrictions
		                // Ensure the state remains consistent if enabling fails
		                if (KinectSandbox.getInstance().waterEnabled) { // Check if it somehow got enabled elsewhere
		                     KinectSandbox.getInstance().waterEnabled = false;
		                     // No need to set element state here, as we are aborting the switch
		                }
		                // No need to redraw if nothing changed visually
		                return; // Stop execution
		            }

		            // --- Action ---
		            // Send message before changing state
		            for (Player p : Bukkit.getOnlinePlayers()) {
		                p.sendMessage(ChatColor.GREEN + "Enabling water...");
		            }
		            // Update the underlying boolean value
		            KinectSandbox.getInstance().waterEnabled = true;

		            // --- Update GUI State ---
		            // *** KEY CHANGE: Tell the element to switch to the 'waterEnabled' state ***
		            // We assume 'change.getElement()' exists or the 'element' variable is accessible.
		            // If using lambda, 'element' should be effectively final or captured.
		            ((GuiStateElement) change.getElement()).setState("waterEnabled");


		            // Redraw the GUI to reflect the changes
		            change.getGui().draw();
		        },
		        "waterDisabled", // Key identifying this state
		        new ItemStack(Material.BUCKET), // Icon: Empty bucket (represents "can enable")
		        ChatColor.GREEN + "Enable Water", // Title: Action to take
		        "Water is currently disabled.", // Lore/Description
		        "Click to enable water flow." // Additional lore line
		    ),

		    // --- STATE: waterEnabled ---
		    // This state represents the condition when water IS currently enabled.
		    // Clicking it should attempt to DISABLE water.
		    new GuiStateElement.State(
		        change -> { // Action executed when clicked in this state
		            Player player = (Player) change.getWhoClicked();
		            player.closeInventory(); // Close inventory first

		            // --- Pre-conditions ---
		             if (GamemodeHelper.gamemodeRunning) {
		                 Bukkit.broadcastMessage(ChatColor.RED + "Can't disable water while gamemode in progress.");
		                 return; // Stop execution
		             }

		            // --- Action ---
		            Bukkit.broadcastMessage(ChatColor.YELLOW + "Disabling water..."); // Use Yellow or Red for disabling actions
		            // Update the underlying boolean value
		            KinectSandbox.getInstance().waterEnabled = false;

		            // --- Update GUI State ---
		            // *** KEY CHANGE: Tell the element to switch to the 'waterDisabled' state ***
		            ((GuiStateElement) change.getElement()).setState("waterDisabled");


		            // Redraw the GUI to reflect the changes
		            change.getGui().draw();
		        },
		        "waterEnabled", // Key identifying this state
		        new ItemStack(Material.WATER_BUCKET), // Icon: Water bucket (represents "can disable")
		        ChatColor.RED + "Disable Water", // Title: Action to take
		        "Water is currently enabled.", // Lore/Description
		        "Click to disable water flow." // Additional lore line
		    )
		);

		// Add the element to the GUI
		gui.addElement(waterElement);

		// --- Remove Redundant SetState ---
		// The constructor already sets the initial state based on the boolean.
		// This block is likely unnecessary and potentially confusing.
		/*
		if (KinectSandbox.getInstance().waterEnabled) {
		    element.setState("waterEnabled");
		} else {
		    element.setState("waterDisabled");
		}
		// If the GUI needs an initial draw after adding elements, do it here:
		// gui.draw(); // Or however the initial drawing is triggered for the whole GUI
		*/
		gui.addElement(new StaticGuiElement('a',
			    new ItemStack(Material.GRASS_BLOCK),
			    1,
			    click -> {
			    	Player player = (Player) click.getWhoClicked();
			        player.closeInventory();
			        if (GamemodeHelper.gamemodeRunning)
			        {
			        	Bukkit.broadcastMessage(ChatColor.RED + "Cant change biome while gamemode in progress.");
			        	return true;
			        }
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
			        if (GamemodeHelper.gamemodeRunning)
			        {
			        	Bukkit.broadcastMessage(ChatColor.RED + "Cant change biome while gamemode in progress.");
			        	return true;
			        }
			    	KinectSandbox.biome = "sand";
			    	if (KinectSandbox.getInstance().waterEnabled)
			    	{
			    		Bukkit.broadcastMessage(ChatColor.RED + "Disabling water for this biome.");
			    		KinectSandbox.getInstance().waterEnabled = false;
				    	waterElement.setState("waterDisabled");
			    	}
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
			        if (GamemodeHelper.gamemodeRunning)
			        {
			        	Bukkit.broadcastMessage(ChatColor.RED + "Cant change biome while gamemode in progress.");
			        	return true;
			        }
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
			        if (GamemodeHelper.gamemodeRunning)
			        {
			        	Bukkit.broadcastMessage(ChatColor.RED + "Cant change biome while gamemode in progress.");
			        	return true;
			        }
			    	KinectSandbox.biome = "mesa";
			    	if (KinectSandbox.getInstance().waterEnabled)
			    	{
			    		Bukkit.broadcastMessage(ChatColor.RED + "Disabling water for this biome.");
			    		KinectSandbox.getInstance().waterEnabled = false;
				    	waterElement.setState("waterDisabled");
			    	}
			    	
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
			        if (GamemodeHelper.gamemodeRunning)
			        {
			        	Bukkit.broadcastMessage(ChatColor.RED + "Cant change biome while gamemode in progress.");
			        	return true;
			        }
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
			        if (GamemodeHelper.gamemodeRunning)
			        {
			        	Bukkit.broadcastMessage(ChatColor.RED + "Cant change biome while gamemode in progress.");
			        	return true;
			        }
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
		    	//Bukkit.getWorld("world").setTime(1000L);
		        KinectSandbox.biome = "rainbow";
		        if (KinectSandbox.getInstance().waterEnabled)
		    	{
		    		Bukkit.broadcastMessage(ChatColor.RED + "Disabling water for this biome.");
		    		KinectSandbox.getInstance().waterEnabled = false;
			    	waterElement.setState("waterDisabled");
		    	}
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
