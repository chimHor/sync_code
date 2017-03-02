package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.IntentFilter.AuthorityEntry;

public class AuthorityEntryGhost implements Serializable {
    public String mOrigHost;
    public String mPort;
    
    public AuthorityEntryGhost(AuthorityEntry ae) {
    	mOrigHost = ae.getHost();
    	mPort = Integer.toString(ae.getPort());
    }
    
    public AuthorityEntry dumpFromGhost() {
    	AuthorityEntry ae = new AuthorityEntry(mOrigHost, mPort);
    	return ae;
    }
}
