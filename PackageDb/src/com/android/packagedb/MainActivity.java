/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.packagedb;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.graphics.drawable.Drawable;
import java.util.List;
import java.util.ArrayList;

import android.content.res.TypedArray;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import android.content.pm.PackageParser;

import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import java.io.File;
import java.lang.reflect.Field;

public class MainActivity extends Activity {
    static final String TAG = "PackageDbActivity";

    PackageParserDataManager ppdManager;
    //File[] apps;

    final String pkgInstallerPath = "/system/app/PackageInstaller";
    final String pkgInstallerName = "com.android.packageinstaller";
    TextView tv;
    public int i = 0;
    @Override
    public void onCreate(Bundle icycle) {
        super.onCreate(icycle);

        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.abc);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        testXmlObj();
        //initTest();
        //testAddpkg();
//        testDbVersion();
    }

    public void initTest() {

        ppdManager = new PackageParserDb(this);
        ppdManager.clear();
//        File systemAppDir = new File("/system/app");
//        apps = systemAppDir.listFiles();
//       if (ArrayUtils.isEmpty(files)) {
//            Log.w(TAG,"empty system/apps");
//        }

    }
    public void testXmlObj() {
        ObjXmlOtpImpl2 x = new ObjXmlOtpImpl2();
        TestObj o = TestObj.createRandomTestObj();
        Log.e("xxx", o.toString());
        String s = x.serializerPkg(o);
        Log.e("xxx", "-------------------------------------");
        tv.setText(s);
        TestObj o2 = (TestObj)x.parsePkg(s);
        if (o2 != null) {
            Log.e("xxx", o2.toString());
        }
        if (o.equals(o2)) {
            Log.e("xxx", o2.toString());
            Log.e("xxx", "---------pass------------");
        } else {
            Log.e("xxx", "---------fail------------");
        }
    }


    public void testXmlpkg() {
        /*
        XmlPkgSerializer x = new XmlPkgSerializer();
        */
        ObjXmlOtpImpl x = new ObjXmlOtpImpl();
        PackageParser pp = new PackageParser();
        PackageParser.Package pkg = null;
        File apkfile = new File(pkgInstallerPath);
        try {
        int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
        pkg = pp.parsePackage(apkfile, flags);
        } catch (PackageParser.PackageParserException e) {
            e.printStackTrace();
        }
        Log.e("xxx", pkgToString(pkg));
        String s = x.serializerPkg(pkg);
        Log.e("xxx", s);
        Log.e("xxx", "-------------------------------------");
        tv.setText(s);
        //PackageParser.Package pkg2 = x.parsePkg(s);
        //Log.e("xxx", pkgToString(pkg2));
    }

    private String pkgToString(PackageParser.Package pkg) {
        return ""+pkg.packageName+" "+pkg.baseCodePath+" "+pkg.mVersionCode+" "+pkg.baseHardwareAccelerated+" "+pkg.mLastPackageUsageTimeInMills+" ";
    }

    public void testAddpkg() {
        ppdManager.clear();
        ppdManager.addPkgParserData(pkgInstallerPath);
        PackageParser.Package pkg = ppdManager.getPkgParserData(pkgInstallerPath);
        if (pkg.packageName.equals(pkgInstallerName)) {
            Log.i(TAG,"test add pkg sucessful");
        } else {
            Log.i(TAG,"test add pkg fail");
        }
    }
    public void testDbVersion() {
        PackageParserDb manager = null;
        try {
            manager = (PackageParserDb) ppdManager;
        } catch (ClassCastException e) {
            return;
        }

        manager.clear();
        manager.addPkgParserData(pkgInstallerPath);
        manager.testLowerVersion();
        PackageParser.Package pkg = manager.getPkgParserData(pkgInstallerPath);
        if (pkg == null) {
            Log.i(TAG,"test testLowerVersion sucessful");
        } else {

            Log.i(TAG,"test testLowerVersion fail");
        }

        manager.clear();
        manager.addPkgParserData(pkgInstallerPath);
        manager.testHigherVersion();
        pkg = manager.getPkgParserData(pkgInstallerPath);
        if (pkg == null) {
            Log.i(TAG,"test testHigherVersion sucessful");
        } else {

            Log.i(TAG,"test testHigherVersion fail");
        }

    }
}

