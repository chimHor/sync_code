package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.FeatureGroupInfo;
import android.content.pm.FeatureInfo;

public class FeatureGroupInfoGhost implements Serializable{
    public FeatureInfoGhost[] features;
    public FeatureGroupInfoGhost(FeatureGroupInfo fgi) {
    	if (fgi.features != null) {
    		features = new FeatureInfoGhost[fgi.features.length];
    		for (int i = 0; i < fgi.features.length; i++) {
    			features[i] = new FeatureInfoGhost(fgi.features[i]);
    		}
    	}
    }
    
    public FeatureGroupInfo dumpFromGhost() {
    	FeatureGroupInfo fgi = new FeatureGroupInfo();
    	if (features != null) {
    		fgi.features = new FeatureInfo[features.length];
    		for (int i = 0; i < features.length; i++) {
    			fgi.features[i] = features[i].dumpFromGhost();
    		}
    	}
    	return fgi;
    }
}
