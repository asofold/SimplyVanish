package me.asofold.simplyvanish.listeners;

import me.asofold.simplyvanish.SimplyVanishCore;
import me.asofold.simplyvanish.config.VanishConfig;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class AttackListener implements Listener {
	private final SimplyVanishCore core;
	public AttackListener(SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancel(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state || cfg.attack.state) return false;
		return true;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEntitiyDamage(EntityDamageByEntityEvent event){
		if (event.isCancelled()) return;
		Entity entity = event.getDamager();
		if (entity == null) return;
		if (entity instanceof Projectile) entity = ((Projectile) entity).getShooter();
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);		
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onProjectileLaunch(ProjectileLaunchEvent event){
		Entity entity = event.getEntity().getShooter();
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onShootBow(EntityShootBowEvent event){
		// nmot sure about this one.
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		if (shouldCancel(((Player) entity).getName())) event.setCancelled(true);
	}
	
}
