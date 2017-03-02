package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.ConfigurationInfo;

public class ConfigurationInfoGhost implements Serializable{
    public int reqTouchScreen;
    public int reqKeyboardType;
    public int reqNavigation;
    public int reqInputFeatures;
    public int reqGlEsVersion;
	
    public ConfigurationInfoGhost(ConfigurationInfo info) {
    	reqTouchScreen = info.reqTouchScreen;
    	reqKeyboardType = info.reqKeyboardType;
    	reqNavigation = info.reqNavigation;
    	reqInputFeatures = info.reqInputFeatures;
    	reqGlEsVersion = info.reqGlEsVersion;
    }
	
    public ConfigurationInfo dumpFromGhost() {
    	ConfigurationInfo info = new ConfigurationInfo();
    	info.reqTouchScreen = reqTouchScreen;
    	info.reqKeyboardType = reqKeyboardType;
    	info.reqNavigation = reqNavigation;
    	info.reqInputFeatures = reqInputFeatures;
    	info.reqGlEsVersion = reqGlEsVersion;
    	return info;
    }
    
}
