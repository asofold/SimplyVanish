package me.asofold.bpl.simplyvanish.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.asofold.bpl.simplyvanish.api.hooks.Hook;
import me.asofold.bpl.simplyvanish.api.hooks.HookListener;
import me.asofold.bpl.simplyvanish.api.hooks.HookPurpose;
import me.asofold.bpl.simplyvanish.config.VanishConfig;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;


/**
 * Auxiliary methods for hooks.
 * @author mc_dev
 *
 */
public class HookUtil {
	/**
	 * Hooks by purpose.
	 */
	private final Map<HookPurpose, List<Hook>> usedHooks = new HashMap<HookPurpose, List<Hook>>();
	
	private final Map<String, HookListener> usedHookListeners = new HashMap<String, HookListener>();
	
	/**
	 * Registered hooks by name.
	 */
	private final Map<String, Hook> registeredHooks = new HashMap<String, Hook>();
	
	private int maxHookId = 0;
	
	/**
	 * 
	 */
	public HookUtil(){
		init();
	}
	
	/**
	 * Ensures that a list for every use is in usedHooks.<br>
	 * NOTE: The LISTENER entry is in it too, though unused.
	 */
	private void init() {
		for (HookPurpose sup : HookPurpose.values()){
			usedHooks.put(sup, new LinkedList<Hook>());
		}
	}

	public boolean addHook(Hook hook) {
		boolean existed = removeHook(hook);
		try{
			String hookName = hook.getHookName();
			// add hook !
			registeredHooks.put(hookName, hook);
			HookPurpose[] supported = hook.getSupportedMethods();
			if (supported == null) supported = HookPurpose.values();
			boolean hasListener = false;
			for (HookPurpose sup : supported){
				getUsedHooks(sup).add(hook);
				if (sup == HookPurpose.LISTENER) hasListener = true;
			}
			if (hasListener){
				HookListener listener = hook.getListener();
				if (listener != null){
					PluginManager pm = Bukkit.getServer().getPluginManager();
					pm.registerEvents(listener, pm.getPlugin("SimplyVanish"));
					usedHookListeners.put(hookName, listener);
				}
			}
			System.out.println("[SimplyVanish] Add hook: "+hook.getHookName());
		} catch (Throwable t){
			Utils.warn("Disable hook ("+hook.getHookName()+") due to failure on registration: "+t.getMessage());
			t.printStackTrace();
			removeHook(hook);
		}
		return existed;
	}
	
	public boolean removeHook(Hook hook) {
		// TODO maybe also check for the hook itself.
		return removeHook(hook.getHookName());
	}
	
	public boolean removeHook(String hookName) {
		Hook hook = registeredHooks.remove(hookName);
		if (hook == null) return false;
		HookListener listener = usedHookListeners.remove(hookName);
		if (listener != null){
			try{
				if (!listener.unregisterEvents()) Utils.warn("HookListener ("+hookName+") returns failure on unregister.");
			} catch (Throwable t){
				Utils.warn("Failed to unregister HookListener ("+hookName+"): "+t.getMessage());
				t.printStackTrace();
			}
		}
		for (HookPurpose sup : usedHooks.keySet()){
			List<Hook> rem = new LinkedList<Hook>();
			List<Hook> present = getUsedHooks(sup);
			for (Hook ref : present){
				if (ref==hook || ref.getHookName().equals(hookName)) rem.add(ref); // equals unnecessary ?
			}
			present.removeAll(rem);
		}
		return true;
	}
	
	/**
	 * (Over cautious.)
	 * @param purpose
	 * @return
	 */
	public final List<Hook> getUsedHooks(final HookPurpose purpose){
		List<Hook> hooks = null;
		if (purpose != null) hooks = usedHooks.get(purpose);
		if (hooks == null) return new LinkedList<Hook>();
		return hooks;
	}
	
	public void removeAllHooks(){
		List<String> names = new LinkedList<String>();
		names.addAll(registeredHooks.keySet());
		for ( String name : names){
			removeHook(name);
			// TODO: maybe something more complete.
		}
		// safety: 
		usedHookListeners.clear();
		usedHooks.clear();
		registeredHooks.clear();
		init();
	}
	
	
	// CALL METHODS ----------------------------------------
	
	public void onHookCallError(HookPurpose sup, Hook hook, String playerName, Throwable t) {
		String msg;
		if (t==null) msg = "<unknown>";
		else msg = t.getMessage();
		Utils.warn("Error on calling "+sup+" on hook("+hook.getHookName()+") for player "+playerName+": "+msg);
		if (t!= null) t.printStackTrace();
	}
	
	public final void callBeforeVanish(final String playerName) {
		final HookPurpose sup = HookPurpose.AFTER_VANISH;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				hook.beforeVanish(playerName);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}
	
	public final void callAfterVanish(final String playerName) {
		final HookPurpose sup = HookPurpose.AFTER_VANISH;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				hook.afterVanish(playerName);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}

	public final void callBeforeSetFlags(final String playerName, final VanishConfig oldCfg, final VanishConfig newCfg) {
		final HookPurpose sup = HookPurpose.BEFORE_SETFLAGS;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				hook.beforeSetFlags(playerName, oldCfg, newCfg);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}

	public final void callAfterSetFlags(final String playerName) {
		final HookPurpose sup = HookPurpose.AFTER_SETFLAGS;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				hook.afterSetFlags(playerName);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}

	public final void callBeforeReappear(final String playerName) {
		final HookPurpose sup = HookPurpose.BEFORE_REAPPEAR;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				hook.beforeReappear(playerName);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}

	public final void callAfterReappear(final String playerName) {
		final HookPurpose sup = HookPurpose.AFTER_REAPPEAR;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				hook.afterSetFlags(playerName);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}
	
	public final boolean allowUpdateVanishState(final Player player, final int hookId) {
		final HookPurpose sup = HookPurpose.ALLOW_UPDATE;
		boolean allow = true;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				allow &= hook.allowUpdateVanishState(player, hookId, allow); 
			} catch (Throwable t){
				onHookCallError(sup, hook, player.getName(), t);
			}
		}
		return allow;
	}
	
	public final boolean allowShow(final Player player, final Player canSee) {
		final HookPurpose sup = HookPurpose.ALLOW_SHOW;
		boolean allow = true;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				allow &= hook.allowShow(player, canSee, allow);
			} catch (Throwable t){
				onHookCallError(sup, hook, player.getName(), t);
			}
		}
		return allow;
	}
	
	public final boolean allowHide(final Player player, final Player canNotSee) {
		final HookPurpose sup = HookPurpose.ALLOW_HIDE;
		boolean allow = true;
		for (final Hook hook : getUsedHooks(sup)){
			try{
				allow &= hook.allowShow(player, canNotSee, allow);
			} catch (Throwable t){
				onHookCallError(sup, hook, player.getName(), t);
			}
		}
		return allow;
	}

	public Hook getHook(String name) {
		return registeredHooks.get(name);
	}

	public int getNewHookId() {
		maxHookId++;
		return maxHookId;
	}

}
