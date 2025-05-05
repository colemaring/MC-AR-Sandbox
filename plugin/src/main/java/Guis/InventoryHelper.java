package Guis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;
import Terrain.TerrainGeneratorHelper;
import de.themoep.inventorygui.InventoryGui;

public class InventoryHelper implements Listener{
	private InventoryGui biomeGui;
	private InventoryGui gameGui;
	public InventoryHelper()
	{
		giveInventory();
	}
	
	 @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Game Menu") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
        {
        	
        	gameGui.show(player);
       		event.setCancelled(true); // prevent breaking a block
        }
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Biome Menu") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
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
	 
	 @EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Bukkit.broadcastMessage("Someone died");
		Player player = event.getPlayer();
	    World world = player.getWorld();

	    int middleX = TerrainGeneratorHelper.findXEnd() / 2;
	    int middleZ =TerrainGeneratorHelper.findZEnd() / 2;
	    int middleY = 81;

	    Location target = new Location(world, middleX + 0.5, middleY, middleZ + 0.5);
	    target.setPitch(90);
	    target.setYaw(90);

	    new BukkitRunnable() {
	        @Override
	        public void run() {
	            player.teleport(target);
	            player.setAllowFlight(true);
	            player.setFlying(true);
	            giveInventory();
	        }
	    }.runTaskLater(KinectSandbox.getInstance(), 2L); // <- make sure to replace `yourPluginInstance`
	}
    
    public void giveInventory()
    {
    	// Clear player's inventory
    	for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
        }
    	
    	BiomeGui bg = new BiomeGui();
    	GamemodeGui gg = new GamemodeGui();
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
                nether_star_meta.setDisplayName(ChatColor.GOLD + "Game Menu");
                compass_meta.setDisplayName(ChatColor.GOLD + "Biome Menu");
                nether_star.setItemMeta(nether_star_meta);
                compass.setItemMeta(compass_meta);
                player.getInventory().addItem(compass);
                player.getInventory().addItem(nether_star);
        	}
        }	
    }
}
