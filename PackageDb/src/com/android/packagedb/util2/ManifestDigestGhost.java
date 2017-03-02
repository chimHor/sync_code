package com.android.packagedb.util2;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import android.content.pm.ManifestDigest;



public class ManifestDigestGhost implements Serializable {
    static Constructor t = null;
    static Field f = null;
    {
        try {
        Class c = android.content.pm.ManifestDigest.class;
        f = c.getDeclaredField("mDigest");
        if (f != null) {
            f.setAccessible(true);
        }
        t = c.getDeclaredConstructor(byte[].class);
        if (t != null) {
            t.setAccessible(true);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public byte[] content;
    
    
    public ManifestDigestGhost(ManifestDigest md) {
        try{
        	content = (byte[])f.get(md);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ManifestDigest dumpFromGhost(){
    	ManifestDigest m = null;
		try {
			m = (ManifestDigest) t.newInstance(content);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return m;
    }
}
