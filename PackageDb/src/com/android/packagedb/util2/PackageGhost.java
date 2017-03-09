package com.android.packagedb.util2;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureGroupInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Instrumentation;
import android.content.pm.PackageParser.Permission;
import android.content.pm.PackageParser.PermissionGroup;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageParser.Package;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Base64;

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
    //public final ApplicationInfo applicationInfo = new ApplicationInfo();
    public final ApplicationInfoGhost applicationInfo;
    public final ArrayList<PermissionGhost> permissions = new ArrayList<PermissionGhost>(0);
    public final ArrayList<PermissionGroupGhost> permissionGroups = new ArrayList<PermissionGroupGhost>(0);
    public final ArrayList<ActivityGhost> activities = new ArrayList<ActivityGhost>(0);
    public final ArrayList<ActivityGhost> receivers = new ArrayList<ActivityGhost>(0);
    public final ArrayList<ProviderGhost> providers = new ArrayList<ProviderGhost>(0);
    public final ArrayList<ServiceGhost> services = new ArrayList<ServiceGhost>(0);
    public final ArrayList<InstrumentationGhost> instrumentation = new ArrayList<InstrumentationGhost>(0);

    //2
    public final ArrayList<String> requestedPermissions = new ArrayList<String>();
    public final ArrayList<Boolean> requestedPermissionsRequired = new ArrayList<Boolean>();
    public ArrayList<String> protectedBroadcasts;
    public ArrayList<String> libraryNames = null;
    public ArrayList<String> usesLibraries = null;
    public ArrayList<String> usesOptionalLibraries = null;
    public String[] usesLibraryFiles = null;
    public ArrayList<ActivityIntentInfoGhost> preferredActivityFilters = null;
    public ArrayList<String> mOriginalPackages = null;
    public String mRealPackage = null;
    public ArrayList<String> mAdoptPermissions = null;

    //3
    //public Bundle mAppMetaData = null;
    public BundleGhost mAppMetaData;

    //4
    public int mVersionCode;
    public String mVersionName;
    public String mSharedUserId;
    public int mSharedUserLabel;

    //5
    //public Signature[] mSignatures;
    public CertificateGhost[][] mCertificates;

    //6
    public int mPreferredOrder = 0;
    //public final ArraySet<String> mDexOptPerformed = new ArraySet<>(4);
    public ArrayList<String> mDexOptPerformed = new ArrayList<>(4);
    public long mLastPackageUsageTimeInMills;
    //public Object mExtras;
    public boolean mOperationPending;

    //7
    public ArrayList<ConfigurationInfoGhost> configPreferences = null;
    public ArrayList<FeatureInfoGhost> reqFeatures = null;
    public ArrayList<FeatureGroupInfoGhost> featureGroups = null;

    //8
    public int installLocation;
    public boolean coreApp;
    public boolean mRequiredForAllUsers;
    public String mRestrictedAccountType;
    public String mRequiredAccountType;

    //9
    public ManifestDigestGhost manifestDigest;

    //10
    public String mOverlayTarget;
    public int mOverlayPriority;
    public boolean mTrustedOverlay;

    //11
    //public ArraySet<PublicKey> mSigningKeys;
    //public ArraySet<String> mUpgradeKeySets;
    public ArrayList<String> mUpgradeKeySets;
    public static class KeySetMapItem {
    	String key;
    	ArrayList<String> value = new ArrayList<String>();
    }
    //public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;
    public ArrayList<KeySetMapItem> mKeySetMapping = null;

    //12
    public String cpuAbiOverride;
    
    public PackageGhost(Package pkg) {
    	//0
    	packageName = pkg.packageName;
        splitNames = pkg.splitNames;
        codePath = pkg.codePath;
        baseCodePath = pkg.baseCodePath;
        splitCodePaths = pkg.splitCodePaths;
        baseRevisionCode = pkg.baseRevisionCode;
        splitRevisionCodes = pkg.splitRevisionCodes;
        splitFlags = pkg.splitFlags;
        baseHardwareAccelerated = pkg.baseHardwareAccelerated;
        
        //1
        applicationInfo = new ApplicationInfoGhost(pkg.applicationInfo);
        for (int i = 0 ; i < pkg.permissions.size() ; i++ ) {
        	PermissionGhost permG = new PermissionGhost(pkg.permissions.get(i));
        	permissions.add(permG);
        }
        for (int i = 0 ; i < pkg.permissionGroups.size() ; i++ ) {
        	PermissionGroupGhost permG = new PermissionGroupGhost(pkg.permissionGroups.get(i));
        	permissionGroups.add(permG);
        }
        for (int i = 0 ; i < pkg.activities.size() ; i++ ) {
        	ActivityGhost ag = new ActivityGhost(pkg.activities.get(i));
        	activities.add(ag);
        }
        for (int i = 0 ; i < pkg.receivers.size() ; i++ ) {
        	ActivityGhost ag = new ActivityGhost(pkg.receivers.get(i));
        	receivers.add(ag);
        }
        for (int i = 0 ; i < pkg.providers.size() ; i++ ) {
        	ProviderGhost pg = new ProviderGhost(pkg.providers.get(i));
        	providers.add(pg);
        }
        for (int i = 0 ; i < pkg.services.size() ; i++ ) {
        	ServiceGhost sg = new ServiceGhost(pkg.services.get(i));
        	services.add(sg);
        }
        for (int i = 0 ; i < pkg.instrumentation.size() ; i++ ) {
        	InstrumentationGhost ig = new InstrumentationGhost(pkg.instrumentation.get(i));
        	instrumentation.add(ig);
        }
        

        //2
        requestedPermissions.addAll(pkg.requestedPermissions);
        requestedPermissionsRequired.addAll(pkg.requestedPermissionsRequired);
        protectedBroadcasts = Helper.add(protectedBroadcasts, pkg.protectedBroadcasts);
        libraryNames = Helper.add(libraryNames, pkg.libraryNames);
        usesLibraries = Helper.add(usesLibraries, pkg.usesLibraries);
        usesOptionalLibraries = Helper.add(usesOptionalLibraries, pkg.usesOptionalLibraries);
        if (pkg.usesLibraryFiles != null) {
        	usesLibraryFiles = pkg.usesLibraryFiles;
        }
        mOriginalPackages = Helper.add(mOriginalPackages, pkg.mOriginalPackages);
        if (pkg.preferredActivityFilters != null) {
        	preferredActivityFilters = new ArrayList<ActivityIntentInfoGhost>();
	        for (int i = 0; i < pkg.preferredActivityFilters.size(); i++) {
	        	ActivityIntentInfoGhost aiig = new ActivityIntentInfoGhost(pkg.preferredActivityFilters.get(i), pkg.activities, pkg.receivers);
	        	preferredActivityFilters.add(aiig);
	        }
        }
        mRealPackage = pkg.mRealPackage;
        mAdoptPermissions = Helper.add(mAdoptPermissions, pkg.mAdoptPermissions);

        //3
        if (pkg.mAppMetaData != null) {
        	mAppMetaData = new BundleGhost(pkg.mAppMetaData);
        }
        //4
        mVersionCode = pkg.mVersionCode;
        mVersionName = pkg.mVersionName;
        mSharedUserId = pkg.mSharedUserId;
        mSharedUserLabel = pkg.mSharedUserLabel;

        //5
        if (pkg.mCertificates != null) {
        mCertificates = new CertificateGhost[pkg.mCertificates.length][];
        for (int i = 0; i < pkg.mCertificates.length; i++) {
        	mCertificates[i] = new CertificateGhost[pkg.mCertificates[i].length];
        	for (int j = 0; j < pkg.mCertificates[i].length; j++) {
        		mCertificates[i][j] = new CertificateGhost(pkg.mCertificates[i][j]);
        	}
        }
        }

        //6
        mPreferredOrder = pkg.mPreferredOrder;
        mDexOptPerformed = Helper.add(mDexOptPerformed, pkg.mDexOptPerformed);
        mLastPackageUsageTimeInMills = pkg.mLastPackageUsageTimeInMills;
        //pkg.mExtras;
        mOperationPending = pkg.mOperationPending;

        //7
        if (pkg.configPreferences != null) {
        	configPreferences = new ArrayList<ConfigurationInfoGhost>();
        	for (int i = 0; i < pkg.configPreferences.size(); i++) {
        		ConfigurationInfoGhost cig = new ConfigurationInfoGhost(pkg.configPreferences.get(i));
        		configPreferences.add(cig);
        	}
        }
        if (pkg.reqFeatures != null) {
        	reqFeatures = new ArrayList<FeatureInfoGhost>();
        	for (int i = 0; i < pkg.reqFeatures.size(); i++) {
        		FeatureInfoGhost fi = new FeatureInfoGhost(pkg.reqFeatures.get(i));
        		reqFeatures.add(fi);
        	}
        }
        if (pkg.featureGroups != null) {
        	featureGroups = new ArrayList<FeatureGroupInfoGhost>();
        	for (int i = 0; i < pkg.featureGroups.size(); i++) {
        		FeatureGroupInfoGhost fi = new FeatureGroupInfoGhost(pkg.featureGroups.get(i));
        		featureGroups.add(fi);
        	}
        }

        //8
        installLocation = pkg.installLocation;
        coreApp = pkg.coreApp;
        mRequiredForAllUsers = pkg.mRequiredForAllUsers;
        mRestrictedAccountType = pkg.mRestrictedAccountType;
        mRequiredAccountType = pkg.mRestrictedAccountType;

        //9
        if (pkg.manifestDigest != null) {
        	manifestDigest = new ManifestDigestGhost(pkg.manifestDigest);
        }
        //10
        mOverlayTarget = pkg.mOverlayTarget;
        mOverlayPriority = pkg.mOverlayPriority;
        mTrustedOverlay = pkg.mTrustedOverlay;

        //11
        mUpgradeKeySets = Helper.add(mUpgradeKeySets, pkg.mUpgradeKeySets);
        if (pkg.mKeySetMapping != null) {
        	mKeySetMapping = new ArrayList<KeySetMapItem>();
        	if (pkg.mKeySetMapping.size()>0) {
                Set<String> keySet = pkg.mKeySetMapping.keySet();
                Iterator<String> iterator = keySet.iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    KeySetMapItem ksmi = new KeySetMapItem();
                    ksmi.key = key;
                    Iterator<PublicKey> subIterator = pkg.mKeySetMapping.get(key).iterator();
                    while(subIterator.hasNext()) {
                    	PublicKey p = subIterator.next();
                    	String encodedKey = new String(Base64.encode(p.getEncoded(), 0));
                    	ksmi.value.add(encodedKey);
                    }
                    mKeySetMapping.add(ksmi);
                }
        	}
        }

    	//12
        cpuAbiOverride = pkg.cpuAbiOverride;
    }
    
    private static Signature[] convertToSignatures(Certificate[][] certs)
            throws CertificateEncodingException {
        final Signature[] res = new Signature[certs.length];
        for (int i = 0; i < certs.length; i++) {
            res[i] = new Signature(certs[i]);
        }
        return res;
    }

    public Package dumpFromGhost(){
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
        applicationInfo.dumpFromGhost(pkg.applicationInfo);
        for (int i = 0 ; i < permissions.size() ; i++ ) {
        	Permission perm = permissions.get(i).dumpFromGhost(pkg);
        	pkg.permissions.add(perm);
        }
        for (int i = 0 ; i < permissionGroups.size() ; i++ ) {
        	PermissionGroup perm = permissionGroups.get(i).dumpFromGhost(pkg);
        	pkg.permissionGroups.add(perm);
        }
        for (int i = 0 ; i < activities.size() ; i++ ) {
        	Activity a = activities.get(i).dumpFromGhost(pkg);
        	pkg.activities.add(a);
        }
        for (int i = 0 ; i < receivers.size() ; i++ ) {
        	Activity a = receivers.get(i).dumpFromGhost(pkg);
        	pkg.receivers.add(a);
        }
        for (int i = 0 ; i < providers.size() ; i++ ) {
        	Provider p = providers.get(i).dumpFromGhost(pkg);
        	pkg.providers.add(p);
        }
        for (int i = 0 ; i < services.size() ; i++ ) {
        	Service s = services.get(i).dumpFromGhost(pkg);
        	pkg.services.add(s);
        }
        for (int i = 0 ; i < instrumentation.size() ; i++ ) {
        	Instrumentation ins = instrumentation.get(i).dumpFromGhost(pkg);
        	pkg.instrumentation.add(ins);
        }


        //2
        pkg.requestedPermissions.addAll(requestedPermissions);
        pkg.requestedPermissionsRequired.addAll(requestedPermissionsRequired);
        pkg.protectedBroadcasts = Helper.add(pkg.protectedBroadcasts, protectedBroadcasts);
        pkg.libraryNames = Helper.add(pkg.libraryNames, libraryNames);
        pkg.usesLibraries = Helper.add(pkg.usesLibraries, usesLibraries);
        pkg.usesOptionalLibraries = Helper.add(pkg.usesOptionalLibraries, usesOptionalLibraries);
        if (usesLibraryFiles != null) {
        	pkg.usesLibraryFiles = usesLibraryFiles;
        }
        pkg.mOriginalPackages = Helper.add(pkg.mOriginalPackages, mOriginalPackages);   
        if (preferredActivityFilters != null) {
        	pkg.preferredActivityFilters = new ArrayList<ActivityIntentInfo>();
        	for (int i = 0; i < preferredActivityFilters.size(); i++) {
        		ActivityIntentInfo aii = preferredActivityFilters.get(i).dumpFromGhost(pkg.activities, pkg.receivers);
        		pkg.preferredActivityFilters.add(aii);
        	}
        }
        pkg.mRealPackage = mRealPackage;
        pkg.mAdoptPermissions = Helper.add(pkg.mAdoptPermissions, mAdoptPermissions);
        //3
        if (mAppMetaData != null) {
        	pkg.mAppMetaData = mAppMetaData.dumpFromGhost(null);
        }	
        //4
        pkg.mVersionCode = mVersionCode;
        pkg.mVersionName = mVersionName;
        pkg.mSharedUserId = mSharedUserId;
        pkg.mSharedUserLabel = mSharedUserLabel;

        //5
        if (mCertificates != null) {
	        pkg.mCertificates = new Certificate[mCertificates.length][];
	        for (int i = 0; i < mCertificates.length; i++) {
	        	pkg.mCertificates[i] = new Certificate[mCertificates[i].length];
	        	for (int j = 0; j < mCertificates[i].length; j++) {
	        		pkg.mCertificates[i][j] = mCertificates[i][j].dumpFromGhost();
	        	}
	        }
        }
        if (pkg.mCertificates != null) {
        	try {
				pkg.mSignatures = convertToSignatures(pkg.mCertificates);
			} catch (CertificateEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        
        //6
        pkg.mPreferredOrder = mPreferredOrder;
        Helper.add(pkg.mDexOptPerformed, mDexOptPerformed);
        pkg.mLastPackageUsageTimeInMills = mLastPackageUsageTimeInMills;
        //pkg.mExtras;
        pkg.mOperationPending = mOperationPending;

        //7
        if (configPreferences != null) {
        	pkg.configPreferences = new ArrayList<ConfigurationInfo>();
        	for (int i = 0; i < configPreferences.size(); i++) {
        		pkg.configPreferences.add(configPreferences.get(i).dumpFromGhost());
        	}
        }
        if (reqFeatures != null) {
        	pkg.reqFeatures = new ArrayList<FeatureInfo>();
        	for (int i = 0; i < reqFeatures.size(); i++) {
        		pkg.reqFeatures.add(reqFeatures.get(i).dumpFromGhost());
        	}
        }
        if (featureGroups != null) {
        	pkg.featureGroups = new ArrayList<FeatureGroupInfo>();
        	for (int i = 0; i < featureGroups.size(); i++) {
        		pkg.featureGroups.add(featureGroups.get(i).dumpFromGhost());
        	}
        }


        //8
        pkg.installLocation = installLocation;
        pkg.coreApp = coreApp;
        pkg.mRequiredForAllUsers = mRequiredForAllUsers;
        pkg.mRestrictedAccountType = mRestrictedAccountType;
        pkg.mRequiredAccountType = mRestrictedAccountType;

        //9
        if (manifestDigest != null) {
        	pkg.manifestDigest = manifestDigest.dumpFromGhost();
        }
        //10
        pkg.mOverlayTarget = mOverlayTarget;
        pkg.mOverlayPriority = mOverlayPriority;
        pkg.mTrustedOverlay = mTrustedOverlay;

        //11
        if (pkg.mCertificates != null) {
	        pkg.mSigningKeys = new ArraySet<PublicKey>();
	        for (int i=0; i < pkg.mCertificates.length; i++) {
	            pkg.mSigningKeys.add(pkg.mCertificates[i][0].getPublicKey());
	        }
        }
        pkg.mUpgradeKeySets = Helper.add(pkg.mUpgradeKeySets, mUpgradeKeySets);
        if (mKeySetMapping != null) {
        	pkg.mKeySetMapping = new ArrayMap<String, ArraySet<PublicKey>>();
        	if (mKeySetMapping.size()>0) {
        		for (KeySetMapItem ksmi : mKeySetMapping){
        			ArraySet<PublicKey> set = new ArraySet<PublicKey>();
        			for (String encodedKey : ksmi.value) {
        				PublicKey p = PackageParser.parsePublicKey(encodedKey);
        				set.add(p);
        			}
        			pkg.mKeySetMapping.put(ksmi.key, set);
        		}
        	}
        }
        
    	//12
        pkg.cpuAbiOverride=cpuAbiOverride;
    	return pkg;
    }
}
