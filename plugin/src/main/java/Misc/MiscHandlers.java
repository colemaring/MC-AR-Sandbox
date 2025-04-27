package Misc;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import Gamemodes.ZombieRush;
import Main.KinectSandbox;
import Terrain.TerrainGeneratorHelper;

public class MiscHandlers implements Listener{
	
	// Prevent hand swap
	@EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }
	
	// Stop zombie burning in daylight
	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
	    if (event.getEntity() instanceof Zombie) {
	        event.setCancelled(true);
	    }
	}
	
	// If zombie rush is running, allow zombie spawns, otherwise disable everything
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
	    if (!ZombieRush.running)
	        event.setCancelled(true);
	    else if (event.getEntityType() != EntityType.ZOMBIE && event.getEntityType() != EntityType.ARMOR_STAND)
	        event.setCancelled(true);
	}
	
	// Prevent grass from turning into dirt
//	@EventHandler
//	public void onBlockPhysicsEvent(BlockPhysicsEvent event)
//	{
//		if (event.getChangedType().equals(Material.DIRT))
//		{
//			event.setCancelled(true);
//		}
//	}


	
	// Prevent item drops when entities die
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
	    if (event.getEntity() instanceof Animals) {
	        event.getDrops().clear(); // Removes all item drops
	        event.setDroppedExp(0);   // Optional: remove XP drop too
	    }
	}
	
	 // Prevent tnt from dropping entities when it breaks blocks
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            block.setType(org.bukkit.Material.AIR); // Remove block
        }
        blocks.clear(); // Prevent items from dropping
    }

	
    // Disable water & lava flow
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!KinectSandbox.allowWaterFlow) {
            Material type = event.getBlock().getType();
            if (type == Material.WATER || type == Material.LAVA) {
                event.setCancelled(true);
            }
        }
    }

    
    // Disable blocks from falling
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();
            Material type = fallingBlock.getBlockData().getMaterial();
            if (type == Material.SAND || type == Material.RED_SAND) {
                event.setCancelled(true);
            }
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

	    int middleX = TerrainGeneratorHelper.findXEnd() / 2;
	    int middleZ =TerrainGeneratorHelper.findZEnd() / 2;
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
    
    // Remove any kind of entity, except players
    public static void killEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                // Check if the entity is not a player
                if (!(entity instanceof Player)) {
                    entity.remove(); // Remove the entity if it's not a player
                }
            }
        }
    }

    

}
