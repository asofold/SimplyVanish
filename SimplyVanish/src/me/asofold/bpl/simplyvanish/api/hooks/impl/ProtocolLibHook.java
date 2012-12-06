package me.asofold.bpl.simplyvanish.api.hooks.impl;

import me.asofold.bpl.simplyvanish.SimplyVanish;
import me.asofold.bpl.simplyvanish.api.hooks.AbstractHook;
import me.asofold.bpl.simplyvanish.api.hooks.HookListener;
import me.asofold.bpl.simplyvanish.api.hooks.HookPurpose;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class ProtocolLibHook extends AbstractHook {

	private static final HookPurpose[] purpose = new HookPurpose[]{
//		HookPurpose.LISTENER,
//		HookPurpose.AFTER_VANISH, HookPurpose.AFTER_REAPPEAR,
//		HookPurpose.AFTER_SETFLAGS,
	};
	
	private final ProtocolManager protocolManager;
	
	public ProtocolLibHook(SimplyVanish plugin){
		protocolManager = ProtocolLibrary.getProtocolManager();
		throw new RuntimeException("not intended for use :)");
//		protocolManager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 0x1F, 0x22) {
//			@Override
//			public void onPacketSending(PacketEvent event) {
//				// Item packets
//				switch (event.getPacketID()) {
//				case 0x1F:
//					try {
//						System.out.println("RELMOVE: " + event.getPacket().getModifier().readSafely(0));
//					} catch (FieldAccessException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
////					event.setCancelled(true);
//					break;
//				case 0x22:
//					try {
//						System.out.println("TELEPORT: " + event.getPacket().getModifier().readSafely(0));
//					} catch (FieldAccessException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
////					event.setCancelled(true);
//					break;
//				}
//			}
//		});
	}

	@Override
	public String getHookName() {
		return "ProtocolLib";
	}

	@Override
	public HookPurpose[] getSupportedMethods() {
		return purpose;
	}

	@Override
	public HookListener getListener() {
		return new HookListener() {
			@Override
			public boolean unregisterEvents() {
				return false;
			}
			// TODO
		};
	}

	@Override
	public void afterVanish(String playerName) {
		// TODO
	}

	@Override
	public void afterReappear(String playerName) {
		// TODO
	}

	@Override
	public void afterSetFlags(String playerName) {
		// TODO
	}

}
