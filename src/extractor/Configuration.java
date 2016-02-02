package extractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
	
	  public Map<String,String> loadProperties(String filename) {

			Properties prop = new Properties();
			InputStream input = null;
			Map<String,String> returnVals = null;
			try {

				//String filename = "extractor/config.properties";
				input = getClass().getClassLoader().getResourceAsStream(filename);
				if (input == null) {
					System.out.println("Sorry, unable to find " + filename);
					return null;
				}

				prop.load(input);
				returnVals = new HashMap<String,String>();
				Enumeration<?> e = prop.propertyNames();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					String value = prop.getProperty(key);
					returnVals.put(key, value);
					System.out.println("Key : " + key + ", Value : " + value);
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return returnVals;
		  }


}
