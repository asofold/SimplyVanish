package asofold.simplyvanish.config.compatlayer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.util.config.Configuration;

@SuppressWarnings("deprecation")
public abstract class AbstractOldConfig extends AbstractConfig {
	File file = null;
	Configuration config = null;
	public AbstractOldConfig(File file){
		this.file = file;
		this.config = new Configuration(file);
	}
	
	@Override
	public boolean hasEntry(String path) {
		Object obj = config.getProperty(path);
		if (obj != null) return true;
		try{
			if ( config.getNode(path) != null ) return true;
		} catch (Throwable t){
			
		}
		return false;
	}
	




	@Override
	public String getString(String path, String defaultValue) {
		if (!hasEntry(path)) return defaultValue;
		return config.getString(path, defaultValue);
	}

	

	@Override
	public List<String> getStringKeys(String path) {
		// TODO policy: only strings or all keys as strings ?
		List<String> out = new LinkedList<String>();
		List<Object> keys = getKeys(path);
		if ( keys == null ) return out;
		for ( Object obj : keys){
			if ( obj instanceof String ) out.add((String) obj);
			else{
				try{
					out.add(obj.toString());
				} catch ( Throwable t){
					// ignore.
				}
			}
		}
		return out;
	}

	@Override
	public List<Object> getKeys(String path) {
		List<Object> out = new LinkedList<Object>();
		List<?> keys;
		if ( path == null) keys = config.getKeys();
		else keys = config.getKeys(path);
		if ( keys == null) return out;
		out.addAll(keys);
		return out;
	}
	
	@Override
	public List<Object> getKeys() {
		return getKeys(null);
	}

	@Override
	public Object getProperty(String path, Object defaultValue) {
		Object obj = config.getProperty(path);
		if ( obj  == null ) return defaultValue;
		else return obj;
	}

	@Override
	public List<String> getStringKeys() {
		return getStringKeys(null);
	}

	@Override
	public void setProperty(String path, Object obj) {
		config.setProperty(path, obj);
	}

	@Override
	public List<String> getStringList(String path, List<String> defaultValue) {
		if ( !hasEntry(path)) return defaultValue;
		List<String> out = new LinkedList<String>();
		List<String> entries = config.getStringList(path, null);
		if ( entries == null ) return defaultValue;
		for ( String entry : entries){
			if ( entry instanceof String) out.add(entry);
			else{
				try{
					out.add(entry.toString());
				} catch (Throwable t){
					// ignore
				}
			}
		}
		return out;
	}

	@Override
	public void removeProperty(String path) {
		config.removeProperty(path);
	}

}
