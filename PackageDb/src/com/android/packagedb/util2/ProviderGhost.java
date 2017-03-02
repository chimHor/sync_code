package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.ProviderIntentInfo;
import android.content.pm.ProviderInfo;

public class ProviderGhost extends ComponentGhost implements Serializable {
	public boolean syncable;
	public ProviderInfoGhost info;
	public ArrayList<ProviderIntentInfoGhost> intents;
	public ProviderGhost(Provider p) {
		super(p);
		syncable = p.syncable;
		info = new ProviderInfoGhost(p.info);
		if (p.intents != null) {
			intents = new ArrayList<ProviderIntentInfoGhost>();
			for (int i = 0 ; i < p.intents.size(); i++) {
				ProviderIntentInfoGhost piig = new ProviderIntentInfoGhost(p.intents.get(i));
				intents.add(piig);
			}
		}
	}
	
	public Provider dumpFromGhost(Package pkg) {
		ProviderInfo pInfo = info.dumpFromGhost(pkg.applicationInfo);
		Provider p = new Provider(pkg, pInfo);
		p.syncable = syncable;
		dumpFromGhost((Component) p);
		if (intents != null) {
			for (int i = 0 ; i < intents.size(); i++) {
				ProviderIntentInfo pii = intents.get(i).dumpFromGhost(p);
				p.intents.add(pii);
			}
		}
		return p;
	}

}
