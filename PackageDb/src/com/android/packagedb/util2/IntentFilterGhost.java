package com.android.packagedb.util2;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.IntentFilter;
import android.content.IntentFilter.AuthorityEntry;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.PatternMatcher;
import android.util.Log;

public class IntentFilterGhost  implements Serializable {
	public int mPriority;
	public ArrayList<String> mActions = new ArrayList<String>();
	public ArrayList<String> mCategories = null;
	public ArrayList<String> mDataSchemes = null;
	public ArrayList<PatternMatcherGhost> mDataSchemeSpecificParts = null;
	public ArrayList<AuthorityEntryGhost> mDataAuthorities = null;
	public ArrayList<PatternMatcherGhost> mDataPaths = null;
	public ArrayList<String> mDataTypes = null;
	public boolean mHasPartialTypes;
    
    public IntentFilterGhost(IntentFilter iF) {
    	mPriority = iF.getPriority();
    	for (int i = 0 ; i < iF.countActions(); i++) {
    		mActions.add(iF.getAction(i));
    	}
    	if (iF.countCategories() > 0) {
    		mCategories = new ArrayList<String>();
    		for (int i = 0; i < iF.countCategories(); i ++) {
    			mCategories.add(iF.getCategory(i));
    		}
    	}
    	if (iF.countDataSchemes() > 0) {
    		mDataSchemes = new ArrayList<String>();
    		for (int i = 0; i < iF.countDataSchemes(); i ++) {
    			mDataSchemes.add(iF.getDataScheme(i));
    		}
    	}
    	if (iF.countDataTypes() > 0) {
    		mDataTypes = new ArrayList<String>();
    		for (int i = 0; i < iF.countDataTypes(); i ++) {
    			mDataTypes.add(iF.getDataType(i));
    		}
    	}
    	
    	if (iF.countDataSchemeSpecificParts() > 0) {
    		mDataSchemeSpecificParts = new ArrayList<PatternMatcherGhost>();
    		for (int i = 0; i < iF.countDataSchemeSpecificParts(); i++) {
    			PatternMatcherGhost pmg = new PatternMatcherGhost(iF.getDataSchemeSpecificPart(i));
    			mDataSchemeSpecificParts.add(pmg);
    		}
    	}
    	if (iF.countDataPaths() > 0) {
    		mDataPaths = new ArrayList<PatternMatcherGhost>();
    		for (int i = 0; i < iF.countDataPaths(); i++) {
    			PatternMatcherGhost pmg = new PatternMatcherGhost(iF.getDataPath(i));
    			mDataPaths.add(pmg);
    		}
    	}
    	
    	if (iF.countDataAuthorities() > 0) {
    		mDataAuthorities = new ArrayList<AuthorityEntryGhost>();
    		for (int i = 0; i < iF.countDataAuthorities(); i++) {
    			AuthorityEntryGhost pmg = new AuthorityEntryGhost(iF.getDataAuthority(i));
    			mDataAuthorities.add(pmg);
    		}
    	}
    	mHasPartialTypes = iF._hasPartialTypes();
    }
    
    public IntentFilter dumpFromGhost(IntentFilter iF) {
    	if (iF == null) {
    		Log.e("IntentFilterGhost", " should not be here");
    		return iF;
    	}
    	iF.setPriority(mPriority);
    	if (mActions.size() > 0) {
    		for (String s : mActions) {
    			iF.addAction(s);
    		}
    	}
    	if (mCategories != null && mCategories.size()>0) {
    		for (String s : mCategories) {
    			iF.addCategory(s);
    		}
    	}
    	if (mDataSchemes != null && mDataSchemes.size()>0) {
    		for (String s : mDataSchemes) {
    			iF.addDataScheme(s);
    		}
    	}
    	if (mDataTypes != null && mDataTypes.size()>0) {
    		for (String s : mDataTypes) {
    			iF._addDataType(s);
    		}
    	}
    	
    	if (mDataSchemeSpecificParts != null && mDataSchemeSpecificParts.size() > 0 ) {
    		for (int i = 0; i < mDataSchemeSpecificParts.size(); i++) {
    			PatternMatcher pm = mDataSchemeSpecificParts.get(i).dumpFromGhost();
    			iF.addDataSchemeSpecificPart(pm);
    		}
    	}
    	if (mDataPaths != null && mDataPaths.size() > 0 ) {
    		for (int i = 0; i < mDataPaths.size(); i++) {
    			PatternMatcher pm = mDataPaths.get(i).dumpFromGhost();
    			iF.addDataPath(pm);
    		}
    	}
    	
    	if (mDataAuthorities != null && mDataAuthorities.size() > 0 ) {
    		for (int i = 0; i < mDataAuthorities.size(); i++) {
    			AuthorityEntry ae = mDataAuthorities.get(i).dumpFromGhost();
    			iF.addDataAuthority(ae);
    		}
    	}
    	iF._setHasPartialTypes(mHasPartialTypes);
    	return iF;
    }
    
}
