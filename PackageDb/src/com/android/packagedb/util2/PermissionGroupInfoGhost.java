package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PackageItemInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;

public class PermissionGroupInfoGhost extends PackageItemInfoGhost implements Serializable{

	public int descriptionRes;
    public String nonLocalizedDescription;
    public int flags;
    public int priority;
    
    public PermissionGroupInfoGhost(PermissionGroupInfo info) {
		super(info);
		descriptionRes = info.descriptionRes;
		nonLocalizedDescription = (String) info.nonLocalizedDescription;
		flags = info.flags;
		priority = info.priority;
	}
    
    public PermissionGroupInfo dumpFromGhost(PermissionGroupInfo info) {
    	if (info == null) {
    		info = new PermissionGroupInfo();
    	}
    	dumpFromGhost((PackageItemInfo)info);
    	info.descriptionRes = descriptionRes;
    	info.nonLocalizedDescription = nonLocalizedDescription;
    	info.flags = flags;
    	info.priority = priority;
    	return info;
    }
    
}
