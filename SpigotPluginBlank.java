package com.cole.blankplugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.io.File;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpigotPluginBlank extends JavaPlugin implements Listener {

    private static final String FILE_PATH = "C:\\Users\\colem\\OneDrive\\Desktop\\mc_test\\output.txt";
    private static int[][] array2D;
    private static String biome = "mountains";
    private static int waterlevel = -1;
    private static boolean autoupdate = false;
    private static int timer = 10;
    private static double heightMultipler = 1;
    private static World world;

    @Override
    public void onEnable() {
    	getServer().getPluginManager().registerEvents(this, this);
    	getCommand("waterlevel").setExecutor(this);
    	getCommand("autoupdate").setExecutor(this);
    	getCommand("timer").setExecutor(this);
    	getCommand("default").setExecutor(this);
    	world = Bukkit.getWorlds().get(0);
    	loadVariables();
    	readFileAndSetBlocks();
    	//setBlocks();
    	
    }
    
    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
        }
        saveVariables();
    }
    
    @EventHandler
    public void onWaterFlow(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.WATER) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event == null) {
            System.out.println("Event is null");
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            System.out.println("Player is null");
            return;
        }
        
        event.setJoinMessage("");
        
        player.sendMessage(ChatColor.WHITE + "Current waterlevel = " + waterlevel);
        player.sendMessage(ChatColor.WHITE + "Auto update is set to " + autoupdate);
        player.sendMessage(ChatColor.WHITE + "Timer set to " + timer);
        player.sendMessage(ChatColor.GOLD + "Type /help for a list of available commands.");
        
        if (player.isOp()) {
            ItemStack BLAZE_ROD = new ItemStack(Material.BLAZE_ROD); // update terrain wand
            ItemStack grass = new ItemStack(Material.GRASS_BLOCK); // normal mountain biome
            ItemStack snow = new ItemStack(Material.SNOW_BLOCK); // snowy mountain biome
            ItemStack mesa = new ItemStack(Material.RED_SAND); // mesa biome
            ItemStack ice = new ItemStack(Material.BLUE_ICE); // frozen ocean biome
            ItemStack sand = new ItemStack(Material.SAND); // desert biome
            BLAZE_ROD.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            grass.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            snow.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            mesa.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            ice.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            sand.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            ItemMeta BLAZE_RODmeta = BLAZE_ROD.getItemMeta();
            ItemMeta grassmeta = grass.getItemMeta();
            ItemMeta snowmeta = snow.getItemMeta();
            ItemMeta mesameta = mesa.getItemMeta();
            ItemMeta icemeta = ice.getItemMeta();
            ItemMeta sandmeta = sand.getItemMeta();
            BLAZE_RODmeta.setDisplayName(ChatColor.GOLD + "Update Terrain");
            grassmeta.setDisplayName(ChatColor.GOLD + "Mountain Biome");
            snowmeta.setDisplayName(ChatColor.GOLD + "Snowy Biome");
            mesameta.setDisplayName(ChatColor.GOLD + "Mesa Biome");
            icemeta.setDisplayName(ChatColor.GOLD + "Icy Biome");
            sandmeta.setDisplayName(ChatColor.GOLD + "Desert Biome");
            BLAZE_ROD.setItemMeta(BLAZE_RODmeta);
            grass.setItemMeta(grassmeta);
            snow.setItemMeta(snowmeta);
            mesa.setItemMeta(mesameta);
            ice.setItemMeta(icemeta);
            sand.setItemMeta(sandmeta);
            player.getInventory().addItem(BLAZE_ROD);
            player.getInventory().addItem(grass);
            player.getInventory().addItem(snow);
            player.getInventory().addItem(mesa);
            player.getInventory().addItem(ice);
            player.getInventory().addItem(sand);
        }
    }
    
    public void readFileAndSetBlocks() {
        Gson gson = new Gson();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
                array2D = gson.fromJson(content, int[][].class);
            } catch (IOException e) {
                e.printStackTrace();
            }
           Bukkit.getScheduler().runTask(this, this::setBlocks);
        });
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("waterlevel")) {
            if (args.length != 1) {
                sender.sendMessage("Usage: /waterlevel <level>");
                return false;
            }
            int level = Integer.parseInt(args[0]);
            waterlevel = level;
            resetWater();
            setWater(waterlevel);
            sender.sendMessage("Changed water level to " + waterlevel);
            
            // Use the level variable as needed
            return true;
        }
        
        else if (command.getName().equalsIgnoreCase("autoupdate"))
        {
        	if (args.length != 1) {
                sender.sendMessage("Usage: /autoupdate on or off");
                return false;
            }
        	String state = args[0];
        	if (state.equals("on"))
        	{
        		sender.sendMessage("Enabled auto terrain updating");
        		autoupdate = true;
        		Bukkit.getScheduler().cancelTasks(this);
                
                Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    public void run() {
                        readFileAndSetBlocks();
//                        setBlocks();
                    }
                }, 0, timer * 20); // Convert seconds to ticks
        	}
        	else	
        	{
        		sender.sendMessage("Disabled auto terrain updating");
        		autoupdate = false;
        		Bukkit.getScheduler().cancelTasks(this);
        	}
            
            
            // Use the level variable as needed
            return true;
        }
        
        else if (command.getName().equalsIgnoreCase("timer"))
        {
            if (args.length != 1) {
                sender.sendMessage("Usage: /timer <seconds>");
                return false;
            }
            timer = Integer.parseInt(args[0]);
            
            // Cancel the existing task if autoupdate is true
            if (autoupdate) {
                Bukkit.getScheduler().cancelTasks(this);
                Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    public void run() {
                    	readFileAndSetBlocks();
                        //setBlocks();
                    }
                }, 0, timer * 20); // Convert seconds to ticks
            }
            
            sender.sendMessage("Changed timer to " + timer + " seconds");
            
            return true;
        }
        else if (command.getName().equalsIgnoreCase("default"))
        {
        	waterlevel = 10;
        	autoupdate = false;
        	timer = 10;
        	biome = "mountains";
        	
//        	setBlocks();
        	
            sender.sendMessage("Reset settings to default values");
            
            // Use the level variable as needed
            return true;
        }
        
        return false;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.BLAZE_ROD && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Update Terrain") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            
//            biome = "mountains";
            player.sendMessage(ChatColor.GREEN + "Updating terrain..");
            //setBlocks();
            readFileAndSetBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.GRASS_BLOCK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mountain Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "mountains";
            player.sendMessage(ChatColor.GREEN + "Updating biome to normal mountains..");
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.SNOW_BLOCK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Snowy Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "snowy";
            player.sendMessage(ChatColor.GREEN + "Updating biome to snowy mountains..");
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.RED_SAND && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mesa Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "mesa";
            player.sendMessage(ChatColor.GREEN + "Updating biome to mesa..");
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.BLUE_ICE && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Icy Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "icy";
            player.sendMessage(ChatColor.GREEN + "Updating biome to frozen ocean..");
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.SAND && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Desert Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "desert";
            player.sendMessage(ChatColor.GREEN + "Updating biome to desert..");
            setBlocks();
        }
        
    }
    
    @SuppressWarnings("deprecation") // not an important feature anyways
	@EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args[0].equalsIgnoreCase("/help")) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            player.sendMessage(ChatColor.GOLD + "Help Menu:");
            player.sendMessage(ChatColor.WHITE + "Left click with the gold stick to manually update the terrain");
            player.sendMessage(ChatColor.WHITE + "Left click with a block to change the biome");
            player.sendMessage(ChatColor.WHITE + "/waterlevel <y level> - sets the water level");
            player.sendMessage(ChatColor.WHITE + "/autoupdate <on or off> - toggle the auto update feature");
            player.sendMessage(ChatColor.WHITE + "/timer <seconds> - set a timer in seconds for autoupdate");
            player.sendMessage(ChatColor.WHITE + "/default - restores settings to default values");
            player.spigot().sendMessage(new ComponentBuilder("check ")
            	    .color(net.md_5.bungee.api.ChatColor.WHITE)
            	    .append("here")
            	    .color(net.md_5.bungee.api.ChatColor.AQUA)
            	    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/colemaring/MC-AR-Sandbox"))
            	    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to visit GitHub page").color(net.md_5.bungee.api.ChatColor.WHITE).create()))
            	    .append(" for additional help")
            	    .color(net.md_5.bungee.api.ChatColor.WHITE)
            	    .create());
            
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
    }
    
    
    private void resetBlocks() {
    	int xScan = 0; 
    	int zScan = 0;
    	// check blocks that need to be replaced
    	// necessary because scale is dynamic
    	while (world.getBlockAt(xScan, 1, 0).getType() != Material.AIR)
    	{
    		xScan++;
    	}
    	while (world.getBlockAt(0, 1, zScan).getType() != Material.AIR)
    	{
    		zScan++;
    	}
    	
        for (int x = 0; x < xScan; x++) {
            for (int y = 0; y < world.getMaxHeight(); y++) {
                for (int z = 0; z < zScan; z++) {
//                    Location location = new Location(world, x, y, z);
//                    location.getBlock().setType(Material.AIR);
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
    
    private void resetWater() {
        
        for (int x = 0; x < array2D.length; x++) {
            for (int y = 0; y < world.getMaxHeight(); y++) {
                for (int z = 0; z < array2D[x].length; z++) {
//                    Location location = new Location(world, x, y, z);
                    if (world.getBlockAt(x, y, z).getType() == Material.WATER) {
                    	world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                   
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
//            		Location location2 = new Location(Bukkit.getWorlds().get(0), x, y, z);
            		if (world.getBlockAt(x, y, z).getType() == Material.AIR) {
                    	world.getBlockAt(x, y, z).setType(Material.WATER);
                    }
            	}
            }
        }
    }
    
    void setBiome(Block block, int i, String biome)
    {
    	if (biome.equals("mountains")) {
	        block.setType(i > 22 ? Material.DIRT : Material.STONE);
	    } else if (biome.equals("snowy")) {
	        block.setType(i > 25 ? Material.SNOW_BLOCK : i > 10 ? Material.STONE : Material.ANDESITE);
	    } else if (biome.equals("mesa")) {
	        if (i < 15)
	        	block.setType(Material.ORANGE_TERRACOTTA);
	        else if (i < 16)
	        {
	        	block.setType(Material.BROWN_TERRACOTTA);
	        }
	        else if (i < 17)
	        {
	        	block.setType(Material.GRAY_TERRACOTTA);
	        }
	        else if (i < 18)
	        {
	        	block.setType(Material.YELLOW_TERRACOTTA);
	        }
	        else if (i < 20)
	        {
	        	block.setType(Material.WHITE_TERRACOTTA);
	        }
	        else if (i < 21)
	        {
	        	block.setType(Material.RED_TERRACOTTA);
	        }
	        else if (i < 21)
	        {
	        	block.setType(Material.RED_TERRACOTTA);
	        }
	        else if (i < 25)
	        {
	        	block.setType(Material.YELLOW_TERRACOTTA);
	        }
	        else if (i < 26)
	        {
	        	block.setType(Material.ORANGE_TERRACOTTA);
	        }
	        else if (i < 30)
	        {
	        	block.setType(Material.BROWN_TERRACOTTA);
	        }
	        else
	        {
	        	block.setType(Material.YELLOW_TERRACOTTA);
	        }
	        
	    } else if (biome.equals("icy")) {
	        block.setType(i > 20 ? Material.ICE : i > 10 ? Material.BLUE_ICE : Material.ANDESITE);
	    }
	    else if (biome.equals("desert")) {
	        if (i < 12)
	        {
	        	block.setType(Material.STONE);
	        }
	        else if (i < 20)
	        {
	        	block.setType(Material.SANDSTONE);
	        }
	        else
	        {
	        	block.setType(Material.SAND);
	        }
	    }
    }

    void setBlocks() {
        if (array2D == null) return;
        
       
        resetBlocks();
        
//        getLogger().info("Setting blocks");

        for (int x = 0; x < array2D.length; x++) {
            for (int z = 0; z < array2D[x].length; z++) {
            	Block block;
            	for (int i = 0; i < Math.abs((int)(heightMultipler * array2D[x][z] * 0.05-82)); i++) {
            	    block = world.getBlockAt(x, i, z);
            	    setBiome(block, i, biome);
            	}
            }
        }
        // non cross sectional stuff
        if (biome == "mesa")
        {
        	 //scan for where to place red sand 
	        for (int a = 0; a < array2D.length; a++) {
	            for (int b = 0; b < 15; b++) {
	            	for (int c = 0; c < array2D[0].length; c++) {
	            		if ((world.getBlockAt(a, b, c).getType() == Material.ORANGE_TERRACOTTA) && (world.getBlockAt(a, b + 1, c ).getType() == Material.AIR))
	            		{
	            			world.getBlockAt(a, b, c).setType(Material.RED_SAND);
	            		}
	            	}
	            }
	        }
        }
        if (biome == "desert")
        {
        	 //scan for where to place red sand 
	        for (int a = 0; a < array2D.length; a++) {
	            for (int b = 5; b < 30; b++) {
	            	for (int c = 0; c < array2D[0].length; c++) {
	            		if (world.getBlockAt(a, b, c ).getType() == Material.SANDSTONE && world.getBlockAt(a, b + 1, c ).getType() == Material.AIR)
	            		{
	            			world.getBlockAt(a, b, c).setType(Material.SAND);
	            			world.getBlockAt(a, b -1 , c).setType(Material.SAND);
	            			
	            		}
	            	}
	            }
	        }
        }
        if (biome == "snowy")
        {
        	 //scan for where to place red sand 
	        for (int a = 0; a < array2D.length; a++) {
	            for (int b = 18; b < 25; b++) {
	            	for (int c = 0; c < array2D[0].length; c++) {
	            		if ((world.getBlockAt(a, b, c).getType() == Material.STONE) && (world.getBlockAt(a, b + 1, c ).getType() == Material.AIR))
	            		{
	            			world.getBlockAt(a, b, c).setType(Material.SNOW_BLOCK);
	            			world.getBlockAt(a, b-1, c).setType(Material.SNOW_BLOCK);
	            		}
	            	}
	            }
	        }
        }
        if (biome == "mountains")
        {
        	 //scan for where to place red sand 
	        for (int a = 0; a < array2D.length; a++) {
	            for (int b = 5; b < 35; b++) {
	            	for (int c = 0; c < array2D[0].length; c++) {
	            		if ((world.getBlockAt(a, b, c).getType() == Material.STONE) && (world.getBlockAt(a, b + 1, c ).getType() == Material.AIR))
	            		{
	            			world.getBlockAt(a, b, c).setType(Material.GRASS_BLOCK);
	            			world.getBlockAt(a, b -1, c).setType(Material.DIRT);
	            		}
	            		else if ((world.getBlockAt(a, b, c).getType() == Material.DIRT) && (world.getBlockAt(a, b + 1, c ).getType() == Material.AIR))
	            		{
	            			world.getBlockAt(a, b, c).setType(Material.GRASS_BLOCK);
	            		}
	            	}
	            }
	        }
        }
        if (waterlevel == -1)
        	setWater(0);
        else if (biome == "mesa")
        	setWater(0);
        else if (biome == "desert")
        	setWater(0);
        else if (biome == "icy")
        	setWater(25);
        else
        	setWater(waterlevel);
        
        getLogger().info("done setblocks");
    }
 	
//Save variables to config file
 public void saveVariables() {
	    File dataFolder = getDataFolder();
	    if (!dataFolder.exists()) {
	        dataFolder.mkdirs();
	    }
	    File configFile = new File(dataFolder, "config.yml");
	    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	    config.set("waterlevel", waterlevel);
	    config.set("autoupdate", autoupdate);
	    config.set("timer", timer);
	    config.set("biome", biome);
	    try {
	        config.save(configFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

//Load variables from config file
 public void loadVariables() {
	    File dataFolder = getDataFolder();
	    if (!dataFolder.exists()) {
	        dataFolder.mkdirs();
	    }
	    File configFile = new File(dataFolder, "config.yml");
	    if (!configFile.exists()) {
	        try {
	            configFile.createNewFile();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	    waterlevel = config.getInt("waterlevel", -1); // -1 is the default value if the key is not found
	    autoupdate = config.getBoolean("autoupdate"); 
	    timer = config.getInt("timer"); 
	    biome = config.getString("biome"); 
	}
 
}