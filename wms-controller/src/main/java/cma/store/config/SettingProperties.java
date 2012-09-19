package cma.store.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import cma.store.serialization.FileSerializer;

public class SettingProperties {
	
	Map<String, PropertiesConfiguration> configMap = new HashMap<String, PropertiesConfiguration>();

	Logger log = Logger.getLogger(getClass());
	
	private static  SettingProperties settingProperties;
	
	
	public static SettingProperties getInstance(){
		if(settingProperties == null) {
			settingProperties = new SettingProperties();
		}
		return settingProperties;
	}
	
	public Object getValue(String file,String key) {
		if(configMap.containsKey(file)) {
			PropertiesConfiguration config = configMap.get(file);
			return config.getProperty(key);
		}else {
			PropertiesConfiguration config = new PropertiesConfiguration();		
			try {
				config = new PropertiesConfiguration(file);
				configMap.put(file, config);
			} catch (ConfigurationException e) {
				log.error("",e);
			}
			return config.getProperty(key);
		}
	}
}
