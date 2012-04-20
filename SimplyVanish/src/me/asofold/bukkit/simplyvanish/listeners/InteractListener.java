package me.asofold.bukkit.simplyvanish.listeners;

import me.asofold.bukkit.simplyvanish.SimplyVanishCore;
import me.asofold.bukkit.simplyvanish.config.Settings;
import me.asofold.bukkit.simplyvanish.config.VanishConfig;
import me.asofold.bukkit.simplyvanish.util.Utils;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

/**
 * TODO: find out which event i missed, i just clicked it away.
 * @author mc_dev
 *
 */
public class InteractListener implements Listener {
	private final SimplyVanishCore core;
	public InteractListener(SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancel(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state || cfg.interact.state) return false;
		return true;
	}
	
	private boolean hasBypass(Player player, EntityType type) {
		if (!core.getVanishConfig(player.getName(), false).bypass.state) return false;
		if (type == null) return false;
		Settings settings = core.getSettings();
		if (!settings.bypassIgnorePermissions && player.hasPermission("simplyvanish.flags.bypass."+(type.toString().toLowerCase()))) return true;
		else if (settings.bypassEntities.contains(type)) return true; 
		else return false;
	}
	
	private boolean hasBypass(Player player, int blockId) {
		if (!core.getVanishConfig(player.getName(), false).bypass.state) return false;
		Settings settings = core.getSettings();
		if (!settings.bypassIgnorePermissions && player.hasPermission("simplyvanish.flags.bypass."+blockId)) return true;
		else if (settings.bypassBlocks.contains(blockId)) return true;
		else return false;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onInteract(PlayerInteractEvent event){
		// This is on highest to allow the use of info and teleport tools.
		if (event.isCancelled()) return;
		final Player player = event.getPlayer();
		if (shouldCancel(player.getName())){
			Block block = event.getClickedBlock();
			if (block != null && hasBypass(player, block.getTypeId())) return;
			event.setCancelled(true);
			Utils.sendBlock(event.getPlayer(), block);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	// This is on highest to allow the use of info and teleport tools.
	void onInteractEntity(PlayerInteractEntityEvent event){
		if (event.isCancelled()) return;
		final Player player = event.getPlayer();
		if (shouldCancel(player.getName())){
			// check bypass:
			if (hasBypass(player, event.getRightClicked().getType())) return;
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	void onBucketFill(PlayerBucketFillEvent event){
		if (event.isCancelled()) return;
		if (shouldCancel(event.getPlayer().getName())){
			event.setCancelled(true);
			Utils.sendBlock(event.getPlayer(), event.getBlockClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onBlockBreak(BlockBreakEvent event){
		// Do add these for bypasses.
		if (event.isCancelled()) return;
		if (shouldCancel(event.getPlayer().getName())) event.setCancelled(true);
	}
	
	
	// TODO: some of the following might be obsolete (interact-entity)

	@EventHandler(priority=EventPriority.LOW)
	void onBucketEmpty(PlayerBucketEmptyEvent event){
		if (event.isCancelled()) return;
		if (shouldCancel(event.getPlayer().getName())){
			event.setCancelled(true);
			Utils.sendBlock(event.getPlayer(), event.getBlockClicked());
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	void onVehicleDestroy(VehicleDestroyEvent event){
		Entity entity = event.getAttacker();
		if (entity instanceof Projectile) entity = ((Projectile) entity).getShooter();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
 	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onVehicleEnter(VehicleEnterEvent event){
		// this could be omitted, probably
		Entity entity = event.getEntered();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onVehicleExit(VehicleExitEvent event){
		Entity entity = event.getExited();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
	
	@EventHandler(priority=EventPriority.LOW)
	void onPaintingBreak(PaintingBreakByEntityEvent event){
		Entity entity = event.getRemover();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEgg(PlayerEggThrowEvent event){
		if (shouldCancel(event.getPlayer().getName())) event.setHatching(false);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onCollision(VehicleEntityCollisionEvent event){
		Entity entity = event.getEntity();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())){
			event.setPickupCancelled(true);
			event.setCollisionCancelled(true);
			// maybe that is enough:
			event.setCancelled(true);
		}
	}
	
}
