package me.asofold.bpl.simplyvanish.listeners;

import me.asofold.bpl.simplyvanish.SimplyVanishCore;
import me.asofold.bpl.simplyvanish.config.Settings;
import me.asofold.bpl.simplyvanish.config.VanishConfig;
import me.asofold.bpl.simplyvanish.inventories.InventoryUtil;
import me.asofold.bpl.simplyvanish.util.Utils;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
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
public final class InteractListener implements Listener {
	private final SimplyVanishCore core;
	public InteractListener(final SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancel(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state || cfg.interact.state) return false;
		return true;
	}
	
	private final boolean hasBypass(final Player player, final EntityType type) {
		if (!core.getVanishConfig(player.getName(), false).bypass.state) return false;
		if (type == null) return false;
		final Settings settings = core.getSettings();
		if (!settings.bypassIgnorePermissions && player.hasPermission("simplyvanish.flags.bypass."+(type.toString().toLowerCase()))) return true;
		else if (settings.bypassEntities.contains(type)) return true; 
		else return false;
	}
	
	private final boolean hasBypass(final Player player, final int blockId) {
		if (!core.getVanishConfig(player.getName(), false).bypass.state) return false;
		final Settings settings = core.getSettings();
		if (!settings.bypassIgnorePermissions && player.hasPermission("simplyvanish.flags.bypass."+blockId)) return true;
		else if (settings.bypassBlocks.contains(blockId)) return true;
		else return false;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	final void onInteract(final PlayerInteractEvent event){
		// This is on highest to allow the use of info and teleport tools.
		if (event.isCancelled()) return;
		final Player player = event.getPlayer();
		if (shouldCancel(player.getName())){
			final Block block = event.getClickedBlock();
			if (block != null && hasBypass(player, block.getTypeId())) return;
			event.setCancelled(true);
			Utils.sendBlock(event.getPlayer(), block);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	// This is on highest to allow the use of info and teleport tools.
	final void onInteractEntity(final PlayerInteractEntityEvent event){
		if (event.isCancelled()) return;
		final Player player = event.getPlayer();
		final String playerName = player.getName();
		final VanishConfig cfg = core.getVanishConfig(playerName, false);
		if (cfg == null) return;
		final Entity entity = event.getRightClicked();
		if (cfg.vanished.state && entity instanceof Player){
			if (core.hasPermission(player,  "simplyvanish.inventories.peek.at-all")){
				final Player other = ((Player) entity);
				InventoryUtil.showInventory(player, cfg, other.getName(), core.getSettings());
			}
		}
		if (shouldCancel(player.getName())){
			// check bypass:
			if (hasBypass(player, entity.getType())) return;
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	final void onBucketFill(final PlayerBucketFillEvent event){
		if (event.isCancelled()) return;
		if (shouldCancel(event.getPlayer().getName())){
			event.setCancelled(true);
			Utils.sendBlock(event.getPlayer(), event.getBlockClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onBlockBreak(final BlockBreakEvent event){
		// Do add these for bypasses.
		if (event.isCancelled()) return;
		if (shouldCancel(event.getPlayer().getName())) event.setCancelled(true);
	}
	
	
	// TODO: some of the following might be obsolete (interact-entity)

	@EventHandler(priority=EventPriority.LOW)
	final void onBucketEmpty(final PlayerBucketEmptyEvent event){
		if (event.isCancelled()) return;
		if (shouldCancel(event.getPlayer().getName())){
			event.setCancelled(true);
			Utils.sendBlock(event.getPlayer(), event.getBlockClicked());
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	final void onVehicleDestroy(final VehicleDestroyEvent event){
		Entity entity = event.getAttacker();
		if (entity instanceof Projectile) entity = Utils.getShooterEntity((Projectile) entity);
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
 	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onVehicleEnter(final VehicleEnterEvent event){
		// this could be omitted, probably
		final Entity entity = event.getEntered();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onVehicleExit(final VehicleExitEvent event){
		Entity entity = event.getExited();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
	
	@EventHandler(priority=EventPriority.LOW)
	final void onPaintingBreak(final HangingBreakByEntityEvent event){
		final Entity entity = event.getRemover();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onEgg(final PlayerEggThrowEvent event){
		if (shouldCancel(event.getPlayer().getName())) event.setHatching(false);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onCollision(final VehicleEntityCollisionEvent event){
		final Entity entity = event.getEntity();
		if (entity == null) return;
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())){
			event.setPickupCancelled(true);
			event.setCollisionCancelled(true);
			// maybe that is enough:
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	final void onInventoryOpen(final InventoryOpenEvent event){
		final LivingEntity entitiy = event.getPlayer();
		if (!(entitiy instanceof Player)) return;
		final Player player = (Player) entitiy;
		final VanishConfig cfg = core.getVanishConfig(player.getName(), false);
		if (cfg == null) return;
		if (!cfg.vanished.state){
			cfg.preventInventoryAction = false;
			return;
		}
		// TODO: extra inventory settings or permission or one more flag ?
		InventoryUtil.prepareInventoryOpen(player, event.getInventory(), cfg);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	final void onInventoryClose(final InventoryCloseEvent event){
		final LivingEntity entitiy = event.getPlayer();
		if (!(entitiy instanceof Player)) return;
		final Player player = (Player) entitiy;
		final VanishConfig cfg = core.getVanishConfig(player.getName(), false);
		if (cfg == null) return;
		cfg.preventInventoryAction = false;
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled=true)
	final void onInventoryClick(final InventoryClickEvent event){
		final LivingEntity entitiy = event.getWhoClicked();
		if (!(entitiy instanceof Player)) return;
		final Player player = (Player) entitiy;
		final VanishConfig cfg = core.getVanishConfig(player.getName(), false);
		if (cfg == null) return;
		if (cfg.preventInventoryAction){
			event.setResult(Result.DENY);
			event.setCancelled(true);
		}
	}
	
}
