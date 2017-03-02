package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Instrumentation;
import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Package;


public class InstrumentationGhost extends ComponentGhost implements Serializable{
	public InstrumentationInfoGhost info;
	public ArrayList<IntentInfoGhost> intents; 
	public InstrumentationGhost(Instrumentation c) {
		super(c);
		info = new InstrumentationInfoGhost(c.info);
		if (c.intents != null) {
			intents = new ArrayList<IntentInfoGhost>();
			for (int i = 0 ; i < c.intents.size(); i++) {
				IntentInfoGhost iig = new IntentInfoGhost((IntentInfo) c.intents.get(i));
				intents.add(iig);
			}
		}
	}
	
	public Instrumentation dumpFromGhost(Package pkg) {
		InstrumentationInfo cInfo = info.dumpFromGhost();
		Instrumentation c = new Instrumentation(pkg, cInfo);
		dumpFromGhost((Component) c);
		if (intents != null) {
			for (int i = 0 ; i < intents.size(); i++) {
				IntentInfo sii = intents.get(i).dumpFromGhost(null);
				c.intents.add(sii);
			}
		}
		return c;
	}

}
