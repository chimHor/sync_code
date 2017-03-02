package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;

public class ActivityInfoGhost extends ComponentInfoGhost implements Serializable{
	public int theme;
	public int launchMode;
	public int documentLaunchMode;
    public int persistableMode;
    public int maxRecents;
    public String permission;
    public String taskAffinity;
    public String targetActivity;
    public int flags;
    public int screenOrientation;
    public int configChanges;
	public int softInputMode;
	public int uiOptions;
	public String parentActivityName;
	
	public ActivityInfoGhost(ActivityInfo info) {
		super(info);
		theme = info.theme;
		launchMode = info.launchMode;
		documentLaunchMode= info.documentLaunchMode;
	    persistableMode = info.persistableMode;
	    maxRecents = info.maxRecents;
	    permission = info.permission;
	    taskAffinity = info.taskAffinity;
	    targetActivity = info.targetActivity;
	    flags = info.flags;
	    screenOrientation = info.screenOrientation;
	    configChanges = info.configChanges;
		softInputMode = info.softInputMode;
		uiOptions = info.uiOptions;
		parentActivityName = info.parentActivityName;
	}

	public ActivityInfo dumpFromGhost(ApplicationInfo ai) {
		ActivityInfo info = new ActivityInfo();
		dumpFromGhost((ComponentInfo) info, ai);
		info.theme = theme;
		info.launchMode = launchMode;
		info.documentLaunchMode= documentLaunchMode;
		info.persistableMode = persistableMode;
		info.maxRecents = maxRecents;
		info.permission = permission;
		info.taskAffinity = taskAffinity;
		info.targetActivity = targetActivity;
		info.flags = flags;
		info.screenOrientation = screenOrientation;
		info.configChanges = configChanges;
		info.softInputMode = softInputMode;
		info.uiOptions = uiOptions;
		info.parentActivityName = parentActivityName;
		return info;
	}
	
}
