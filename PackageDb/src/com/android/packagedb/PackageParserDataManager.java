package com.android.packagedb;

import android.content.pm.PackageParser;

import java.util.Objects;

/**
 * Created by chim on 1/19/17.
 */


public interface PackageParserDataManager {
    public boolean addPkgParserData(String apkPath);
    public boolean addPkgParserData(PackageParser.Package pkg);
    public PackageParser.Package getPkgParserData(String apkPath);
    public boolean removePkgParserData(String apkPath);
    public boolean isUpdateNeeded(String apkPath);
    public boolean updatePkgParserData(String apkPath);
    public void clear();
    public boolean isClearNeeded();
}
