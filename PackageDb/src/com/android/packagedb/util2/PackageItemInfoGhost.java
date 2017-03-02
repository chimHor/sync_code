package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;

public class PackageItemInfoGhost implements Serializable{
    public String name;
    public String packageName;
    public int labelRes;
    public String nonLocalizedLabel;
    public int icon;
    public int banner;
    public int logo;
    //public Bundle metaData;
    public int metaData = -1;
    public int showUserIcon = UserHandle.USER_NULL;
    
    public PackageItemInfoGhost(PackageItemInfo info) {
    	name = info.name;
    	packageName = info.packageName;
    	labelRes = info.labelRes;
    	nonLocalizedLabel = (String) info.nonLocalizedLabel;
    	icon = info.icon;
    	banner = info.banner;
    	logo = info.logo;
    	if (info.metaData != null) {
    		metaData = Helper.getBundleRefId(info.metaData);
    	}
    	showUserIcon = info.showUserIcon;
    }
    
    public PackageItemInfo dumpFromGhost(PackageItemInfo info) {
    	if (info == null) {
    		return null;
    	}
    	info.name = name;
    	info.packageName = packageName;
    	info.labelRes = labelRes;
    	info.nonLocalizedLabel = nonLocalizedLabel;
    	info.icon = icon;
    	info.banner = banner;
    	info.logo = logo;
    	info.metaData = Helper.getBundleByRefId(metaData);
    	info.showUserIcon = showUserIcon;
    	return info;
    }
}
