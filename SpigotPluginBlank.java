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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

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

    private static final String FILE_PATH = "C:\\Users\\colem\\OneDrive\\Desktop\\mcserver\\output.txt";
    private static int[][] array2D;
    private static String biome = "mountains";
    private static int waterlevel = -1;
    private static boolean autoupdate = false;
    private static int timer = 1000;
    private static double heightMultipler = 1; // for later use
    private static World world;
    private static int scale = 150;
	private int[][] previousArray2D;
	private Map<Position, colChange> changes = new ConcurrentHashMap<>();
	private boolean firstRead = true;
	private int updateTaskId = -1;	

    // runs when the plugin is enabled after the server is started
    @Override
    public void onEnable() {
    	previousArray2D = new int[150][285];
    	getServer().getPluginManager().registerEvents(this, this);
    	getCommand("waterlevel").setExecutor(this);
    	getCommand("autoupdate").setExecutor(this);
    	getCommand("timer").setExecutor(this);
    	getCommand("default").setExecutor(this);
    	getCommand("scale").setExecutor(this);
    	world = Bukkit.getWorlds().get(0);
    	loadVariables();
    	startReadingFile();
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
                    scaleMatrix(array2D, 264);

                if (firstRead) {
                    previousArray2D = array2D.clone(); // initialize previousArray2D with array2D values
                    firstRead = false;
                }
                
                //System.out.println("reading file");
                // Create the list of block changes
                
                for (int x = 0; x < array2D.length; x++) {
                    for (int z = 0; z < array2D[0].length; z++) {
                        if (previousArray2D[x][z] != array2D[x][z]) {
                        	Position position = new Position(x, z);
                            changes.put(position, new colChange(x, z, array2D[x][z]));
                            //System.out.println("change found");
                        }
                    }
                }

                // Update previousArray2D
                previousArray2D = array2D.clone();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 10); // 1 tick = 50ms
    }
    
    
    
    public class Position {
        private int x;
        private int z;

        public Position(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && z == position.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    public class colChange {
        private int x;
        private int z;
        private int value;

        public colChange(int x, int z, int value) {
            this.x = x;
            this.z = z;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            colChange colChange = (colChange) o;
            return x == colChange.x && z == colChange.z && value == colChange.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z, value);
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
    	
    	int[][] trimmed = new int[150][285];
    	for (int x = 0; x < trimmed.length; x++) {
    		for (int z = 0; z < trimmed[0].length; z++) {
    			trimmed[x][z] = newArr[x+47][z+20];
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
        	// Cancel the existing update task if it exists
            if (updateTaskId != -1) {
                Bukkit.getScheduler().cancelTask(updateTaskId);
            }
            
            // scheduler to repeatedly run terrain update task
            updateTaskId = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                public void run() {
                    updateBlocks();
                }
            }, 0, (long) (((double)timer / 1000) * 20)).getTaskId(); // 1 sec = 20 ticks
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
//    public void readFileAndupdateBlocks() {
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
//            // wait until above process is done to call updateBlocks()
//           Bukkit.getScheduler().runTask(this, this::updateBlocks);
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
                
                // Cancel the existing update task if it exists
                if (updateTaskId != -1) {
                    Bukkit.getScheduler().cancelTask(updateTaskId);
                }
                
                // scheduler to repeatedly run terrain update task
                updateTaskId = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    public void run() {
                        updateBlocks();
                    }
                }, 0, (long) (((double)timer / 1000) * 20)).getTaskId(); // 1 sec = 20 ticks
            }
        	else	
        	{
        		sender.sendMessage("Disabled auto terrain updating");
        		autoupdate = false;
        		Bukkit.getScheduler().cancelTask(updateTaskId);
        	}
            
            return true;
        }
        
        else if (command.getName().equalsIgnoreCase("timer"))
        {

            if (args.length != 1) {
                sender.sendMessage("Usage: /timer <ms>");
                return false;
            }
            timer = Integer.parseInt(args[0]);
            
            
            // cancel existing task so we can create a new one of a different timer value
            if (autoupdate) {
            	// Cancel the existing update task if it exists
                if (updateTaskId != -1) {
                    Bukkit.getScheduler().cancelTask(updateTaskId);
                }
                
                // scheduler to repeatedly run terrain update task
                updateTaskId = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    public void run() {
                        updateBlocks();
                    }
                }, 0, (long) (((double)timer / 1000) * 20)).getTaskId(); // 1 sec = 20 ticks
            }
            
            
            sender.sendMessage("Changed timer to " + timer + " ms");
            
            return true;
        }
        else if (command.getName().equalsIgnoreCase("default"))
        {
        	// "default" values can be changed as seen fit
        	waterlevel = 10;
        	autoupdate = false;
        	timer = 1000;
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
            updateBlocks();
            
        }
        if (event.getItem() != null && event.getItem().getType() == Material.GRASS_BLOCK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mountain Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "mountains";
            player.sendMessage(ChatColor.GREEN + "Updating biome to normal mountains..");
            resetBlocks();
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.SNOW_BLOCK && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Snowy Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "snowy";
            player.sendMessage(ChatColor.GREEN + "Updating biome to snowy mountains..");
            resetBlocks();
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.RED_SAND && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mesa Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "mesa";
            player.sendMessage(ChatColor.GREEN + "Updating biome to mesa..");
            resetBlocks();
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.BLUE_ICE && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Icy Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "icy";
            player.sendMessage(ChatColor.GREEN + "Updating biome to frozen ocean..");
            resetBlocks();
            setBlocks();
        }
        if (event.getItem() != null && event.getItem().getType() == Material.SAND && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Desert Biome") && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            biome = "desert";
            player.sendMessage(ChatColor.GREEN + "Updating biome to desert..");
            resetBlocks();
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
            player.sendMessage(ChatColor.WHITE + "/timer <ms> - set a timer in ms for autoupdate");
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
//    	System.out.println("reupdateBlocks called");
    	
//    	int xScan = 0; 
//    	int zScan = 0;
    	// check blocks that need to be replaced
    	// necessary because scale is dynamic
    	
        for (int x = 0; x < 150; x++) {
            for (int y = 0; y < world.getMaxHeight(); y++) {
                for (int z = 0; z < 285; z++) {
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
	            for (int b = 0; b < 20; b++) {
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
	            for (int b = 10; b < 45; b++) {
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
	            for (int b = 27; b < 45; b++) {
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
	            for (int b = 10; b < 45; b++) {
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
    
    void setBlocks()
    {
    // for each block in the specified data range, chose that block's material and set it
      for (int x = 0; x < array2D.length; x++) {
          for (int z = 0; z < array2D[x].length; z++) {
          	Block block;
          	for (int i = 0; i < 25+ Math.abs((int)(heightMultipler * array2D[x][z] * 0.05-130)); i++) {
          	    block = world.getBlockAt(x, i, z);
          	    setBiomeBlock(block, i, biome);
          	}
          }
      }
//      touchUpBiome(biome);
    }

    // replace terrain with new data
    void updateBlocks() {
        if (array2D == null) return;
            
//        // delete old blocks
//        reupdateBlocks();
        
//        System.out.println("updateBlocks called");
//        
//        for (colChange change : changes.values()) {
//            System.out.println("x: " + change.x + ", z: " + change.z + ", value: " + (40 - Math.abs((int)(heightMultipler * change.value * 0.05-110))));
//        }
//        
        for (colChange change : changes.values()) {
        	int y = 25 + ( Math.abs((int)(heightMultipler * change.value * 0.05-130)));
        
            Block block = world.getBlockAt(change.x, y, change.z);
//            System.out.println(block.getType().equals(Material.AIR));
            if (block.getType().equals(Material.AIR) || block.getType().equals(Material.WATER)) {
            	int scany = y;
            	while (block.getType().equals(Material.AIR) || block.getType().equals(Material.WATER))
            	{
            		 block = world.getBlockAt(change.x, scany, change.z);
            		 scany -= 1;
            	}
            	for (int y1 = scany; y1 < y; y1++)
            	{
        		 //world.getBlockAt(change.x, y1, change.z).setType(Material.DIAMOND_BLOCK);
        		 //System.out.println("changing block at x:" + change.x + " z:" + change.z + " y:" +y1 + " to blocks");
        		 setBiomeBlock(world.getBlockAt(change.x, y1, change.z), y1, biome);
            	
            	}
            }
            else
            {
            	while (!block.getType().equals(Material.AIR))
            	{
            		int scany = y;
                	while (!block.getType().equals(Material.AIR))
                	{
                		 block = world.getBlockAt(change.x, scany, change.z);
                		 scany += 1;
                	}
                	for (int y1 = scany; y1 > y; y1--)
                	{
                		world.getBlockAt(change.x, y1, change.z).setType(Material.AIR);
                		//System.out.println("changing block at x:" + change.x + " z:" + change.z + " y:" +y1 + " to air");
                	}
            	}
            }
            
            
   
        // for each block in the specified data range, chose that block's material and set it
//        for (int x = 0; x < array2D.length; x++) {
//            for (int z = 0; z < array2D[x].length; z++) {
//            	Block block;
//            	for (int i = 0; i < 40 - Math.abs((int)(heightMultipler * array2D[x][z] * 0.05-110)); i++) {
//            	    block = world.getBlockAt(x, i, z);
//            	    setBiomeBlock(block, i, biome);
//            	}
//            }
//        }
//        
        }
        touchUpBiome(biome);
//        setWater(waterlevel);
        changes.clear();
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
