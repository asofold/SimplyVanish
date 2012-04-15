package asofold.simplyvanish.config.compatlayer;

public class ConfigUtil {
	
	public static final int canaryInt = Integer.MIN_VALUE +7;
	public static final long canaryLong = Long.MIN_VALUE + 7L;
	public static final double canaryDouble = Double.MIN_VALUE*.7;
	
	public static String stringPath( String path ){
		String[] split = path.split("\\.");
		StringBuilder builder = new StringBuilder();
		builder.append(stringPart(split[0]));
		for (int i = 1; i<split.length; i++){
			builder.append("."+stringPart(split[i]));
		}
		return builder.toString();
	}
	
	public static String bestPath(CompatConfig cfg, String path){
		String[] split = path.split("\\.");
		String res;
		if (cfg.hasEntry(split[0]) )res = split[0];
		else{
			res = stringPart(split[0]);
			if ( !cfg.hasEntry(res)) return path;
		}
		for (int i = 1; i<split.length; i++){
			if (cfg.hasEntry(res+"."+split[i]) ) res += "."+split[i];
			else{
				res += "."+stringPart(split[i]);
				if ( !cfg.hasEntry(res)) return path;
			}
		}
		return res;
	}
	
	public static String stringPart( String input){
		try{
			Double.parseDouble(input);
			return "'"+input+"'";
		} catch (NumberFormatException e){
		}
		try{
			Long.parseLong(input);
			return "'"+input+"'";
		} catch (NumberFormatException e){
		}
		try{
			Integer.parseInt(input);
			return "'"+input+"'";
		} catch (NumberFormatException e){
		}
		return input;
	}
}
