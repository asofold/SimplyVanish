package asofold.simplyvanish.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import asofold.simplyvanish.config.VanishConfig;
import asofold.simplyvanish.hooks.Hook;
import asofold.simplyvanish.hooks.HookListener;
import asofold.simplyvanish.hooks.HookPurpose;

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
	public List<Hook> getUsedHooks(HookPurpose purpose){
		
		
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
	
	public boolean callBeforeVanish(String playerName) {
		boolean res = true;
		HookPurpose sup = HookPurpose.AFTER_VANISH;
		for (Hook hook : getUsedHooks(sup)){
			try{
				if (!hook.beforeVanish(playerName)) res = false;
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
		return res;
	}
	
	public final void callAfterVanish(String playerName) {
		HookPurpose sup = HookPurpose.AFTER_VANISH;
		for (Hook hook : getUsedHooks(sup)){
			try{
				
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}

	public boolean callBeforeSetFlags(String playerName, VanishConfig oldCfg, VanishConfig newCfg) {
		boolean res = true;
		HookPurpose sup = HookPurpose.BEFORE_SETFLAGS;
		for (Hook hook : getUsedHooks(sup)){
			try{
				if (!hook.beforeSetFlags(playerName, oldCfg, newCfg)) res = false;
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
		return res;
	}

	public void callAfterSetFlags(String playerName) {
		HookPurpose sup = HookPurpose.AFTER_SETFLAGS;
		for (Hook hook : getUsedHooks(sup)){
			try{
				hook.afterSetFlags(playerName);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}

	public boolean callBeforeReappear(String playerName) {
		boolean res = true;
		HookPurpose sup = HookPurpose.BEFORE_REAPPEAR;
		for (Hook hook : getUsedHooks(sup)){
			try{
				if (!hook.beforeReappear(playerName)) res = false;
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
		return res;
	}

	public void callAfterReappear(String playerName) {
		HookPurpose sup = HookPurpose.AFTER_REAPPEAR;
		for (Hook hook : getUsedHooks(sup)){
			try{
				hook.afterSetFlags(playerName);
			} catch (Throwable t){
				onHookCallError(sup, hook, playerName, t);
			}
		}
	}


}
