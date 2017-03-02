package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.IntentInfo;

public class ActivityIntentInfoGhost extends IntentInfoGhost implements Serializable{
	public int activityRefId;
	public boolean forReceiver;
	public ActivityIntentInfoGhost(ActivityIntentInfo aii, ArrayList<Activity> activities,  ArrayList<Activity> receivers) {
		super(aii);
		if (activities != null) {
			for (int i = 0 ; i < activities.size();i++) {
				if (activities.get(i) == aii.activity) {
					activityRefId = i;
					forReceiver = false;
					return;
				}
			}
		} else if (receivers != null) {
			for (int i = 0 ; i < receivers.size();i++) {
				if (receivers.get(i) == aii.activity) {
					activityRefId = i;
					forReceiver = true;
					return;
				}
			}
		} else {
			activityRefId = -1;
			forReceiver =false;
		}
	}
	
	public ActivityIntentInfo dumpFromGhost(Activity activity) {
		ActivityIntentInfo aii = new ActivityIntentInfo(activity);
		dumpFromGhost((IntentInfo) aii);
		return aii;
	}
	
	public ActivityIntentInfo dumpFromGhost(ArrayList<Activity> activities, ArrayList<Activity> receivers) {
		if (forReceiver) {
			ActivityIntentInfo aii = new ActivityIntentInfo(receivers.get(activityRefId));
			dumpFromGhost((IntentInfo) aii);
			return aii;
		} else {
			ActivityIntentInfo aii = new ActivityIntentInfo(activities.get(activityRefId));
			dumpFromGhost((IntentInfo) aii);
			return aii;
		}
	}
}
