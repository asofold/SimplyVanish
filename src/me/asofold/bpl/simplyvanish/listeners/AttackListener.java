package me.asofold.bpl.simplyvanish.listeners;

import me.asofold.bpl.simplyvanish.SimplyVanishCore;
import me.asofold.bpl.simplyvanish.config.VanishConfig;
import me.asofold.bpl.simplyvanish.util.Utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public final class AttackListener implements Listener {
	private final SimplyVanishCore core;
	public AttackListener(final SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancelAttack(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state || cfg.attack.state) return false;
		return true;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onEntitiyDamage(final EntityDamageByEntityEvent event){
		// TODO: maybe integrate with the damage check
		if (event.isCancelled()) return;
		Entity entity = event.getDamager();
		if (entity == null) return;
		if (entity instanceof Projectile) entity = Utils.getShooterEntity((Projectile) entity);
		if (!(entity instanceof Player)) return;
		if (shouldCancelAttack(((Player) entity).getName())) event.setCancelled(true);		
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onProjectileLaunch(final ProjectileLaunchEvent event){
		final Entity entity = Utils.getShooterEntity(event.getEntity());
		if (!(entity instanceof Player)) return;
		if (shouldCancelAttack(((Player) entity).getName())) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onShootBow(final EntityShootBowEvent event){
		// nmot sure about this one.
		final Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		if (shouldCancelAttack(((Player) entity).getName())) event.setCancelled(true);
	}
	
}
