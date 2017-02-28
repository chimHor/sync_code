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

public class ApplicationInfoGhost implements Serializable {
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
    public int enabledSetting = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
    public int installLocation = PackageInfo.INSTALL_LOCATION_UNSPECIFIED;

    public ApplicationInfo toApplictionInfo() {
        taskAffinity = source.readString();
        permission = source.readString();
        processName = source.readString();
        className = source.readString();
        theme = source.readInt();
        flags = source.readInt();
        requiresSmallestWidthDp = source.readInt();
        compatibleWidthLimitDp = source.readInt();
        largestWidthLimitDp = source.readInt();
        scanSourceDir = source.readString();
        scanPublicSourceDir = source.readString();
        sourceDir = source.readString();
        publicSourceDir = source.readString();
        splitSourceDirs = source.readStringArray();
        splitPublicSourceDirs = source.readStringArray();
        nativeLibraryDir = source.readString();
        secondaryNativeLibraryDir = source.readString();
        nativeLibraryRootDir = source.readString();
        nativeLibraryRootRequiresIsa = source.readInt() != 0;
        primaryCpuAbi = source.readString();
        secondaryCpuAbi = source.readString();
        resourceDirs = source.readStringArray();
        seinfo = source.readString();
        sharedLibraryFiles = source.readStringArray();
        dataDir = source.readString();
        uid = source.readInt();
        targetSdkVersion = source.readInt();
        versionCode = source.readInt();
        enabled = source.readInt() != 0;
        enabledSetting = source.readInt();
        installLocation = source.readInt();
        manageSpaceActivityName = source.readString();
        backupAgentName = source.readString();
        descriptionRes = source.readInt();
        uiOptions = source.readInt();
    }


}
