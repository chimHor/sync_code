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
import android.os.SystemClock;
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
import com.android.packagedb.util2.CertificateGhost;
import com.android.packagedb.util2.ManifestDigestGhost;
import com.android.packagedb.util2.PackageDataManager;

public class MainActivity extends Activity {
    static final String TAG = "PackageDbActivity";

    PackageDataManager ppdManager;
    //File[] apps;

    final String pkgInstallerPath = "/system/app/Gallery2";
    final String pkgInstallerName = "com.android.packageinstaller";
    TextView tv;
    Button br;
    Button bw;
    Button bt;
    public int i = 0;
    @Override
    public void onCreate(Bundle icycle) {
        super.onCreate(icycle);

        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.abc);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        CertificateGhost g = new CertificateGhost(null);
        ManifestDigestGhost m = new ManifestDigestGhost(null);
        ppdManager = PackageDataManager.getInstance(this);
        
        br = (Button) findViewById(R.id.butr);
        br.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
		                	long t = SystemClock.uptimeMillis();
		                	ppdManager.readDbData();
		                	Log.e("xxx", "readDbData time :"+ (SystemClock.uptimeMillis()-t));
		                	ppdManager.waitForReadTasksFinish();
		                	Log.e("xxx", "finish dump time :"+ (SystemClock.uptimeMillis()-t));
		                	readDbAntTest();
		                	ppdManager.cleanNotScanedPkg();
                        }
                });
        bw = (Button) findViewById(R.id.butw);
        bw.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                			ppdManager.clear();
                			writeDb();
                			
                        }
                });
        bt = (Button) findViewById(R.id.butt);
        bt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                			//readDbAntTest();
                        }
                });
    }
    
    public void readDbAntTest() {
    	final File systemPrivAppDir = new File("/system/priv-app");
    	final File systemAppDir = new File("/system/app");
    	final File[] files1 = systemPrivAppDir.listFiles();
    	final File[] files2 = systemAppDir.listFiles();
    	PackageParser pp = new PackageParser();
    	int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
    	int suc = 0;
    	for (File file : files1) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
    		PackageParser.Package pkg2 = ppdManager.getPkgParserData(file.getPath());
            boolean b = TestCompareHelper.compare(pkg, pkg2);
            if (!b) {
            Log.w("xxx", " pkg " + file.toString()+"parse cmp ret : "+ b);
            return;
            } else  {
            	suc +=1;
            }
            } catch (PackageParser.PackageParserException e) {
                e.printStackTrace();
            }
            
    	}
    	for (File file : files2) {
    		try{
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
    		PackageParser.Package pkg2 = ppdManager.getPkgParserData(file.getPath());
            boolean b = TestCompareHelper.compare(pkg, pkg2);
            if (!b) {
            Log.w("xxx", " pkg " + file.toString()+"parse cmp ret : "+ b);
            return;
            } else  {
            	suc +=1;
            }
	        } catch (PackageParser.PackageParserException e) {
	            e.printStackTrace();
	        }
    	}
    	Log.w("xxx", " pkg "+suc+"/"+ (files1.length+files2.length) +" pass ");
    	return ;
    }
    public void writeDb() {
    	final File systemPrivAppDir = new File("/system/priv-app");
    	final File systemAppDir = new File("/system/app");
    	final File[] files1 = systemPrivAppDir.listFiles();
    	final File[] files2 = systemAppDir.listFiles();
    	PackageParser pp = new PackageParser();
    	int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
    	long t = 0;
    	for (File file : files1) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
            long t1 = SystemClock.uptimeMillis();
            ppdManager.addPkgParserData(pkg);
            t += SystemClock.uptimeMillis() - t1;
            } catch (PackageParser.PackageParserException e) {
                e.printStackTrace();
            }
    	}
    	for (File file : files2) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
            long t1 = SystemClock.uptimeMillis();
            ppdManager.addPkgParserData(pkg);
            t += SystemClock.uptimeMillis() - t1;
	        } catch (PackageParser.PackageParserException e) {
	            e.printStackTrace();
	        }
    	}
    	Log.w("xxx", " serilize pkg time : " + t);
    	ppdManager.saveDataToDb();
    }
}

