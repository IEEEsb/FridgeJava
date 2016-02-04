package es.ieeesb.nevera.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class PropertiesManager.
 */
public class PropertiesManager {
	
	/** The Constant PROPERTIES_PATH. */
	private static final String PROPERTIES_PATH = "Connection.properties";
	
	/** The properties. */
	public static Properties properties;
	
	/**
	 * Load properties.
	 */
	public static void loadProperties() {
		properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream( PROPERTIES_PATH );
			properties.load( input );
			input.close();
		} catch (IOException e) {
			System.err.println("No se pudo leer el fichero .properties");
			System.exit( -1 );
		} 		
	}
}
