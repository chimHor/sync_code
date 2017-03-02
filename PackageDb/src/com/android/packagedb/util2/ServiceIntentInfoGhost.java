package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageParser.ServiceIntentInfo;

public class ServiceIntentInfoGhost extends IntentInfoGhost implements Serializable{

	public ServiceIntentInfoGhost(IntentInfo ii) {
		super(ii);
		// TODO Auto-generated constructor stub
	}

	public ServiceIntentInfo dumpFromGhost(Service s) {
		ServiceIntentInfo sii = new ServiceIntentInfo(s);
		dumpFromGhost((IntentInfo) sii);
		return sii;
	}
	
}
