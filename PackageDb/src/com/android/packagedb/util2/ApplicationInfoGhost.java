package com.android.packagedb.util2;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;

import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureGroupInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.ManifestDigest;
import android.content.pm.PackageItemInfo;
import android.content.pm.Signature;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Instrumentation;
import android.content.pm.PackageParser.Permission;
import android.content.pm.PackageParser.PermissionGroup;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageParser.Package;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;

public class ApplicationInfoGhost extends PackageItemInfoGhost implements Serializable {
    public String taskAffinity;
    public String permission;
    public String processName;
    public String className;
    public int descriptionRes;
    public int theme;
    public String manageSpaceActivityName;
    public String backupAgentName;
    public int uiOptions = 0;
    public int flags = 0;
    public int requiresSmallestWidthDp = 0;
    public int compatibleWidthLimitDp = 0;
    public int largestWidthLimitDp = 0;
    public String scanSourceDir;
    public String scanPublicSourceDir;
    public String sourceDir;
    public String publicSourceDir;
    public String[] splitSourceDirs;
    public String[] splitPublicSourceDirs;
    public String[] resourceDirs;
    public String seinfo;
    public String[] sharedLibraryFiles;
    public String dataDir;
    public String nativeLibraryDir;
    public String secondaryNativeLibraryDir;
    public String nativeLibraryRootDir;
    public boolean nativeLibraryRootRequiresIsa;
    public String primaryCpuAbi;
    public String secondaryCpuAbi;
    public int uid;
    public int targetSdkVersion;
    public int versionCode;
    public boolean enabled = true;
    public int enabledSetting = 0;
    public int installLocation = 0;
    
    public ApplicationInfoGhost(ApplicationInfo aInfo) {
    	super(aInfo);
    	taskAffinity = aInfo.taskAffinity;
    	permission = aInfo.permission;
    	processName = aInfo.processName;
    	className = aInfo.className;
    	theme = aInfo.theme;
    	flags = aInfo.flags;
    	requiresSmallestWidthDp = aInfo.requiresSmallestWidthDp;
    	compatibleWidthLimitDp = aInfo.compatibleWidthLimitDp;
    	largestWidthLimitDp = aInfo.largestWidthLimitDp;
    	scanSourceDir = aInfo.scanSourceDir;
    	scanPublicSourceDir = aInfo.scanPublicSourceDir;
    	sourceDir = aInfo.sourceDir;
    	publicSourceDir = aInfo.publicSourceDir;
    	splitSourceDirs = aInfo.splitSourceDirs;
    	splitPublicSourceDirs = aInfo.splitPublicSourceDirs;
    	nativeLibraryDir = aInfo.nativeLibraryDir;
    	secondaryNativeLibraryDir = aInfo.secondaryNativeLibraryDir;
    	nativeLibraryRootDir = aInfo.nativeLibraryRootDir;
    	nativeLibraryRootRequiresIsa = aInfo.nativeLibraryRootRequiresIsa;
    	primaryCpuAbi = aInfo.primaryCpuAbi;
    	secondaryCpuAbi = aInfo.secondaryCpuAbi;
    	resourceDirs = aInfo.resourceDirs;
    	seinfo = aInfo.seinfo;
    	sharedLibraryFiles = aInfo.sharedLibraryFiles;
    	dataDir = aInfo.dataDir;
    	uid = aInfo.uid;
    	targetSdkVersion = aInfo.targetSdkVersion;
    	versionCode = aInfo.versionCode;
    	enabled = aInfo.enabled;
    	enabledSetting = aInfo.enabledSetting;
    	installLocation = aInfo.installLocation;
    	manageSpaceActivityName = aInfo.manageSpaceActivityName;
    	backupAgentName = aInfo.backupAgentName;
    	descriptionRes = aInfo.descriptionRes;
    	uiOptions = aInfo.uiOptions;
    }
    

    public ApplicationInfo dumpFromGhost(ApplicationInfo aInfo) {
    	dumpFromGhost((PackageItemInfo)aInfo);
    	aInfo.taskAffinity = taskAffinity;
    	aInfo.permission = permission;
    	aInfo.processName = processName;
    	aInfo.className = className;
    	aInfo.theme = theme;
    	aInfo.flags = flags;
    	aInfo.requiresSmallestWidthDp = requiresSmallestWidthDp;
    	aInfo.compatibleWidthLimitDp = compatibleWidthLimitDp;
    	aInfo.largestWidthLimitDp = largestWidthLimitDp;
    	aInfo.scanSourceDir = scanSourceDir;
    	aInfo.scanPublicSourceDir = scanPublicSourceDir;
    	aInfo.sourceDir = sourceDir;
    	aInfo.publicSourceDir = publicSourceDir;
    	aInfo.splitSourceDirs = splitSourceDirs;
    	aInfo.splitPublicSourceDirs = splitPublicSourceDirs;
    	aInfo.nativeLibraryDir = nativeLibraryDir;
    	aInfo.secondaryNativeLibraryDir = secondaryNativeLibraryDir;
    	aInfo.nativeLibraryRootDir = nativeLibraryRootDir;
    	aInfo.nativeLibraryRootRequiresIsa = nativeLibraryRootRequiresIsa;
    	aInfo.primaryCpuAbi = primaryCpuAbi;
    	aInfo.secondaryCpuAbi = secondaryCpuAbi;
    	aInfo.resourceDirs = resourceDirs;
    	aInfo.seinfo = seinfo;
    	aInfo.sharedLibraryFiles = sharedLibraryFiles;
    	aInfo.dataDir = dataDir;
    	aInfo.uid = uid;
    	aInfo.targetSdkVersion = targetSdkVersion;
    	aInfo.versionCode = versionCode;
    	aInfo.enabled = enabled;
    	aInfo.enabledSetting = enabledSetting;
    	aInfo.installLocation = installLocation;
    	aInfo.manageSpaceActivityName = manageSpaceActivityName;
    	aInfo.backupAgentName = backupAgentName;
    	aInfo.descriptionRes = descriptionRes;
    	aInfo.uiOptions = uiOptions;
    	return aInfo;
    }
}