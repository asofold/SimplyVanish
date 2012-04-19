package me.asofold.simplyvanish.listeners;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import me.asofold.simplyvanish.SimplyVanishCore;
import me.asofold.simplyvanish.config.VanishConfig;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class DamageListener implements Listener {
	
	private final SimplyVanishCore core;
	
	public DamageListener(SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancelDamage(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state || cfg.damage.state) return false;
		return true;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onFoodLevel(FoodLevelChangeEvent event){
		if ( event.isCancelled() ) return;
		final LivingEntity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		Player player = (Player) entity;
		if (event.getFoodLevel() - player.getFoodLevel() >= 0) return;
		if (shouldCancelDamage(player.getName()))	event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onEntityDamage(final EntityDamageEvent event){
		if ( event.isCancelled() ) return;
		final Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		if (shouldCancelDamage(((Player) entity).getName())){
			event.setCancelled(true);
			if ( entity.getFireTicks()>0) entity.setFireTicks(0);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPotionSplash(PotionSplashEvent event){
		try{
			final List<Entity> rem = new LinkedList<Entity>();
			final Collection<LivingEntity> affected = event.getAffectedEntities();
			for ( LivingEntity entity : affected){
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
