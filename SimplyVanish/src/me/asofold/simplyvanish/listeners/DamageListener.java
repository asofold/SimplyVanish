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
	
	@EventHandler(priority=EventPriority.LOW)
	void onFoodLevel(FoodLevelChangeEvent event){
		if ( event.isCancelled() ) return;
		final LivingEntity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		Player player = (Player) entity;
		if (event.getFoodLevel() - player.getFoodLevel() >= 0) return;
		final VanishConfig cfg = core.getVanishConfig(player.getName(), false);
		if (cfg == null) return;
		if (!cfg.vanished.state || cfg.damage.state) return;
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onEntityDamage(final EntityDamageEvent event){
		if ( event.isCancelled() ) return;
		final Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		final String playerName = ((Player) entity).getName();
		final VanishConfig cfg = core.getVanishConfig(playerName, false);
		if (cfg == null) return;
		if (!cfg.vanished.state || cfg.damage.state) return;
		event.setCancelled(true);
		if ( entity.getFireTicks()>0) entity.setFireTicks(0);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPotionSplash(PotionSplashEvent event){
		try{
			final List<Entity> rem = new LinkedList<Entity>();
			final Collection<LivingEntity> affected = event.getAffectedEntities();
			for ( LivingEntity entity : affected){
				if (entity instanceof Player ){
					String playerName = ((Player) entity).getName();
					VanishConfig cfg = core.getVanishConfig(playerName, false);
					if (cfg == null) continue;
					if (cfg.vanished.state){
						if (!cfg.damage.state) rem.add(entity);
					}
				}
			}
			if (!rem.isEmpty()) affected.removeAll(rem);
		} catch(Throwable t){
			// ignore (fast addition.)
		}
	}
	
}
