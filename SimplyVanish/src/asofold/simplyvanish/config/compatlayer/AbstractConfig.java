package asofold.simplyvanish.config.compatlayer;

import java.util.LinkedList;
import java.util.List;


/**
 * Some generic checks and stuff using getString, hasEntry, getStringList, ...
 * @author mc_dev
 *
 */
public abstract class AbstractConfig implements CompatConfig {

	@Override
	public Boolean getBoolean(String path, Boolean defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			// return Boolean.parseBoolean(val);
			String t = val.trim().toLowerCase();
			if ( t.equals("true")) return true;
			else if ( t.equals("false")) return false;
			else return defaultValue;
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}

	@Override
	public Double getDouble(String path, Double defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			return Double.parseDouble(val);
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}

	@Override
	public Long getLong(String path, Long defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			return Long.parseLong(val);
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}

	@Override
	public Integer getInt(String path, Integer defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			return Integer.parseInt(val);
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}
	
	@Override
	public List<Integer> getIntList(String path, List<Integer> defaultValue){
		if ( !hasEntry(path) ) return defaultValue;
		List<String> strings = getStringList(path, null);
		if ( strings == null ) return defaultValue;
		List<Integer> out = new LinkedList<Integer>();
		for ( String s : strings){
			try{
				out.add(Integer.parseInt(s));
			} catch(NumberFormatException exc){
				// ignore
			}
		}
		return out;
	}

	@Override
	public List<Double> getDoubleList(String path, List<Double> defaultValue) {
		if ( !hasEntry(path) ) return defaultValue;
		List<String> strings = getStringList(path, null);
		if ( strings == null ) return defaultValue;
		List<Double> out = new LinkedList<Double>();
		for ( String s : strings){
			try{
				out.add(Double.parseDouble(s));
			} catch(NumberFormatException exc){
				// ignore
			}
		}
		return out;
	}
	
	
}
