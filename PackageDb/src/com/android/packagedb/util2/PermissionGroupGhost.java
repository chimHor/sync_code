package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PermissionGroupInfo;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PermissionGroup;

public class PermissionGroupGhost extends ComponentGhost implements Serializable{
	 public PermissionGroupInfoGhost info;
	 
	public PermissionGroupGhost(PermissionGroup c) {
		super(c);
		info = new PermissionGroupInfoGhost(c.info);
	}
	public PermissionGroup dumpFromGhost(Package pkg) {
		PermissionGroupInfo _info = info.dumpFromGhost(null);
		PermissionGroup perm = new PermissionGroup(pkg, _info);
		dumpFromGhost((Component) perm);
		return perm;
	}
}
