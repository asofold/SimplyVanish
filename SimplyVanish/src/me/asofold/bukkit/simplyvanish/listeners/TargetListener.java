package me.asofold.bukkit.simplyvanish.listeners;

import me.asofold.bukkit.simplyvanish.SimplyVanishCore;
import me.asofold.bukkit.simplyvanish.config.Settings;
import me.asofold.bukkit.simplyvanish.config.VanishConfig;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class TargetListener implements Listener {
	private final SimplyVanishCore core;
	public TargetListener(SimplyVanishCore core){
		this.core = core;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEntityTarget(final EntityTargetEvent event){
		if ( event.isCancelled() ) return;
		final Entity target = event.getTarget();
		if (!(target instanceof Player)) return;
		final String playerName = ((Player) target).getName();
		final VanishConfig cfg = core.getVanishConfig(playerName, false);
		if (cfg == null) return;
		if (cfg.vanished.state){
			Settings settings = core.getSettings();
			if (settings.expEnabled && !cfg.pickup.state){
				Entity entity = event.getEntity();
				if ( entity instanceof ExperienceOrb){
					repellExpOrb((Player) target, (ExperienceOrb) entity, settings);
					event.setCancelled(true);
					event.setTarget(null);
					return;
				}
			}
			if (!cfg.target.state) event.setTarget(null);
		}
	}
	
	/**
	 * Attempt some workaround for experience orbs:
	 * prevent it getting near the player.
	 * @param target
	 * @param entity
	 */
	void repellExpOrb(Player player, ExperienceOrb orb, Settings settings) {
		Location pLoc = player.getLocation();
		Location oLoc = orb.getLocation();
		Vector dir = oLoc.toVector().subtract(pLoc.toVector());
		double dx = Math.abs(dir.getX());
		double dz = Math.abs(dir.getZ());
		if ( (dx == 0.0) && (dz == 0.0)){
			// Special case probably never happens
			dir.setX(0.001);
		}
		if ((dx < settings.expThreshold) && (dz < settings.expThreshold)){
			Vector nDir = dir.normalize();
			Vector newV = nDir.clone().multiply(settings.expVelocity);
			newV.setY(0);
			orb.setVelocity(newV);
			if ((dx < settings.expTeleDist) && (dz < settings.expTeleDist)){
				// maybe oLoc
				orb.teleport(oLoc.clone().add(nDir.multiply(settings.expTeleDist)), TeleportCause.PLUGIN);
			} 
			if ((dx < settings.expKillDist) && (dz < settings.expKillDist)){
				orb.remove();
			} 
		} 
	}
}