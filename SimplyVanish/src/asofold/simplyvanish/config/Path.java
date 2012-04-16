package asofold.simplyvanish.config;


public class Path {
	public final char sep;
	
	public final String pickup;
	public final String exp;
	public final String expWorkaround;
	public String expDistance;
	public final String expThreshold;
	public final String  expTeleDist;
	public final String  expKillDist;
	public final String  expVelocity;
	public final String  expEnabled;

	public final String messages;
	public final String msgSuppress;
	public final String suppressJoinMessage;
	public final String suppressQuitMessage;
	public final String msgFake;
	public final String sendFakeMessages;
	public final String fakeJoinMessage;
	public final String fakeQuitMessage;
	public final String msgNotify;
	public final String msgPing;
	public final String notifyState;
	public final String notifyStateEnabled ;
	public final String notifyStatePerm;
	
	public final String panic;
	public final String  panicKickAll;
	public final String  panicKickInvolved;
	public final String  panicKickMessage;
	public final String  panicMessage;
	public final String  panicMessageTargets;
	public final String  panicRunCommand;
	public final String  panicCommand;
	
	public final String  saveVanished;
	public final String  saveVanishedAlways;
	public final String  saveVanishedInterval;
	
	public final String autoVanish;
	public final String  autoVanishUse;
	public final String  autoVanishPerm;
	
	public final String  noAbort;
	
	public final String  pingEnabled;
	/**
	 * Stored in milliseconds, read from config as seconds.
	 */
	public final String  pingPeriod;

	
	public final String permRoot;
	public final String  allowOps;
	public final String superperms;
	public final String permSets;
	
	public final String keyPerms;
	public final String keyPlayers;


	public final String addExtended;

	public Path(char sep){
		this.sep = sep;
		pickup = "pickup";
		exp = pickup + sep + "exp";
		expWorkaround = exp + sep + "workaround";
		expDistance = expWorkaround + sep + "distance";
		expThreshold = expDistance + sep + "threshold";
		expTeleDist = expDistance + sep + "teleport";
		expKillDist = expDistance + sep + "remove";

	    expVelocity = expWorkaround + sep + "velocity";
		
		expEnabled = expWorkaround + sep + "enabled";

		messages = "messages";
		msgSuppress = "messages" + sep + "suppress";
		suppressJoinMessage = msgSuppress + sep + "join";
		suppressQuitMessage = msgSuppress + sep + "quit";

		msgFake = messages + sep + "fake";
		sendFakeMessages = msgFake + sep + "enabled";
		fakeJoinMessage = msgFake + sep + "join";
		fakeQuitMessage = msgFake + sep + "quit";
		
		msgNotify = messages + sep + "notify";
		notifyState = msgNotify + sep + "state";
		notifyStateEnabled = notifyState + sep + "enabled";
		notifyStatePerm = notifyState + sep + "permission";
		msgPing = msgNotify + sep + "ping";
		pingEnabled = msgPing + sep + "enabled";
		pingPeriod = msgPing + sep + "period";
		
		panic = "panic";
		panicKickAll = panic + sep + "kick-all";
		panicKickInvolved = panic + sep + "kick-involved";
		panicKickMessage = panic + sep + "kick-message";
		panicMessage = panic + sep + "message";
		panicMessageTargets = panic + sep + "message-targets";
		panicRunCommand = panic + sep + "run-command";
		panicCommand = panic + sep + "command";
		
		// TODO: save vanished section	
		saveVanished = "save-vanished";
		saveVanishedAlways = "save-vanished-always";
		saveVanishedInterval = "save-vanished-interval";
		
		autoVanish = "auto-vanish";
		autoVanishUse = autoVanish + sep + "use";
		autoVanishPerm = autoVanish + sep + "permission";
		
		noAbort = "no-abort";
		
	
		permRoot = "permissions";
		allowOps = permRoot + sep + "allow-ops";
		superperms = permRoot + sep + "superperms";
		permSets = permRoot + sep + "fake-permissions";

		keyPerms = "permissions";
		keyPlayers = "players";
		
		addExtended = "extended-configuration";
	}
}
