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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpigotPluginBlank extends JavaPlugin implements Listener {

    private static final String FILE_PATH = "C:\\Users\\colem\\OneDrive\\Desktop\\mc_test\\output.txt";
    private static int[][] array2D;
    private static String biome = "mountains";

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
    public void onWaterFlow(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.WATER) {
            event.setCancelled(true);
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
        
        player.sendMessage(ChatColor.WHITE + "Type /help for a list of available commands.");
        
        if (player.isOp()) {
            ItemStack stick = new ItemStack(Material.STICK); // update terrain wand
            ItemStack grass = new ItemStack(Material.GRASS_BLOCK); // normal mountain biome
            ItemStack snow = new ItemStack(Material.SNOW_BLOCK); // snowy mountain biome
            ItemStack mesa = new ItemStack(Material.RED_SAND); // mesa biome
            ItemStack ice = new ItemStack(Material.BLUE_ICE); // frozen ocean biome
            stick.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            grass.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            snow.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            mesa.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            ice.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            ItemMeta stickmeta = stick.getItemMeta();
            ItemMeta grassmeta = stick.getItemMeta();
            ItemMeta snowmeta = stick.getItemMeta();
            ItemMeta mesameta = stick.getItemMeta();
            ItemMeta icemeta = stick.getItemMeta();
            stickmeta.setDisplayName(ChatColor.GOLD + "Update Terrain");
            grassmeta.setDisplayName(ChatColor.GOLD + "Mountain Biome");
            snowmeta.setDisplayName(ChatColor.GOLD + "Snowy Biome");
            mesameta.setDisplayName(ChatColor.GOLD + "Mesa Biome");
            icemeta.setDisplayName(ChatColor.GOLD + "Icy Biome");
            stick.setItemMeta(stickmeta);
            grass.setItemMeta(grassmeta);
            snow.setItemMeta(snowmeta);
            mesa.setItemMeta(mesameta);
            ice.setItemMeta(icemeta);
            player.getInventory().addItem(stick);
            player.getInventory().addItem(grass);
            player.getInventory().addItem(snow);
            player.getInventory().addItem(mesa);
            player.getInventory().addItem(ice);
        }
    }
    
    public void readFile()
    {
    	 Gson gson = new Gson();
         try {
             String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
             array2D = gson.fromJson(content, int[][].class);
             Bukkit.getScheduler().runTask(this, this::setBlocks);
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.STICK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Update Terrain") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            readFile();
//            biome = "mountains";
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.GRASS_BLOCK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mountain Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "mountains";
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.SNOW_BLOCK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Snowy Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "snowy";
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.RED_SAND && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mesa Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "mesa";
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.BLUE_ICE && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Icy Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "icy";
            setBlocks();
        }
        
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args[0].equalsIgnoreCase("/help")) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            player.sendMessage(ChatColor.GOLD + "Help Menu:");
            player.sendMessage(ChatColor.WHITE + "Left click with the stick to update the terrain to match the sandbox.");
            player.sendMessage(ChatColor.WHITE + "Left click with a block to change the biome.");
            player.sendMessage(ChatColor.WHITE + "Use /waterlevel followed by a number to change the water level. eg. /waterlevel 10 sets the waterlevel at y pos 10.");
        }
        
//        if (args[0].equalsIgnoreCase("/waterlevel")) {
//            event.setCancelled(true);
//
//            Player player = event.getPlayer();
//            player.sendMessage(ChatColor.GOLD + "Help Menu:");
//            player.sendMessage(ChatColor.WHITE + "Left click with the stick to update the terrain to match the sandbox.");
//            player.sendMessage(ChatColor.WHITE + "Left click with a block to change the biome.");
//            player.sendMessage(ChatColor.WHITE + "Use /waterlevel followed by a number to change the water level. eg. /waterlevel 10 sets the waterlevel at y pos 10.");
//        }
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
        world.setTime(6000); 
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);  // not working?
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

 void setBlocks() {
        if (array2D == null) return;
        
        resetBlocks();
        
//        getLogger().info("Setting blocks");

        for (int x = 0; x < array2D.length; x++) {
            for (int z = 0; z < array2D[x].length; z++) {
            	Location location = new Location(Bukkit.getWorlds().get(0), x, 0, z);
                location.getBlock().setType(Material.BEDROCK);
            	for (int i = 0; i < Math.abs((int)(array2D[x][z] * 0.05-82)); i++)
            	{
//            		System.out.println(i);
            		Location location1 = new Location(Bukkit.getWorlds().get(0), x, i, z);
            		if (biome.equals("mountains"))
            		{
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
            		else if (biome.equals("snowy"))
            		{
            			if (i > 20)
                        {
                        	location1.getBlock().setType(Material.SNOW_BLOCK);
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
            		else if (biome.equals("mesa"))
            		{
            			if (i > 20)
                        {
                        	location1.getBlock().setType(Material.RED_SAND);
                        }
                        else if (i >10)
                        {
                        	location1.getBlock().setType(Material.SANDSTONE);
                        }
                        else 
                        {
                        	location1.getBlock().setType(Material.ANDESITE);
                        }
            		}
            		else if (biome.equals("icy"))
            		{
            			if (i > 20)
                        {
                        	location1.getBlock().setType(Material.ICE);
                        }
                        else if (i >10)
                        {
                        	location1.getBlock().setType(Material.BLUE_ICE);
                        }
                        else 
                        {
                        	location1.getBlock().setType(Material.ANDESITE);
                        }
            		}
                    
            	}
            }
        }
        
        setWater(10);
    }
}