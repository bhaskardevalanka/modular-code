package com.techvedika.harmonycvi.gateway.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonUtils {
	
	public static JSONObject parseJsonString(String jsonString) throws Exception {
        JSONParser parser = new JSONParser();
        try {
            // Parse the string as a JSON object
            return (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new Exception("Error parsing JSON response", e);
        }
    }
	
	// Helper method to get value from JSON safely (check for null and non-existent fields)
    public static String getJsonFieldValue(JSONObject jsonResponse, String key) {
        if (jsonResponse.containsKey(key) && jsonResponse.get(key) != null) {
            return jsonResponse.get(key).toString();
        }
        return "";  // Default value when key is not found or value is null
    }
    
 // Overloaded method to provide a default value when the key is null
    public static String getJsonFieldValue(JSONObject jsonResponse, String key, String defaultValue) {
        if (jsonResponse.containsKey(key) && jsonResponse.get(key) != null) {
            return jsonResponse.get(key).toString();
        }
        return defaultValue;
    }
    
    // Overloaded method to provide a default value for Long fields
    public static Long getJsonFieldValueForLong(JSONObject jsonResponse, String key, Long defaultValue) {
        if (jsonResponse.containsKey(key) && jsonResponse.get(key) != null) {
            return Long.valueOf(jsonResponse.get(key).toString());
        }
        return defaultValue;
    }

    // Overloaded method to provide a default value for Boolean fields
    public static Boolean getJsonFieldValueForBoolean(JSONObject jsonResponse, String key, Boolean defaultValue) {
        if (jsonResponse.containsKey(key) && jsonResponse.get(key) != null) {
            return Boolean.valueOf(jsonResponse.get(key).toString());
        }
        return defaultValue;
    }

}
