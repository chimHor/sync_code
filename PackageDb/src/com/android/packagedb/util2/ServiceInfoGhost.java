package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ServiceInfo;

public class ServiceInfoGhost extends ComponentInfoGhost implements Serializable{
	public String permission;
	public int flags;
	public ServiceInfoGhost(ServiceInfo info) {
		super(info);
		permission = info.permission;
		flags = info.flags;
	}

	public ServiceInfo dumpFromGhost(ApplicationInfo ai) {
		ServiceInfo info = new ServiceInfo();
		dumpFromGhost((ComponentInfo) info, ai);
		info.permission = permission;
		info.flags = flags;
		return info;
	}
}
