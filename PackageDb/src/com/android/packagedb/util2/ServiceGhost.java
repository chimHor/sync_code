package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageParser.ServiceIntentInfo;
import android.content.pm.ServiceInfo;

public class ServiceGhost extends ComponentGhost implements Serializable{
	public ServiceInfoGhost info;
	public ArrayList<ServiceIntentInfoGhost> intents; 
	public ServiceGhost(Service s) {
		super(s);
		info = new ServiceInfoGhost(s.info);
		if (s.intents != null) {
			intents = new ArrayList<ServiceIntentInfoGhost>();
			for (int i = 0 ; i < s.intents.size(); i++) {
				ServiceIntentInfoGhost siig = new ServiceIntentInfoGhost(s.intents.get(i));
				intents.add(siig);
			}
		}
	}
	public Service dumpFromGhost(Package pkg) {
		ServiceInfo sInfo = info.dumpFromGhost(pkg.applicationInfo);
		Service s = new Service(pkg, sInfo);
		dumpFromGhost((Component) s);
		if (intents != null) {
			for (int i = 0 ; i < intents.size(); i++) {
				ServiceIntentInfo sii = intents.get(i).dumpFromGhost(s);
				s.intents.add(sii);
			}
		}
		return s;
	}
	
}
