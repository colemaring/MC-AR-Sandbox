package Guis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Main.KinectSandbox;
import de.themoep.inventorygui.InventoryGui;

public class InventoryHelper implements Listener{
	private KinectSandbox plugin;
	private InventoryGui biomeGui;
	private InventoryGui gameGui;
	public InventoryHelper(KinectSandbox plugin)
	{
		this.plugin = plugin;
		giveInventory();
	}
	
	 @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Game Selector Menu") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
        {
        	gameGui.show(player);
       		event.setCancelled(true); // prevent breaking a block
        }
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Biome Selector Menu") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
        {
        	biomeGui.show(player);
        	event.setCancelled(true);
        }
        	
	}
	 
	 @EventHandler
	 // Give op players gui menu items
	 public void onPlayerJoin(PlayerJoinEvent event)
	 {
		 giveInventory();
	 }
    
    public void giveInventory()
    {
    	// Clear player's inventory
    	for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
        }
    	
    	BiomeGui bg = new BiomeGui(plugin);
    	GamemodeGui gg = new GamemodeGui(plugin);
    	biomeGui = bg.createGui();
    	gameGui = gg.createGui();
        for (Player player : Bukkit.getOnlinePlayers())
        {
        	if (player.isOp())
        	{
        		// give them compass and north star thing
                ItemStack nether_star = new ItemStack(Material.NETHER_STAR);
                ItemStack compass = new ItemStack(Material.COMPASS);
                nether_star.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1); 
                compass.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                ItemMeta nether_star_meta = nether_star.getItemMeta();
                ItemMeta compass_meta = compass.getItemMeta();
                nether_star_meta.setDisplayName(ChatColor.GOLD + "Game Selector Menu");
                compass_meta.setDisplayName(ChatColor.GOLD + "Biome Selector Menu");
                nether_star.setItemMeta(nether_star_meta);
                compass.setItemMeta(compass_meta);
                player.getInventory().addItem(compass);
                player.getInventory().addItem(nether_star);
        	}
        }	
    }
}
