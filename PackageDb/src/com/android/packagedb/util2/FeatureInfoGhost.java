package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.FeatureInfo;

public class FeatureInfoGhost implements Serializable{
    public String name;
    public int reqGlEsVersion;
    public int flags;
    
    public FeatureInfoGhost(FeatureInfo fi) {
    	name = fi.name;
    	reqGlEsVersion = fi.reqGlEsVersion;
    	flags = fi.flags;
    }
    
    public FeatureInfo dumpFromGhost() {
    	FeatureInfo fi= new FeatureInfo();
    	fi.name = name;
    	fi.reqGlEsVersion = reqGlEsVersion;
    	fi.flags = flags;
    	return fi;
    }
}
