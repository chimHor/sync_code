package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.ProviderIntentInfo;

public class ProviderIntentInfoGhost extends IntentInfoGhost implements Serializable {

	public ProviderIntentInfoGhost(IntentInfo ii) {
		super(ii);
		// TODO Auto-generated constructor stub
	}

	public ProviderIntentInfo dumpFromGhost(Provider p) {
		// TODO Auto-generated method stub
		ProviderIntentInfo pii = new ProviderIntentInfo(p);
		dumpFromGhost((IntentInfo)pii);
		return pii;
	}

}
