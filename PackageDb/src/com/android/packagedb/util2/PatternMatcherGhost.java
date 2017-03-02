package com.android.packagedb.util2;

import java.io.Serializable;

import android.os.PatternMatcher;

public class PatternMatcherGhost implements Serializable {
    public String mPattern;
    public int mType;
    public PatternMatcherGhost(PatternMatcher pm) {
    	mPattern = pm.getPath();
    	mType = pm.getType();
    }
    
    public PatternMatcher dumpFromGhost() {
    	PatternMatcher pm = new PatternMatcher(mPattern, mType);
    	return pm;
    }
}
