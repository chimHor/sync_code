package com.android.packagedb.util2;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.os.BaseBundle;
import android.os.Bundle;
import android.util.ArrayMap;

public class BundleGhost implements Serializable{

	public static class Item implements Serializable {
		
	}
	
	public static class StringItem extends Item implements Serializable {
		public String key;
		public String value;
		public StringItem(String aKey, String aValue) {
			key = aKey;
			value = aValue;
		}
	}
	public static class IntItem extends Item implements Serializable {
		public String key;
		public int value;
		public IntItem(String aKey, Integer aValue) {
			key = aKey;
			value = aValue;
		}
	}
	public static class FloatItem extends Item implements Serializable {
		public String key;
		public float value;
		public FloatItem(String aKey, Float aValue) {
			key = aKey;
			value = aValue;
		}
	}
	public static class BooleanItem extends Item implements Serializable {
		public String key;
		public boolean value;
		public BooleanItem(String aKey, Boolean aValue) {
			key = aKey;
			value = aValue;
		}
	}
	
	ArrayList<Item> list = new ArrayList<Item>();
	
	static Field mapField;
	static {
		try {
			mapField = BaseBundle.class.getDeclaredField("mMap");
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mapField.setAccessible(true);
	}
	
	public BundleGhost(Bundle b) {
		if (b == null) {
			return;
		}
		ArrayMap<String, Object> bMap = null;
		try {
			bMap = (ArrayMap<String, Object>) mapField.get(b);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bMap == null) {
			return;
		}
        Set<String> keySet = bMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = bMap.get(key);
            if (value instanceof String) {
            	Item item = new StringItem(key, (String)value);
            	list.add(item);
            } else if (value instanceof Boolean) {
            	Item item = new BooleanItem(key, (Boolean)value);
            	list.add(item);
            } else if (value instanceof Integer) {
            	Item item = new IntItem(key, (Integer)value);
            	list.add(item);
            } else if (value instanceof Float) {
            	Item item = new FloatItem(key, (Float)value);
            	list.add(item);
            }
        }
	}
	
	public Bundle dumpFromGhost(Bundle b) {
		if (b == null) {
			b = new Bundle();
		}
		for (Item item : list) {
			if (item instanceof StringItem) {
				StringItem sItem = (StringItem) item;
				b.putString(sItem.key, sItem.value);
			} else if (item instanceof BooleanItem) {
				BooleanItem bItem = (BooleanItem) item;
				b.putBoolean(bItem.key, bItem.value);
			} else if (item instanceof IntItem) {
				IntItem iItem = (IntItem) item;
				b.putInt(iItem.key, iItem.value);
			} else if (item instanceof FloatItem) {
				FloatItem fItem = (FloatItem) item;
				b.putFloat(fItem.key, fItem.value);
			}
		}
		return b;
	}
	
}
