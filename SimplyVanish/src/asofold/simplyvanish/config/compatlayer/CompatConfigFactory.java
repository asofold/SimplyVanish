package asofold.simplyvanish.config.compatlayer;

import java.io.File;

public class CompatConfigFactory {
	
	public static final String version = "0.2.0";
	
	/**
	 * Attempt to get a working file configuration.
	 * Use getDBConfig to use this with a database.
	 * @param file
	 * @return null if fails.
	 */
	public static final CompatConfig getConfig(File file){
		CompatConfig out = null;
		// TODO: add more (latest API)
		try{
			return new OldConfig(file);
		} catch (Throwable t){
		}
		try{
			return new NewConfig(file);
		} catch (Throwable t){
			
		}
		return out;
	}
	
	public static final CompatConfig getOldConfig(File file){
		return new OldConfig(file);
	}
	
	public static final CompatConfig getNewConfig(File file){
		return new NewConfig(file);
	}
	
//	/**
//	 * Get a ebeans based database config (!).
//	 * @param file
//	 * @return
//	 */
//	public static final CompatConfig getDBConfig(EbeanServer server, String dbKey){
//		try{
//			return new SnakeDBConfig(server, dbKey);
//		} catch (Throwable t){
//			
//		}
//		return new DBConfig(server, dbKey);
//	}
	
	
	// TODO: add setup helpers (!)
	// TODO: add conversion helpers (!)
}
