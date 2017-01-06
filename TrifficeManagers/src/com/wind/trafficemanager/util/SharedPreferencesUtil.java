package com.wind.trafficemanager.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SharedPreferencesUtil
{

	
	public static  void saveInfo(SharedPreferences sp, String key, Map<Integer, Boolean> map) {
	        Iterator<Entry<Integer, Boolean>> iterator = map.entrySet().iterator();
	 
	        JSONObject object = new JSONObject();
	 
	        while (iterator.hasNext()) {
	            Entry<Integer, Boolean> entry = iterator.next();
	            try {
	                object.put(String.valueOf(entry.getKey()), entry.getValue());
	            } catch (Exception e) {
	            	Log.i("xb", "excptions");
	            }
	        }
	    Editor editor = sp.edit();
	    editor.putString(key, object.toString());
	    editor.commit();
	}
	 
	public static  Map<Integer, Boolean> getInfo(SharedPreferences sp, String key) {
	    Map<Integer, Boolean> itemMap = new HashMap<Integer, Boolean>();
	    String result = sp.getString(key, "");
	    if(!result.equals(""))
	    {
	    try {
	    	JSONObject jobject = new JSONObject(result);
	            JSONArray names = jobject.names();
	            if (names != null) {
	                for (int j = 0; j < names.length(); j++) {
	                    //int name = Integer.valueOf(names.getString(j));
	                	String name = names.getString(j);
	                	Boolean value =Boolean.valueOf( jobject.getString(name));
	                    itemMap.put(Integer.valueOf(names.getString(j)), value);
	                }
	            }
	    }catch (Exception e) {
	    	Log.i("xb", "excption");
	    }
	    }
	    return itemMap;
}
	}
