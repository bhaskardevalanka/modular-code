package com.techvedika.harmonycvi.gateway.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationPropertyReader {
	
//	private static final String APPLICATION_FILE = "application.properties";
    private static final String PARAMETER_FILE = "parameter.properties";

//    private static Properties applicationProps = new Properties();
    private static Properties parameterProps = new Properties();

    static {
//        loadProperties(APPLICATION_FILE, applicationProps);
        loadProperties(PARAMETER_FILE, parameterProps);
    }

//    private static void loadProperties(String fileName, Properties props) {
//        try (InputStream input = new FileInputStream(fileName)) {
//            props.load(input);
//            System.out.println("Loaded properties from " + fileName);
//        } catch (IOException e) {
//            System.err.println("Could not load " + fileName + ": " + e.getMessage());
//        }
//    }
    
    private static void loadProperties(String fileName, Properties props) {
        try (InputStream input = ApplicationPropertyReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.err.println("Could not find " + fileName + " in classpath");
                return;
            }
            props.load(input);
            System.out.println("Loaded properties from " + fileName);
        } catch (IOException e) {
            System.err.println("Could not load " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Get property from parameter.properties
     */
    public static String getParameterProperty(String key) {
        return parameterProps.getProperty(key);
    }

}
