package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PathPermission;
import android.os.PatternMatcher;

public class PathPermissionGhost extends PatternMatcherGhost implements Serializable{
    public String mReadPermission;
    public String mWritePermission;
	public PathPermissionGhost(PathPermission pm) {
		super(pm);
		mReadPermission = pm.getReadPermission();
		mWritePermission = pm.getWritePermission();
	}
	
    public PathPermission dumpFromGhost() {
    	PathPermission pm = new PathPermission(mPattern, mType, mReadPermission, mWritePermission);
    	return pm;
    }
}
