package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Package;

public class ActivityGhost extends ComponentGhost implements Serializable {

	public ActivityInfoGhost info;
    public ArrayList<ActivityIntentInfoGhost> intents;
	public ActivityGhost(Activity c) {
		super(c);
		info = new ActivityInfoGhost(c.info);
		if (c.intents != null) {
			intents = new ArrayList<ActivityIntentInfoGhost>();
			for (int i = 0 ; i < c.intents.size(); i++) {
				ActivityIntentInfoGhost aiig = new ActivityIntentInfoGhost(c.intents.get(i), null, null);
				intents.add(aiig);
			}
		}
	}

	public Activity dumpFromGhost(Package pkg) {
		ActivityInfo aInfo = info.dumpFromGhost(pkg.applicationInfo);
		Activity a = new Activity(pkg, aInfo);
		dumpFromGhost((Component) a);
		if (intents != null) {
			for (int i = 0 ; i < intents.size(); i++) {
				ActivityIntentInfo aii = intents.get(i).dumpFromGhost(a);
				a.intents.add(aii);
			}
		}
		return a;
	}
	
}
