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

public class PackageGhost implements Serializable {
	//0
    public String packageName;
    public String[] splitNames;
    public String codePath;
    public String baseCodePath;
    public String[] splitCodePaths;
    public int baseRevisionCode;
    public int[] splitRevisionCodes;
    public int[] splitFlags;
    public boolean baseHardwareAccelerated;
    
    //1
    public final ApplicationInfo applicationInfo = new ApplicationInfo();
    public final ArrayList<Permission> permissions = new ArrayList<Permission>(0);
    public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>(0);
    public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
    public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
    public final ArrayList<Provider> providers = new ArrayList<Provider>(0);
    public final ArrayList<Service> services = new ArrayList<Service>(0);
    public final ArrayList<Instrumentation> instrumentation = new ArrayList<Instrumentation>(0);
    
    //2
    public final ArrayList<String> requestedPermissions = new ArrayList<String>();
    public final ArrayList<Boolean> requestedPermissionsRequired = new ArrayList<Boolean>();
    public ArrayList<String> protectedBroadcasts;
    public ArrayList<String> libraryNames = null;
    public ArrayList<String> usesLibraries = null;
    public ArrayList<String> usesOptionalLibraries = null;
    public String[] usesLibraryFiles = null;
    public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;
    public ArrayList<String> mOriginalPackages = null;
    public String mRealPackage = null;
    public ArrayList<String> mAdoptPermissions = null;

    //3
    public Bundle mAppMetaData = null;

    //4
    public int mVersionCode;
    public String mVersionName;
    public String mSharedUserId;
    public int mSharedUserLabel;
    
    //5
    public Signature[] mSignatures;
    public Certificate[][] mCertificates;

    //6
    public int mPreferredOrder = 0;
    //public final ArraySet<String> mDexOptPerformed = new ArraySet<>(4);
    public ArrayList<String> mDexOptPerformed = new ArrayList<>(4);
    public long mLastPackageUsageTimeInMills;
    public Object mExtras;
    public boolean mOperationPending;

    //7
    public ArrayList<ConfigurationInfo> configPreferences = null;
    public ArrayList<FeatureInfo> reqFeatures = null;
    public ArrayList<FeatureGroupInfo> featureGroups = null;

    //8
    public int installLocation;
    public boolean coreApp;
    public boolean mRequiredForAllUsers;
    public String mRestrictedAccountType;
    public String mRequiredAccountType;

    //9
    public ManifestDigest manifestDigest;

    //10
    public String mOverlayTarget;
    public int mOverlayPriority;
    public boolean mTrustedOverlay;

    //11
    public ArraySet<PublicKey> mSigningKeys;
    public ArraySet<String> mUpgradeKeySets;
    public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;

    //12
    public String cpuAbiOverride;
    
    
    
    public Package toPackage(){
    	Package pkg = new Package(packageName);
    	//0
        pkg.splitNames=splitNames;
        pkg.codePath=codePath;
        pkg.baseCodePath=baseCodePath;
        pkg.splitCodePaths=splitCodePaths;
        pkg.baseRevisionCode=baseRevisionCode;
        pkg.splitRevisionCodes=splitRevisionCodes;
        pkg.splitFlags=splitFlags;
        pkg.baseHardwareAccelerated=baseHardwareAccelerated;
    	
        //1
        
        
        //2
        pkg.requestedPermissions.addAll(requestedPermissions);
        pkg.requestedPermissionsRequired.addAll(requestedPermissionsRequired);
        pkg.protectedBroadcasts = Helper.add(pkg.protectedBroadcasts, protectedBroadcasts);
        pkg.libraryNames = Helper.add(pkg.libraryNames, libraryNames);
        pkg.usesLibraries = Helper.add(pkg.usesLibraries, usesLibraries);
        pkg.usesOptionalLibraries = Helper.add(pkg.usesOptionalLibraries, usesOptionalLibraries);
        
        /*
        public ArrayList<String> usesOptionalLibraries = null;
        public String[] usesLibraryFiles = null;
        public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;
        public ArrayList<String> mOriginalPackages = null;
        public String mRealPackage = null;
        public ArrayList<String> mAdoptPermissions = null;
        */
    	//12
        pkg.cpuAbiOverride=cpuAbiOverride;
    	return pkg;
    }
}
