package me.asofold.bpl.simplyvanish.api.hooks.impl;

import me.asofold.bpl.simplyvanish.SimplyVanish;
import me.asofold.bpl.simplyvanish.api.events.SimplyVanishAtLoginEvent;
import me.asofold.bpl.simplyvanish.api.events.SimplyVanishStateEvent;
import me.asofold.bpl.simplyvanish.api.hooks.AbstractHook;
import me.asofold.bpl.simplyvanish.api.hooks.HookListener;
import me.asofold.bpl.simplyvanish.api.hooks.HookPurpose;
import me.asofold.bpl.simplyvanish.config.VanishConfig;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.api.PlayerDisguiseEvent;
import pgDev.bukkit.DisguiseCraft.api.PlayerUndisguiseEvent;

public class DisguiseCraftHook  extends AbstractHook {
	
	
//	boolean blocked = false; // TODO: consider this.
	
	private final HookListener listener = new HookListener(){
		
		@Override
		public boolean unregisterEvents() {
			// TODO: wait for Bukkit ?
			return false;
		}
		
		@EventHandler(priority = EventPriority.MONITOR)
		void onVisibility(SimplyVanishStateEvent event){
			if (event.isCancelled()) return;
			if (event.getVisibleAfter()){
				boolean keep = false;
				if (event instanceof SimplyVanishAtLoginEvent) keep = true;
				onInvisible(event.getPlayerName(), keep);
			} 
			else{
				Player player = Bukkit.getServer().getPlayerExact(event.getPlayerName());
				if (player !=null && DisguiseCraft.getAPI().isDisguised(player)){
					event.setCancelled(true);
					// TODO: Consider suppressing if notify=false.
					if (shouldNotify(player)) {
						player.sendMessage(SimplyVanish.msgLabel+ChatColor.GRAY+"Use "+ChatColor.YELLOW+"/undis"+ChatColor.GRAY+"guise !");
					}
				}
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR)
		void onDisguise(PlayerDisguiseEvent event){
			if (event.isCancelled()) return;
			Player player = event.getPlayer();
			String name = player.getName();
			if (SimplyVanish.isVanished(name)){
				if (!SimplyVanish.setVanished(player, false)){
					event.setCancelled(true); // TODO: something
					// TODO: Consider suppressing if notify=false.
					if (shouldNotify(player)) {
						player.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Can not disguise (something prevents reappear).");
					}
				}
			}
		}
		
		private boolean shouldNotify(Player player) {
			final VanishConfig vcfg = SimplyVanish.getVanishConfig(player.getName(), false);
			if (vcfg == null) {
				return true;
			} else {
				return vcfg.notify.state;
			}
		}
		
		@SuppressWarnings("unused")
		@EventHandler(priority=EventPriority.MONITOR)
		void onUndisguise(PlayerUndisguiseEvent event){
			if (event.isCancelled()) return;
			final Player player = event.getPlayer();
			String name = player.getName();
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SimplyVanish.getPluginInstance(), new Runnable(){
				@Override
				public void run() {
					Player dummy = Bukkit.getServer().getPlayerExact(player.getName());
					if (dummy != null) SimplyVanish.updateVanishState(dummy, false, hookId);
				}});
		}
		
	};
	
	/**
	 * 
	 * @param playerName
	 * @param keepDisguise
	 */
	public void onInvisible(String playerName, boolean keepDisguise) {
		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player == null) return;
		DisguiseCraftAPI api = DisguiseCraft.getAPI();
		if (api.isDisguised(player)){
			// Disguise and remember disguise.
			api.undisguisePlayer(player); // TODO: change priorities and check result, act accordingly.
		}
		return;
	}

	@Override
	public final boolean allowUpdateVanishState(final Player player, final int hookId, boolean isAllowed) {
		if (!isAllowed) return false;
		if (hookId == this.hookId) return true;
		else {
			DisguiseCraftAPI api = DisguiseCraft.getAPI();
			return !api.isDisguised(player);
		}
	}
	
	

	@Override
	public boolean allowShow(Player player, Player canSee, boolean isAllowed) {
		DisguiseCraftAPI api = DisguiseCraft.getAPI();
		return !api.isDisguised(player);
	}
	
	@Override
	public String getHookName() {
		return "DisguiseCraft";
	}

	@Override
	public HookPurpose[] getSupportedMethods() {
		return new HookPurpose[]{HookPurpose.LISTENER, HookPurpose.ALLOW_UPDATE, HookPurpose.ALLOW_SHOW};
	}

	@Override
	public HookListener getListener() {
		return listener;
	}
	
	public DisguiseCraftHook(){
		DisguiseCraft.getAPI(); // to fail in case.
	}

}
