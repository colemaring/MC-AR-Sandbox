import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	            "abcdefghi",
	            "         "
	        };
		InventoryGui gui = new InventoryGui(plugin, null, "Biome Menu", guiSetup);
		gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
		
		gui.addElement(new StaticGuiElement('a',
			    new ItemStack(Material.GRASS_BLOCK),
			    1,
			    click -> {
			        // set biome to normal
			    	return true;
			    },
			    "§aBiome name here",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('b',
			    new ItemStack(Material.SAND),
			    1,
			    click -> {
			        // set biome to normal
			    	return true;
			    },
			    "§aBiome name here",
			    "§7Click to change."
			));
		gui.addElement(new StaticGuiElement('c',
			    new ItemStack(Material.SNOW_BLOCK),
			    1,
			    click -> {
			        // set biome to normal
			    	return true;
			    },
			    "§aBiome name here",
			    "§7Click to change."
			));

		
		return gui;
	}
}
