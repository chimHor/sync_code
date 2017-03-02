package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.os.PatternMatcher;

public class ProviderInfoGhost extends ComponentInfoGhost implements Serializable {

    public String authority = null;
    public String readPermission = null;
    public String writePermission = null;
    public boolean grantUriPermissions = false;
    public PatternMatcherGhost[] uriPermissionPatterns = null;
    public PathPermissionGhost[] pathPermissions = null;
    public boolean multiprocess = false;
    public int initOrder = 0;
    public int flags = 0;
    public boolean isSyncable = false;

	public ProviderInfoGhost(ProviderInfo info) {
		super(info);
		authority = info.authority;
		readPermission = info.readPermission;
		writePermission = info.writePermission;
		grantUriPermissions = info.grantUriPermissions;
		multiprocess = info.multiprocess;
		initOrder = info.initOrder;
		flags = info.flags;
		isSyncable = info.isSyncable;
		if (info.uriPermissionPatterns != null) {
			uriPermissionPatterns = new PatternMatcherGhost[info.uriPermissionPatterns.length];
			for (int i = 0; i < info.uriPermissionPatterns.length; i++) {
				uriPermissionPatterns[i] = new PatternMatcherGhost(info.uriPermissionPatterns[i]);
			}
		}
		if (info.pathPermissions != null) {
			pathPermissions = new PathPermissionGhost[info.pathPermissions.length];
			for (int i = 0; i < info.pathPermissions.length; i++) {
				pathPermissions[i] = new PathPermissionGhost(info.pathPermissions[i]);
			}
		}
	}

	
	public ProviderInfo dumpFromGhost(ApplicationInfo ai) {
		ProviderInfo info = new ProviderInfo();
		dumpFromGhost((ComponentInfo)info, ai);
		info.authority = authority;
		info.readPermission = readPermission;
		info.writePermission = writePermission;
		info.grantUriPermissions = grantUriPermissions;
		info.multiprocess = multiprocess;
		info.initOrder = initOrder;
		info.flags = flags;
		info.isSyncable = isSyncable;
		if (uriPermissionPatterns != null) {
			info.uriPermissionPatterns = new PatternMatcher[uriPermissionPatterns.length];
			for (int i = 0; i < uriPermissionPatterns.length; i++) {
				info.uriPermissionPatterns[i] = uriPermissionPatterns[i].dumpFromGhost();
			}
		}
		if (pathPermissions != null) {
			info.pathPermissions = new PathPermission[pathPermissions.length];
			for (int i = 0; i < pathPermissions.length; i++) {
				info.pathPermissions[i] = pathPermissions[i].dumpFromGhost();
			}
		}
		return info;
	}
}
