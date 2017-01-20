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

import java.io.File;
import java.lang.reflect.Field;
//import java.lang.NoSuchFieldException;

public class MainActivity extends Activity {
    static final String TAG = "PackageDbActivity";

    PackageParserDataManager ppdManager;
    //File[] apps;

    final String pkgInstallerPath = "/system/app/PackageInstaller";
    final String pkgInstallerName = "com.android.packageinstaller";

    public int i = 0;
    @Override
    public void onCreate(Bundle icycle) {
        super.onCreate(icycle);

        setContentView(R.layout.main);

        initTest();
        testAddpkg();
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

