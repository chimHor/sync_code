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
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.text.method.ScrollingMovementMethod;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import android.os.Debug;


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

public class MainActivity extends Activity {
    static final String TAG = "PackageDbActivity";

    PackageParserDataManager ppdManager;
    //File[] apps;

    final String pkgInstallerPath = "/system/app/PackageInstaller";
    final String pkgInstallerName = "com.android.packageinstaller";
    TextView tv;
    Button b;
    public int i = 0;
    PackageParser.Package pkg = null;
    @Override
    public void onCreate(Bundle icycle) {
        super.onCreate(icycle);

        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.abc);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        b = (Button) findViewById(R.id.but);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                            //testXmlpkg();
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
        pkg = pp.parsePackage(apkfile, flags);
        pp.collectCertificates(pkg,flags);
        pp.collectManifestDigest(pkg);

        ObjXmlOtpImpl2 x = new ObjXmlOtpImpl2();
        String s = x.serializerPkg(pkg);
        Log.e("xxx", "----  string size :  " + s.length()+ "  -----------------------------");
        tv.setText(s);

        PackageParser.Package pkg2 = null;
        pkg2 = (PackageParser.Package)x.parsePkg(s);
        if (pkg2 != null) {
            Log.e("xxx", pkg2.toString());
        }
        } catch (PackageParser.PackageParserException e) {
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


    public static class Holder implements Serializable {
        //public byte[] mDigest;
        public Certificate[][] mCertificates;
        public Holder(PackageParser.Package pkg) {
            mCertificates = pkg.mCertificates;
        }
    }

    public static class Holder2 implements Serializable {
        //public byte[] mDigest;
        public final byte[] bytes;
        public Holder2(byte[] array) {
            bytes = array;
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
        int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
        try {
        pkg = pp.parsePackage(apkfile, flags);
        pp.collectCertificates(pkg,flags);
        pp.collectManifestDigest(pkg);
        Holder h = new Holder(pkg);

        X509Certificate[] certs = new X509Certificate[h.mCertificates.length];
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Class c = org.apache.harmony.security.utils.JarUtils.class;
        Class[] cs = c.getDeclaredClasses();
        Class c2 = null;
        for (Class c1 : cs) {
            if (c1.getName().contains("VerbatimX509Certificate")) {
                c2 = c1;
                break;
            }
        }
        Constructor t = null;
        if (c2 != null) {
            Log.e("xxx", "c2:"+c2.getName());
            Constructor[] ts = c2.getConstructors();
            //t = c2.getDeConstructor(X509Certificate.class, byte[].class);
            t = ts[0];
            if (t != null)
                Log.e("xxx", "find c2: t");
        }
        t.setAccessible(true);
        int i = 0;
        for (Certificate encCert : h.mCertificates[0]) {
            final byte[] encoded = encCert.getEncoded();
            final InputStream is = new ByteArrayInputStream(encoded);
            certs[i++] = (X509Certificate)t.newInstance(cf.generateCertificate(is), encoded);
        }

        Holder2 hh = new Holder2(certs[0].getEncoded());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outBytes);
        out.writeObject(hh);
        out.close();
        ByteArrayInputStream inBytes = new ByteArrayInputStream(outBytes.toByteArray());
        Holder2 h2 = null;
        if (inBytes.available()>0) {
            ObjectInputStream in = new ObjectInputStream(inBytes);
            h2 = (Holder2)in.readObject();
        }

        Log.e("xxx", "-------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        Log.e("xxx", pkgToString(pkg));
        String s = x.serializerPkg(pkg);
        Log.e("xxx", s);
        Log.e("xxx", "-------------------------------------");
        tv.setText(s);
        */
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

