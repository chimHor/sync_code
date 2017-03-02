package com.android.packagedb.util2;

import java.io.Serializable;

import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageItemInfo;

public class InstrumentationInfoGhost extends PackageItemInfoGhost implements Serializable{

    public String targetPackage;
    public String sourceDir;
    public String publicSourceDir;
    public String[] splitSourceDirs;
    public String[] splitPublicSourceDirs;
    public String dataDir;
    public String nativeLibraryDir;
    public boolean handleProfiling;
    public boolean functionalTest;
	
	public InstrumentationInfoGhost(InstrumentationInfo info) {
		super(info);
	    targetPackage = info.targetPackage;
	    sourceDir = info.sourceDir;
	    publicSourceDir = info.publicSourceDir;
	    splitSourceDirs = info.splitSourceDirs;
	    splitPublicSourceDirs = info.splitPublicSourceDirs;
	    dataDir = info.dataDir;
	    nativeLibraryDir = info.nativeLibraryDir;
	    handleProfiling = info.handleProfiling;
	    functionalTest = info.functionalTest;
	}

	
	public InstrumentationInfo dumpFromGhost() {
		InstrumentationInfo info = new InstrumentationInfo();
		dumpFromGhost((PackageItemInfo)info);
	    info.targetPackage = targetPackage;
	    info.sourceDir = sourceDir;
	    info.publicSourceDir = publicSourceDir;
	    info.splitSourceDirs = splitSourceDirs;
	    info.splitPublicSourceDirs = splitPublicSourceDirs;
	    info.dataDir = dataDir;
	    info.nativeLibraryDir = nativeLibraryDir;
	    info.handleProfiling = handleProfiling;
	    info.functionalTest = functionalTest;
		return info;
	}
	
}
