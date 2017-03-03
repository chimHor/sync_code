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
        ppdManager = new PackageDataManager(this);
        br = (Button) findViewById(R.id.butr);
        br.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                            //testPkg();
                			//cmpSystemApp();
                			//ttt();
                			//cmpSystemApp2();
                			//cmpSystemApp3();
                			//com.android.packagedb.util2.Helper.serializaPerfTest();
                			readDb();
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
                			readDbAntTest();
                        }
                });
    //testXmlObj();
//        initTest();
//        testAddpkg();
//        testDbVersion();
    }

    public void cmpSystemApp() {
    	final File systemAppDir = new File("/system/app");
    	final File[] files = systemAppDir.listFiles();
    	PackageParser pp = new PackageParser();
    	int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
    	PkgSerializer x = new PkgSerializer();
    	int suc = 0;
    	long t= 0;
    	for (File file : files) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
            String s = x.serializerPkg(pkg);
            writeFile(s);
            //Log.e("xxx", file.toString() + "----  string size :  " + s.length()+ "  -----------------------------");
            PackageParser.Package pkg2 = null;
            long t1 = SystemClock.uptimeMillis();
            Log.e("xxx",file.toString()+"time mark");
            //Debug.startMethodTracing(file.getName());
            pkg2 = (PackageParser.Package)x.parsePkg(s);
            //Debug.stopMethodTracing();
            long t2 = SystemClock.uptimeMillis();
            t += t2-t1;
            Log.e("xxx",file.toString()+"time mark2");
            boolean b = TestCompareHelper.compare(pkg, pkg2);
            if (!b) {
            Log.w("xxx", " pkg " + file.toString()+"parse cmp ret : "+ b);
            writeFile(s);
            break;
            } else  {
            	suc +=1;readDb();
            }
            } catch (PackageParser.PackageParserException e) {
                e.printStackTrace();
            }
    	}
    	Log.w("xxx", "suc : "+ suc + " t : "+ t);
    	
    }
    
    public void cmpSystemApp2() {
    	final File systemAppDir = new File("/system/app");
    	final File[] files = systemAppDir.listFiles();
    	PackageParser pp = new PackageParser();
    	int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
    	int suc = 0;
    	long t= 0;
    	for (File file : files) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
            byte[] bytes = com.android.packagedb.util2.Helper.serializePackage(pkg);
            PackageParser.Package pkg2 = null;
            long t1 = SystemClock.uptimeMillis();
            //Debug.startMethodTracing(file.getName());
            pkg2 = (PackageParser.Package)com.android.packagedb.util2.Helper.parsePackage(bytes);
            //Debug.stopMethodTracing();
            long t2 = SystemClock.uptimeMillis();
            t += t2-t1;
            boolean b = TestCompareHelper.compare(pkg, pkg2);
            if (!b) {
            Log.w("xxx", " pkg " + file.toString()+"parse cmp ret : "+ b);
            //break;
            } else  {
            	suc +=1;
            }
            } catch (PackageParser.PackageParserException e) {
                e.printStackTrace();
            }
    	}
    	Log.w("xxx", "suc : "+ suc + " t : "+ t);
    	
    }
    
    public void cmpSystemApp3() {
    	final File systemAppDir = new File("/system/priv-app");
    	final File[] files = systemAppDir.listFiles();
    	PackageParser pp = new PackageParser();
    	int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;
    	int suc = 0;
    	long t= 0;
    	for (File file : files) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
            byte[] bytes = com.android.packagedb.util2.Helper.serializePackage(pkg);
            PackageParser.Package pkg2 = null;
            long t1 = SystemClock.uptimeMillis();
            //Debug.startMethodTracing(file.getName());
            pkg2 = (PackageParser.Package)com.android.packagedb.util2.Helper.parsePackage(bytes);
            //Debug.stopMethodTracing();
            long t2 = SystemClock.uptimeMillis();
            t += t2-t1;
            boolean b = TestCompareHelper.compare(pkg, pkg2);
            if (!b) {
            Log.w("xxx", " pkg " + file.toString()+"parse cmp ret : "+ b);
            //break;
            } else  {
            	suc +=1;
            }
            } catch (PackageParser.PackageParserException e) {
                e.printStackTrace();
            }
    	}
    	Log.w("xxx", "suc : "+ suc + " t : "+ t);
    	
    }
    
    public List<PackageParser.Package> readDb() {
    	final File systemPrivAppDir = new File("/system/priv-app");
    	final File systemAppDir = new File("/system/app");
    	final File[] files1 = systemPrivAppDir.listFiles();
    	final File[] files2 = systemAppDir.listFiles();
    	long t1 = SystemClock.uptimeMillis();
    	ArrayList<PackageParser.Package> list = new ArrayList<PackageParser.Package>();
    	for (File file : files1) {
    		PackageParser.Package pkg = ppdManager.getPkgParserData(file.getPath());
    		list.add(pkg);
    	}
    	for (File file : files2) {
    		PackageParser.Package pkg = ppdManager.getPkgParserData(file.getPath());
    		list.add(pkg);
    	}
    	long t2 = SystemClock.uptimeMillis();
    	Log.w("xxx", " parse time  :" + (t2-t1));
    	ppdManager.close();
    	return list;
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
    	ppdManager.close();
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
    	for (File file : files1) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
            ppdManager.addPkgParserData(pkg);
            } catch (PackageParser.PackageParserException e) {
                e.printStackTrace();
            }
    	}
    	for (File file : files2) {
    		try {
    		PackageParser.Package pkg = pp.parsePackage(file, flags);
    		pp.collectCertificates(pkg,flags);
            pp.collectManifestDigest(pkg);
            ppdManager.addPkgParserData(pkg);
	        } catch (PackageParser.PackageParserException e) {
	            e.printStackTrace();
	        }
    	}
    	ppdManager.close();
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
        
        //Log.e("xxx", "----  string size :  " + s.length()+ "  -----------------------------");
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


}

