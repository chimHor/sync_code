package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PermissionInfo;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Permission;
import android.content.pm.PackageParser.Package;

public class PermissionGhost extends ComponentGhost implements Serializable{
    public PermissionInfoGhost info;
    public boolean tree;
    
	public PermissionGhost(Permission c) {
		super(c);
		info = new PermissionInfoGhost(c.info);
		tree = c.tree;
	}
	public Permission dumpFromGhost(Package pkg) {
		PermissionInfo _info = info.dumpFromGhost(null);
		Permission perm = new Permission(pkg, _info);
		dumpFromGhost((Component) perm);
		return perm;
	}
}
