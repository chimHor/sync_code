package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PackageItemInfo;
import android.content.pm.PermissionInfo;

public class PermissionInfoGhost extends PackageItemInfoGhost implements Serializable{

	public int protectionLevel;
    public String group;
    public int flags;
    public int descriptionRes;
    public String nonLocalizedDescription;
    
    public PermissionInfoGhost(PermissionInfo info) {
		super(info);
		protectionLevel = info.protectionLevel;
		group = info.group;
		flags = info.flags;
		descriptionRes = info.descriptionRes;
		nonLocalizedDescription = (String) info.nonLocalizedDescription;
	}
    
    
    public PermissionInfo dumpFromGhost(PermissionInfo info) {
    	if (info == null) {
    		info = new PermissionInfo();
    	}
    	dumpFromGhost((PackageItemInfo)info);
    	info.protectionLevel = protectionLevel;
    	info.group = group;
    	info.flags = flags;
    	info.descriptionRes = descriptionRes;
    	info.nonLocalizedDescription = nonLocalizedDescription;
    	return info;
    }
    
}
