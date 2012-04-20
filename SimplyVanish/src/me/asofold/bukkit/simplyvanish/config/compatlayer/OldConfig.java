package me.asofold.bukkit.simplyvanish.config.compatlayer;

import java.io.File;
import java.util.Map;


@SuppressWarnings("deprecation")
public class OldConfig extends AbstractOldConfig{
	public OldConfig(File file) {
		super(file);
	}
	@Override
	public void load(){
		config.load();
	}
	@Override
	public boolean save(){
		return config.save();
	}
	@Override
	public Map<String, Object> getValuesDeep() {
		Map<String, Object> all = config.getAll();
		// TODO: maybe check for sub nodes ? ... CHECK IT!
		return all;
	}

}
