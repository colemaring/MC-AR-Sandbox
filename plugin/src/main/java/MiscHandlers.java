import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class MiscHandlers implements Listener{
	KinectSandbox plugin;
	MiscHandlers(KinectSandbox plugin)
	{
		this.plugin = plugin;
	}
	
	// RE-WRITE THIS 
	// using values from terrainGeneratorHelper probably
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

	    int middleX = x / 2;
	    int middleZ = z / 2;
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
