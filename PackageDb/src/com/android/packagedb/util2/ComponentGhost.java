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
    public int  metaData;
    
    public ComponentGhost(Component c) {
    	metaData = Helper.getBundleRefId(c.metaData);
    }
	
    public Component dumpFromGhost(Component c) {
    	c.metaData = Helper.getBundleByRefId(metaData);
    	return c;
    }
    
}
