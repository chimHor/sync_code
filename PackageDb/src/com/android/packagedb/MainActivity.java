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

import android.content.Context;
import android.content.res.TypedArray;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import android.content.pm.PackageParser;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.text.method.ScrollingMovementMethod;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import android.os.Debug;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
//import org.apache.harmony.security.utils.JarUtils.VerbatimX509Certificate;

import com.android.packagedb.test.TestCompareHelper;
import com.android.packagedb.test.TestObj;
import com.android.packagedb.util.PkgSerializer;

public class MainActivity extends Activity {
    static final String TAG = "PackageDbActivity";

    PackageParserDataManager ppdManager;
    //File[] apps;

    final String pkgInstallerPath = "/system/app/PackageInstaller";
    final String pkgInstallerName = "com.android.packageinstaller";
    TextView tv;
    Button b;
    public int i = 0;
    @Override
    public void onCreate(Bundle icycle) {
        super.onCreate(icycle);

        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.abc);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        b = (Button) findViewById(R.id.but);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                            //testPkg();
                            ttt();
                        }
                });

    //testXmlObj();
//        initTest();
//        testAddpkg();
//        testDbVersion();
    }

    public void ttt() {
        PackageParser pp = new PackageParser();
        try {
        int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
        File apkfile = new File(pkgInstallerPath);
        PackageParser.Package pkg = pp.parsePackage(apkfile, flags);
        pp.collectCertificates(pkg,flags);
        pp.collectManifestDigest(pkg);

        PkgSerializer x = new PkgSerializer();
        String s = x.serializerPkg(pkg);
        
        Log.e("xxx", "----  string size :  " + s.length()+ "  -----------------------------");
        writeFile(s);
        tv.setText(s);

        PackageParser.Package pkg2 = null;
        pkg2 = (PackageParser.Package)x.parsePkg(s);
        boolean b = TestCompareHelper.compare(pkg, pkg2);
        Log.w("xxx", " pkg parse cmp ret : "+ b);
        } catch (PackageParser.PackageParserException e) {
            e.printStackTrace();
        }

    }

    private void writeFile(String s) {
        try{
            
            //true = append file
            FileOutputStream outputStream = this.openFileOutput("pkg.xml", Context.MODE_PRIVATE);
            outputStream.write(s.getBytes());
            outputStream.close();
           }catch(IOException e){
            e.printStackTrace();
           }
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
        PkgSerializer x = new PkgSerializer();
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

    public void testPkg() {
        /*
        XmlPkgSerializer x = new XmlPkgSerializer();
        */
        PackageParser pp = new PackageParser();
        PackageParser.Package pkg = null;
        PackageParser.Package pkg2 = null;
        File apkfile = new File(pkgInstallerPath);
        int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
        
        try {
        pkg = pp.parsePackage(apkfile, flags);
        pp.collectCertificates(pkg,flags);
        pp.collectManifestDigest(pkg);
	
        pkg2 = pp.parsePackage(apkfile, flags);
        pp.collectCertificates(pkg2,flags);
        pp.collectManifestDigest(pkg2);
        boolean b = TestCompareHelper.compareInner(pkg, pkg2);
        Log.w("xxx","res : "+ b);
        //PackageParser.Package pkg2 = x.parsePkg(s);
        //Log.e("xxx", pkgToString(pkg2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void testAddpkg() {
        ppdManager.clear();
        ppdManager.addPkgParserData(pkgInstallerPath);
        PackageParser.Package pkg = ppdManager.getPkgParserData(pkgInstallerPath);
        /*
        if (pkg.packageName.equals(pkgInstallerName)) {
            Log.i(TAG,"test add pkg sucessful");
        } else {
            Log.i(TAG,"test add pkg fail");
        }
        */
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

