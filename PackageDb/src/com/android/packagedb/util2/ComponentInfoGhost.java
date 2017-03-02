package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageItemInfo;

public class ComponentInfoGhost extends PackageItemInfoGhost implements Serializable{

    public String processName;
    public int descriptionRes;
    public boolean enabled = true;
    public boolean exported = false;
	
    
	public ComponentInfoGhost(ComponentInfo info) {
		super(info);
		processName = info.processName;
		descriptionRes = info.descriptionRes;
		enabled = info.enabled;
		exported = info.exported;
	}
	
	public ComponentInfo dumpFromGhost(ComponentInfo ci , ApplicationInfo ai) {
		dumpFromGhost((PackageItemInfo)ci);
		ci.applicationInfo = ai;
		ci.processName = processName;
		ci.descriptionRes = descriptionRes;
		ci.enabled = enabled;
		ci.exported = exported;
		return ci;
	}
}
