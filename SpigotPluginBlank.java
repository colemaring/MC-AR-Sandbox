package com.cole.blankplugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpigotPluginBlank extends JavaPlugin implements Listener {

    private static final String FILE_PATH = "C:\\Users\\colem\\OneDrive\\Desktop\\mc_test\\output.txt";
    private static int[][] array2D;

    @Override
    public void onEnable() {
    	getServer().getPluginManager().registerEvents(this, this);

    }
    
    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	System.out.println("PlayerJoinEvent triggered");
        if (event == null) {
            System.out.println("Event is null");
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            System.out.println("Player is null");
            return;
        }
        
        if (player.isOp()) {
        	getLogger().info("op player joined");
            ItemStack stick = new ItemStack(Material.STICK);
            stick.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            ItemMeta meta = stick.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Update Terrain"); // Set the name
            stick.setItemMeta(meta);
            player.getInventory().addItem(stick);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.STICK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Update Terrain") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            System.out.println("Update Terrain stick left-clicked!");
            Gson gson = new Gson();
            try {
                String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
                array2D = gson.fromJson(content, int[][].class);
                Bukkit.getScheduler().runTask(this, this::setBlocks);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setBlocks();
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        world.setTime(6000); // Sets the time to 6:00 AM (permanent day)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false); // Disables the daylight cycle
    }
    
    private void resetBlocks() {
        World world = Bukkit.getWorlds().get(0);
        for (int x = 0; x < world.getMaxHeight(); x++) {
            for (int y = 0; y < world.getMaxHeight(); y++) {
                for (int z = 0; z < 120; z++) {
                    Location location = new Location(world, x, y, z);
                    location.getBlock().setType(Material.AIR);
                }
            }
        }
        // getLogger().info("All blocks have been reset to air");
    }
    
    private void setWater(int waterlevel)
    {
    	for (int x = 0; x < array2D.length; x++) {
            for (int z = 0; z < array2D[x].length; z++) {
            	for(int y = 0; y < waterlevel; y++)
            	{
            		Location location2 = new Location(Bukkit.getWorlds().get(0), x, y, z);
            		if ( location2.getBlock().getBlockData().getMaterial().equals(Material.AIR))
            		{
            			location2.getBlock().setType(Material.WATER);
            		}
            	}
            }
        }
    }
    
    

    private void setBlocks() {
        if (array2D == null) return;
        
        resetBlocks();
        
        getLogger().info("Setting blocks");

        for (int x = 0; x < array2D.length; x++) {
            for (int z = 0; z < array2D[x].length; z++) {
            	Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);
                location.getBlock().setType(Material.BEDROCK);
            	for (int i = 0; i < (int)(array2D[x][z] * 0.05 - 50); i++)
            	{
            		Location location1 = new Location(Bukkit.getWorlds().get(0), x, i, z);
                    if (i > 20)
                    {
                    	location1.getBlock().setType(Material.DIRT);
                    }
                    else if (i >10)
                    {
                    	location1.getBlock().setType(Material.STONE);
                    }
                    else 
                    {
                    	location1.getBlock().setType(Material.ANDESITE);
                    }
            	}
//                int height = (int)(array2D[x][z] * 0.05 - 50);
                
//                getLogger().info("set block at " + x + ", " + height + ", " + z + " to stone");
            }
        }
        
        setWater(10);
    }
}