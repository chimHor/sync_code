package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.IntentFilter;
import android.content.pm.PackageParser.IntentInfo;
import android.util.Log;

public class IntentInfoGhost extends IntentFilterGhost implements Serializable {
	
    public boolean hasDefault;
    public int labelRes;
    public String nonLocalizedLabel;
    public int icon;
    public int logo;
    public int banner;
    public int preferred;

    public IntentInfoGhost(IntentInfo ii) {
    	super(ii);
    	hasDefault = ii.hasDefault;
    	labelRes = ii.labelRes;
    	nonLocalizedLabel = (String) ii.nonLocalizedLabel;
    	icon = ii.icon;
    	logo = ii.logo;
    	banner = ii.banner;
    	preferred = ii.preferred;
    	
    }
    
    
    public IntentInfo dumpFromGhost(IntentInfo ii) {
    	if (ii == null) {
    		//Log.e("IntentInfoGhost", " should not be here");
    		//return ii;
    		ii = new IntentInfo();
    	}
    	dumpFromGhost((IntentFilter)ii);
    	ii.hasDefault = hasDefault;
    	ii.labelRes = labelRes;
    	ii.nonLocalizedLabel = nonLocalizedLabel;
    	ii.icon = icon;
    	ii.logo = logo;
    	ii.banner = banner;
    	ii.preferred = preferred;
    	return ii;
    }
}
