package me.asofold.bukkit.simplyvanish.listeners;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import me.asofold.bukkit.simplyvanish.SimplyVanishCore;
import me.asofold.bukkit.simplyvanish.config.VanishConfig;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public final class DamageListener implements Listener {
	
	private final SimplyVanishCore core;
	
	public DamageListener(final SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancelDamage(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (cfg.god.state) return true;
		if (!cfg.vanished.state || cfg.damage.state) return false;
		return true;
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	final void onFoodLevel(final FoodLevelChangeEvent event){
//		if ( event.isCancelled() ) return;
		final LivingEntity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		final Player player = (Player) entity;
		if (event.getFoodLevel() - player.getFoodLevel() >= 0) return;
		if (shouldCancelDamage(player.getName()))	event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	final void onEntityDamage(final EntityDamageEvent event){
//		if ( event.isCancelled() ) return;
		final Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		if (shouldCancelDamage(((Player) entity).getName())){
			event.setCancelled(true);
			if ( entity.getFireTicks()>0) entity.setFireTicks(0);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	final void onPotionSplash(final PotionSplashEvent event){
		try{
			final List<Entity> rem = new LinkedList<Entity>();
			final Collection<LivingEntity> affected = event.getAffectedEntities();
			for ( final LivingEntity entity : affected){
				if (entity instanceof Player ){
					if (shouldCancelDamage(((Player) entity).getName())) rem.add(entity);
				}
			}
			if (!rem.isEmpty()) affected.removeAll(rem);
		} catch(Throwable t){
			// ignore (fast addition.)
		}
	}
	
}
