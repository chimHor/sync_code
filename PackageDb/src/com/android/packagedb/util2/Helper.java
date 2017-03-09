package com.android.packagedb.util2;

import android.content.pm.PackageParser.Package;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Helper {
    public static <T> ArrayList<T> add(ArrayList<T> cur, ArrayList<T> addL) {
    	if (addL == null) {
    		return cur;
    	}
        if (cur == null) {
            cur = new ArrayList<T>();
        }
        cur.addAll(addL);
        return cur;
    }
    public static <T> ArraySet<T> add(ArraySet<T> cur, ArrayList<T> addL) {
    	if (addL == null) {
    		return cur;
    	}
        if (cur == null) {
            cur = new ArraySet<T>();
        }
        cur.addAll(addL);
        return cur;
    }
    
    public static <T> ArrayList<T> add(ArrayList<T> cur, ArraySet<T> addL) {
    	if (addL == null) {
    		return cur;
    	}
        if (cur == null) {
            cur = new ArrayList<T>();
        }
        cur.addAll(addL);
        return cur;
    }

    public static ArrayList<BundleGhost> bundleGhostList = new ArrayList<BundleGhost>();
    public static ArrayList<Bundle> bundleList = new ArrayList<Bundle>();
    
    public static int getBundleRefId(Bundle b){
    	if (b == null) {
    		return -1;
    	}
    	for (int i = 0; i < bundleList.size(); i ++) {
    		if (b == bundleList.get(i)) {
    			return i;
    		}
    	}
    	bundleList.add(b);
    	BundleGhost bg = new BundleGhost(b);
    	bundleGhostList.add(bg);
    	return bundleList.size()-1;
    }
    public static Bundle getBundleByRefId(int refId) {
    	if (refId < 0) {
    		return null;
    	}
    	if (bundleList.size()==0 && bundleGhostList.size() > 0) {
    		for(int i = 0; i < bundleGhostList.size(); i++) {
    			bundleList.add(bundleGhostList.get(i).dumpFromGhost(null));
    		}
    	}
    	if (refId >= bundleList.size()) {
    		Log.e("xxx", " should not be here");
    		return null;
    	}
    	return bundleList.get(refId);
    }
    
    
    public static void cleanBundle() {
    	bundleGhostList.clear();
    	bundleList.clear();
    }

    public static byte[] serializePackage(Package pkg) {
    	serializeBefore();
        try {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outBytes);
            PackageGhost pkgGhost = new PackageGhost(pkg);
            //out.writeObject(bundleGhostList);
            out.writeObject(pkgGhost);
            out.close();
            return outBytes.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	serializeAfter();
        }
        return null;
    }
    public static Package parsePackage(byte[] bytes) {
    	if (bytes == null) {
    		return null;
    	}
    	parseBefore();
        try {
        	long t1 = SystemClock.uptimeMillis();
            ByteArrayInputStream inBytes = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(inBytes);
            //bundleGhostList = (ArrayList<BundleGhost>)in.readObject();
            PackageGhost pkgGhost = (PackageGhost)in.readObject();
            long t2 = SystemClock.uptimeMillis();
            Package pkg = pkgGhost.dumpFromGhost();
            Log.e("xxx", pkg.packageName + " t1: "+ (t2-t1) + "  t2:"+ (SystemClock.uptimeMillis()-t2));
            return pkg;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	parseAfter();
        }
    	
    	return null;
    }
    
    public static Package testPackage(Package pkg) {
    	serializeBefore();
    	byte[] bytes;
        try {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outBytes);
            PackageGhost pkgGhost = new PackageGhost(pkg);
            //out.writeObject(bundleGhostList);
            out.writeObject(pkgGhost);
            out.close();
            bytes =outBytes.toByteArray();
            serializeAfter();
            parseBefore();
            ByteArrayInputStream inBytes = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(inBytes);
            //bundleGhostList = (ArrayList<BundleGhost>)in.readObject();
            PackageGhost pkgGhost2 = (PackageGhost)in.readObject();
            Package pkg2 = pkgGhost.dumpFromGhost();
            return pkg2;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	parseAfter();
        }
        return null;
    }
    
    public static void serializaPerfTest() {
    	byte[] bytes;
        try {
        	int count = 1000;
        	
        	{
	            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
	            ObjectOutputStream out = new ObjectOutputStream(outBytes);
	            for (int i = 0 ; i < count; i++) {
		            SerializableTestObj obj = SerializableTestObj.randomTestObj();
		            out.writeObject(obj);
	        	}
	            out.close();
	            bytes =outBytes.toByteArray();
	            ByteArrayInputStream inBytes = new ByteArrayInputStream(bytes);
	            ObjectInputStream in = new ObjectInputStream(inBytes);
	            long t1 = SystemClock.uptimeMillis();
	            for (int i = 0 ; i < count; i++) {
	            	SerializableTestObj obj2 = (SerializableTestObj) in.readObject();
	            }
	            long t2 = SystemClock.uptimeMillis();
	            Log.e("xxx", "SerializableTestObj : " + (t2-t1));
	            in.close();
        	}
        	
        	{
	            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
	            ObjectOutputStream out = new ObjectOutputStream(outBytes);
	            for (int i = 0 ; i < count; i++)  {
		            SerializableTestObj2 obj = SerializableTestObj2.randomTestObj(); 
		            out.writeObject(obj);
	            }
	            out.close();
	            bytes =outBytes.toByteArray();
	            ByteArrayInputStream inBytes = new ByteArrayInputStream(bytes);
	            ObjectInputStream in = new ObjectInputStream(inBytes);
	            long t1 = SystemClock.uptimeMillis();
	            for (int i = 0 ; i < count; i++) {
	            	SerializableTestObj2 obj2 = (SerializableTestObj2) in.readObject();
	            }
	            long t2 = SystemClock.uptimeMillis();
	            Log.e("xxx", "SerializableTestObj2 : " + (t2-t1));
	            in.close();
        	}
        	
        	
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void serializeBefore() {
    	cleanBundle();
    }
    public static void serializeAfter() {
    	cleanBundle();
    }
    public static void parseBefore() {
    	cleanBundle();
    }
    public static void parseAfter() {
    	cleanBundle();
    }

}
