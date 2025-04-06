import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.themoep.inventorygui.InventoryGui;

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
	            "abcdefghi",
	            "         "
	        };
		InventoryGui gui = new InventoryGui(plugin, null, "Biome Menu", guiSetup);
		gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS, 1));
		
		
		return gui;
	}
}
