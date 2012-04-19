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

public class AttackListener implements Listener {
	private final SimplyVanishCore core;
	public AttackListener(SimplyVanishCore core){
		this.core = core;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEntitiyDamage(EntityDamageByEntityEvent event){
		if (event.isCancelled()) return;
		Entity entity = event.getDamager();
		if (entity == null) return;
		if (entity instanceof Projectile){
			Projectile p = (Projectile) entity;
			entity = p.getShooter();
		} 
		if (!(entity instanceof Player)) return;
		final VanishConfig cfg = core.getVanishConfig(((Player) entity).getName(), false);
		if (cfg == null) return;
		if (!cfg.vanished.state || cfg.attack.state) return;
		event.setCancelled(true);
	}
}
