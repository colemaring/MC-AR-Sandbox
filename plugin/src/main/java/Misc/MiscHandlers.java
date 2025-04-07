package Misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import Main.KinectSandbox;
import Terrain.TerrainGeneratorHelper;

public class MiscHandlers implements Listener{
	KinectSandbox plugin;
	public MiscHandlers(KinectSandbox plugin)
	{
		this.plugin = plugin;
	}
	
    // Disable water & lava flow
	// lava flow not being stopped?
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Material type = event.getBlock().getType();

        if (type == Material.WATER || type == Material.LAVA)
            event.setCancelled(true);
    }
    
    // Disable blocks from falling
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Material type = event.getBlock().getType();

        if (type == Material.SAND || type == Material.RED_SAND) {
            event.setCancelled(true);
        }
    }


	// TP players to middle of world when joining
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
	    Player player = event.getPlayer();
	    World world = player.getWorld();

	    // find world bounds 
	    // this solution is so naive and bad 
	    int x = 0;
	    int z = 0;
	    while (!world.getBlockAt(x, 0, 0).isEmpty())
	        x++;
	    while (!world.getBlockAt(0, 0, z).isEmpty())
	        z++;

	    int middleX = TerrainGeneratorHelper.terrainHeight / 2;
	    int middleZ =TerrainGeneratorHelper.terrainWidth / 2;
	    int middleY = 81;

	    Location target = new Location(world, middleX + 0.5, middleY, middleZ + 0.5);
	    target.setPitch(90);
	    target.setYaw(90);

	    player.teleport(target);
	}

	// prevent non-op players from breaking blocks
	 @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!p.isOp())
            event.setCancelled(true);
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (!p.isOp())
            event.setCancelled(true);
    }

}
