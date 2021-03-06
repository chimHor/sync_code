package com.android.packagedb;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.os.Build;

import java.io.File;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;
import android.util.ArrayMap;

import android.content.pm.PackageParser;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
/**
 * Created by chim on 1/19/17.
 */

public class PackageParserDb implements PackageParserDataManager {

    static final String TAG = "PackageParserDb";

    static final String DB_NAME = "package.db";
    static final int DB_VERSION = 2;
    static final String HEAD_TABLE = "head";
    static final String SYSTEMPKG_TABLE = "systempkg";
    static class HeadTableColumns {
        public static final String _KEY = "key";
        public static final String _VALUE = "value";
        public static final String K_FINGERPRINT = "FINGERPRINT";

    }

    static class PackageTableColumns {
        public static final String _CODEPATH = "codePath";
        public static final String _PACKAGENAME = "packageName";
        public static final String _CONTENT = "content";

    }

    class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context, int version) {
            super(context, DB_NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createHeadTable(db);
            createSystemPkgTable(db);
        }

        private void createHeadTable(SQLiteDatabase db) {
            final String sql = "CREATE TABLE " + HEAD_TABLE + " (" + HeadTableColumns._KEY +" TEXT PRIMARY KEY, "
                    + HeadTableColumns._VALUE +" TEXT);";
            db.execSQL(sql);
            insertHeadTable(db, HeadTableColumns.K_FINGERPRINT, Build.FINGERPRINT);
        }

        private void insertHeadTable(SQLiteDatabase db, String key, String value) {
            final String sql = "INSERT INTO " + HEAD_TABLE + " (" + HeadTableColumns._KEY + " , " + HeadTableColumns._VALUE + " ) "
                    + " VALUES ('" + key + "', '" + value + "');";
            db.execSQL(sql);
        }
        private void createSystemPkgTable(SQLiteDatabase db) {
            // todo: package parser update point
            //final String sql = "CREATE TABLE " + SYSTEMPKG_TABLE + " (" + PackageTableColumns._CODEPATH +" TEXT PRIMARY KEY, "
            //        + PackageTableColumns._PACKAGENAME +" TEXT);";
            //db.execSQL(sql);

            StringBuilder sb = new StringBuilder("CREATE TABLE " + SYSTEMPKG_TABLE + " (");
            sb.append(PackageTableColumns._CODEPATH + " TEXT PRIMARY KEY");
            sb.append(", " + PackageTableColumns._PACKAGENAME + " TEXT");
            sb.append(", " + PackageTableColumns._CONTENT + " TEXT");
                /*
            for (Map.Entry<String, Field> entry : pkgFieldsMap.entrySet()) {
                String key = entry.getKey();
                if (key.equals("codePath")) {
                    continue;
                }
                sb.append(", " + key + " TEXT");
                class type = entry.getValue().getType();
                if (type.equals(int.class)) {
                    sb.append(", " + key + " 
                } else if (type.equals(boolean.class)) {

                } else if (type.equals(String.class)) {

                }
            }
            */
            sb.append(");");
            db.execSQL(sb.toString());
        }

        public void clearDb(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + HEAD_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SYSTEMPKG_TABLE);
            createHeadTable(db);
            createSystemPkgTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"onUpgrade");
            clearDb(db);
        }
        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG,"onDowngrade");
            clearDb(db);
        }
    }


    Context mCtx;
    DbHelper helper = null;

    public PackageParserDb(Context c) {
        mCtx = c;
        helper = new DbHelper(c, DB_VERSION);
    }


    static void fillContentValues(ContentValues cv, PackageParser.Package pkg) {
        try {
        	byte[] bytes = com.android.packagedb.util2.Helper.serializePackage(pkg);
            cv.put(PackageTableColumns._CONTENT, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addPkgParserData(String apkPath) {
        File apkfile = new File(apkPath);
        //todo : flags
        int flags = PackageParser.PARSE_IS_SYSTEM | PackageParser.PARSE_MUST_BE_APK;

        PackageParser pp = new PackageParser();
        PackageParser.Package pkg = null;
        try {
        pkg = pp.parsePackage(apkfile, flags);
        } catch (PackageParser.PackageParserException e) {
            e.printStackTrace();
        }
        if (pkg == null) {
            return false;
        }
        return addPkgParserData(pkg);
    }


    public boolean addPkgParserData(PackageParser.Package pkg) {

        // todo: package parser update point
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        ContentValues cv = new ContentValues();
        fillContentValues(cv,pkg);
        cv.put(PackageTableColumns._CODEPATH, pkg.codePath);
        cv.put(PackageTableColumns._PACKAGENAME, pkg.packageName);
        db.insert(SYSTEMPKG_TABLE,null,cv);
        cv.clear();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return true;
    }

    public PackageParser.Package parsePkgFromCursor(Cursor cursor) {
        //PackageParser.Package pkg = new PackageParser.Package(cursor.getString(cursor.getColumnIndex(PackageTableColumns._PACKAGENAME)));
        PackageParser.Package pkg = null;
        try {
            byte[] content = cursor.getBlob(cursor.getColumnIndex(PackageTableColumns._CONTENT));
            pkg = com.android.packagedb.util2.Helper.parsePackage(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //pkg.codePath = cursor.getString(cursor.getColumnIndex(PackageTableColumns._CODEPATH));
        return pkg;
    }

    @Override
    public PackageParser.Package getPkgParserData(String apkPath) {
        //todo: package parser update point
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] args = {new String(apkPath)};
        Cursor cursor = db.query(SYSTEMPKG_TABLE, null , PackageTableColumns._CODEPATH + " = ?", args, null, null, null);
        PackageParser.Package pkg = null;

        if (cursor.getCount()==1) {
            cursor.moveToFirst();
            pkg = parsePkgFromCursor(cursor);
        }
        db.close();
        return pkg;
    }

    @Override
    public boolean removePkgParserData(String apkPath) {
        return false;
    }

    @Override
    public boolean isUpdateNeeded(String apkPath) {
        return false;
    }

    @Override
    public boolean updatePkgParserData(String apkPath) {
        return removePkgParserData(apkPath) && addPkgParserData(apkPath);
    }

    @Override
    public void clear() {

        SQLiteDatabase db = helper.getWritableDatabase();
        helper.clearDb(db);
        db.close();
    }

    @Override
    public boolean isClearNeeded() {
        return false;
    }


    //test api

    public void testLowerVersion() {
        helper.close();
        helper = new DbHelper(mCtx, DB_VERSION-1);
    }

    public void testHigherVersion() {
        helper.close();
        helper = new DbHelper(mCtx, DB_VERSION+1);
    }



}
