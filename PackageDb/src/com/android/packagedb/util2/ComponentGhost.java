package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Package;
import android.os.Bundle;

public class ComponentGhost implements Serializable{
    //public Package owner;
    //public final ArrayList<II> intents;
    //public String className;
	 public BundleGhost metaData;
    
    public ComponentGhost(Component c) {
    	if (c.metaData != null) {
    		metaData = new BundleGhost(c.metaData);
    	}
    }
	
    public Component dumpFromGhost(Component c) {
    	if (metaData != null) {
    		c.metaData = metaData.dumpFromGhost(null);
    	}
    	return c;
    }
    
}
