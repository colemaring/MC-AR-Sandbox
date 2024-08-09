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
import java.util.ArrayList;
import java.util.List;

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

    private static final String FILE_PATH = "C:\\Users\\colem\\Desktop\\mcserver\\output.txt";
    private static int[][] array2D;
    private static String biome = "mountains";
    private static int waterlevel = -1;
    private static boolean autoupdate = false;
    private static int timer = 10;
    private static double heightMultipler = 1; // for later use
    private static World world;
    private static int scale = 150;
	private static double isRealTime = 1.0;
	private int[][] previousArray2D;
    private List<BlockChange> changes = new ArrayList<>();


    // runs when the plugin is enabled after the server is started
    @Override
    public void onEnable() {
    	previousArray2D = new int[90][171];
    	getServer().getPluginManager().registerEvents(this, this);
    	getCommand("waterlevel").setExecutor(this);
    	getCommand("autoupdate").setExecutor(this);
    	getCommand("timer").setExecutor(this);
    	getCommand("default").setExecutor(this);
    	getCommand("scale").setExecutor(this);
    	world = Bukkit.getWorlds().get(0);
    	loadVariables();
    	startReadingFile();
//    	setBlocks();
    	System.out.println("done");
    	
    	
    	
    	
    }
    
    // runs on server / plugin stop
    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
        }
        saveVariables();
    }
    
    public void startReadingFile() {
        Gson gson = new Gson();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
                array2D = gson.fromJson(content, int[][].class);
                if (scale != -1)
                    scaleMatrix(array2D, 150);

                // Create the list of block changes
                changes.clear();
                for (int x = 0; x < array2D.length; x++) {
                    for (int z = 0; z < array2D[0].length; z++) {
                        if (previousArray2D[x][z] != array2D[x][z]) {
                            changes.add(new BlockChange(x, z, array2D[x][z]));
                        }
                    }
                }

                // Update previousArray2D
                previousArray2D = array2D.clone();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 1); // 1 tick = 50ms
    }

    private class BlockChange {
        int x, z, y;

        public BlockChange(int x, int z, int y) {
            this.x = x;
            this.z = z;
            this.y = y;
        }
    }
    // WIP
    // changes the scale of the 2d array
    // think of this as converting a high resolution image to a lower resolution one,
    public void scaleMatrix(int[][] arr, int newLength) {
    	// newLength is the length, in blocks, that arr will be converted to fit
    	int[][] newArr = new int[(int) (newLength)][(arr.length * newLength)/arr[0].length];
    	
//    	System.out.println("size: " + newLength  + " " + (arr.length * newLength)/arr[0].length);
    	
    	double xIncrement = (double)arr.length / newLength;
    	double zIncrement = (double)arr[0].length / ((arr.length * newLength)/(double)arr[0].length);
//    	System.out.println("xinc " + xIncrement + " yinc " + zIncrement);
    	
    	for (int x = 0; x < newArr.length; x++) {
    		for (int z = 0; z < newArr[0].length; z++) {
        		newArr[x][z] = arr[(int)(x*xIncrement)][(int)(z*zIncrement)];
        	}
    	}
    	
    	int[][] trimmed = new int[90][171];
    	for (int x = 0; x < trimmed.length; x++) {
    		for (int z = 0; z < trimmed[0].length; z++) {
    			trimmed[x][z] = newArr[x+20][z+3];
        	}
    	}
    	
    	
    	array2D = trimmed;
    }
    
    // prevent water from flowing
    @EventHandler
    public void onWaterFlow(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.WATER) {
            event.setCancelled(true);
        }
    }
    
    // runs when a player joins
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
        
        if (autoupdate == true)
        {
        	Bukkit.getScheduler().cancelTasks(this);
            
    		// scheduler to repeatedly run terrain update task
            Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                public void run() {
                    setBlocks();
                }
            }, 0, (long) (timer * 20 * isRealTime)); // 1 sec = 20 ticks
        }
        
        // player join message added too much clutter
        event.setJoinMessage(""); 
        
        // inform player of settings and help menu
        player.sendMessage(ChatColor.WHITE + "Current waterlevel = " + waterlevel);
        player.sendMessage(ChatColor.WHITE + "Auto update is set to " + autoupdate);
        if (timer == 0)
        	player.sendMessage(ChatColor.WHITE + "Timer set to real-time");
        else
        	player.sendMessage(ChatColor.WHITE + "Timer set to " + timer); 
        player.sendMessage(ChatColor.WHITE + "Biome set to " + biome);
        player.sendMessage(ChatColor.GOLD + "Type /help for a list of available commands.");
        
        // give op player items for changing terrain and biome
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
    
    // reads the Kinect DepthFrame output file on seperate thread
//    public void readFileAndSetBlocks() {
//        Gson gson = new Gson();
//        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
//            try {
//                String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
//                array2D = gson.fromJson(content, int[][].class);
//                if (scale != -1)
//                	scaleMatrix(array2D, 150);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            
//            // wait until above process is done to call setBlocks()
//           Bukkit.getScheduler().runTask(this, this::setBlocks);
//        });
//    }
    
    
    // listener for player commands
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
                
        		// scheduler to repeatedly run terrain update task
                Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    public void run() {
                        setBlocks();
                    }
                }, 0, (long) (timer * 20 * isRealTime)); // 1 sec = 20 ticks
        	}
        	else	
        	{
        		sender.sendMessage("Disabled auto terrain updating");
        		autoupdate = false;
        		Bukkit.getScheduler().cancelTasks(this);
        	}
            
            return true;
        }
        
        else if (command.getName().equalsIgnoreCase("timer"))
        {

            if (args.length != 1) {
                sender.sendMessage("Usage: /timer <seconds>");
                return false;
            }
            timer = Integer.parseInt(args[0]);
            
        	if (timer == 0)
        		isRealTime = 0.75;
        	else
        		isRealTime = 1;
            
            // cancel existing task so we can create a new one of a different timer value
            if (autoupdate) {
                Bukkit.getScheduler().cancelTasks(this);
                Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    public void run() {
                    	setBlocks();
                    }
                }, 0, (long) (timer * 20 * isRealTime));
            }
            
            if (timer == 0)
            	sender.sendMessage("Changed timer to real-time");
            
            sender.sendMessage("Changed timer to " + timer + " seconds");
            
            return true;
        }
        else if (command.getName().equalsIgnoreCase("default"))
        {
        	// "default" values can be changed as seen fit
        	waterlevel = 10;
        	autoupdate = false;
        	timer = 10;
        	biome = "mountains";
        	scale = 150; 
        	
            sender.sendMessage("Reset settings to default values");
            
            return true;
        }
        else if (command.getName().equalsIgnoreCase("scale"))
        {
            if (args.length != 1) {
                sender.sendMessage("Usage: /scale <length>");
                return false;
            }
            scale = Integer.parseInt(args[0]);
            
            sender.sendMessage("Changed scale to " + scale + " blocks");
            
            return true;
        }
        
        
        
        return false;
    }
    
    // event handler for player interacting with an item 
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.BLAZE_ROD && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Update Terrain") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            player.sendMessage(ChatColor.GREEN + "Updating terrain..");
            setBlocks();
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
    
    // why am i using this and onCommand?
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
            player.sendMessage(ChatColor.WHITE + "/timer 0 will attempt to render in real time");
            player.sendMessage(ChatColor.WHITE + "/scale <length> - length in blocks of wanted x dimension");
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
    	// remove contents of inventory when player leaves
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
    }
    
    // replace blocks with air
    private void resetBlocks() {
//    	System.out.println("resetblocks called");
    	
//    	int xScan = 0; 
//    	int zScan = 0;
    	// check blocks that need to be replaced
    	// necessary because scale is dynamic
    	
        for (int x = 0; x < 90; x++) {
            for (int y = 0; y < world.getMaxHeight(); y++) {
                for (int z = 0; z < 171; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
    
    // used for changing water level
    private void resetWater() {
        for (int x = 0; x < array2D.length; x++) {
            for (int y = 0; y < world.getMaxHeight(); y++) {
                for (int z = 0; z < array2D[x].length; z++) {
                    if (world.getBlockAt(x, y, z).getType() == Material.WATER) 
                    	world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
    
    // sets the terrain water level
    private void setWater(int waterlevel) {
    	for (int x = 0; x < array2D.length; x++) {
            for (int z = 0; z < array2D[x].length; z++) {
            	for(int y = 0; y < waterlevel; y++) {
            		if (world.getBlockAt(x, y, z).getType() == Material.AIR) 
                    	world.getBlockAt(x, y, z).setType(Material.WATER);
            	}
            }
        }
    }
    
    // chooses what blocks are at what y level
    void setBiomeBlock(Block block, int i, String biome)
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
    
    // makes the given biome look more natural
    public void touchUpBiome(String biome)
    {
    	// non cross sectional stuff
        if (biome.equals("mesa"))
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
        if (biome.equals("desert"))
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
        if (biome.equals("snowy"))
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
        if (biome.equals("mountains"))
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
        else if (biome.equals("mesa"))
        	setWater(0);
        else if (biome.equals("desert"))
        	setWater(0);
        else if (biome.equals("icy"))
        	setWater(25);
        else
        	setWater(waterlevel);
    }

    // replace terrain with new data
    void setBlocks() {
        if (array2D == null) return;
            
        // delete old blocks
//        resetBlocks();
        
        for (BlockChange change : changes) {
            Block block = world.getBlockAt(change.x, change.y, change.z);
            if (block.getType().equals(Material.AIR)) {
            	int scany = change.y;
            	while (block.getType().equals(Material.AIR))
            	{
            		 block = world.getBlockAt(change.x, scany, change.z);
            		 scany -= 1;
            	}
            	for (int y1 = scany; y1 <= change.y; y1++)
            	{
            		 setBiomeBlock(world.getBlockAt(change.x, y1, change.z), y1, biome);
            	}
            }
            else
            {
            	while (!block.getType().equals(Material.AIR))
            	{
            		int scany = change.y;
                	while (!block.getType().equals(Material.AIR))
                	{
                		 block = world.getBlockAt(change.x, scany, change.z);
                		 scany += 1;
                	}
                	for (int y1 = scany; y1 >= change.y; y1--)
                	{
                		world.getBlockAt(change.x, scany, change.z).setType(Material.AIR);
                	}
            	}
            }
            
        }
        
//        // for each block in the specified data range, chose that block's material and set it
//        for (int x = 0; x < array2D.length; x++) {
//            for (int z = 0; z < array2D[x].length; z++) {
//            	Block block;
//            	for (int i = 0; i < 40 - Math.abs((int)(heightMultipler * array2D[x][z] * 0.05-110)); i++) {
//            	    block = world.getBlockAt(x, i, z);
//            	    setBiomeBlock(block, i, biome);
//            	}
//            }
//        }
//        touchUpBiome(biome);
    }
 	
    // save variables to config.yml in plugin data folder
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
	    config.set("scale", scale);
	    try {
	        config.save(configFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

    // load variables from config.yml file
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
	    waterlevel = config.getInt("waterlevel", -1);
	    autoupdate = config.getBoolean("autoupdate"); 
	    timer = config.getInt("timer"); 
	    biome = config.getString("biome"); 
	    scale = config.getInt("scale"); 
	}
 
}
