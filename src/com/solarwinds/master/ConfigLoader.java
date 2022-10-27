package com.solarwinds.master;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
	
	public static Config config;

	public ConfigLoader(String path){
		try (InputStream input = new FileInputStream(path)) {

            Properties prop = new Properties(); 
            prop.load(input); 
            
            config = new Config();
            
            config.setBasecurrency(prop.getProperty("basecurrency"));
            config.setTraderApikey(prop.getProperty("traderApikey"));
            config.setTraderSecretkey(prop.getProperty("traderSecretkey"));
            config.setCopierApikeys(prop.getProperty("copierApikeys"));
            config.setCopierSecretkeys(prop.getProperty("copierSecretkeys"));
            config.setTradetype(prop.getProperty("tradetype"));
            config.setTrademode(prop.getProperty("trademode"));
            config.setTraderatio(Integer.parseInt(prop.getProperty("traderatio")));
            config.setTradefixed(Integer.parseInt(prop.getProperty("tradefixed"))); 
            config.setApiUrl(prop.getProperty("apiUrl"));
            config.setWsUrl(prop.getProperty("wsUrl"));
            

        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

}
