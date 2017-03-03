package com.android.packagedb.util2;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;


public class PackageDataManager {

    static final String TAG = "PackageParserDb";

    static final String DB_NAME = "package.db";
    static final int DB_VERSION = 2;
    static final String HEAD_TABLE = "head";
    static final String SYSTEMAPPPKG_TABLE = "systemapp";
    static final String SYSTEMPRIVAPPPKG_TABLE = "systemprivapp";
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
            createSystemPrivAppPkgTable(db);
            createSystemAppPkgTable(db);
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
        private void createSystemPrivAppPkgTable(SQLiteDatabase db) {

            StringBuilder sb = new StringBuilder("CREATE TABLE " + SYSTEMAPPPKG_TABLE + " (");
            sb.append(PackageTableColumns._CODEPATH + " TEXT PRIMARY KEY");
            sb.append(", " + PackageTableColumns._PACKAGENAME + " TEXT");
            sb.append(", " + PackageTableColumns._CONTENT + " TEXT");
            sb.append(");");
            db.execSQL(sb.toString());
        }
        private void createSystemAppPkgTable(SQLiteDatabase db) {

            StringBuilder sb = new StringBuilder("CREATE TABLE " + SYSTEMPRIVAPPPKG_TABLE + " (");
            sb.append(PackageTableColumns._CODEPATH + " TEXT PRIMARY KEY");
            sb.append(", " + PackageTableColumns._PACKAGENAME + " TEXT");
            sb.append(", " + PackageTableColumns._CONTENT + " TEXT");
            sb.append(");");
            db.execSQL(sb.toString());
        }

        public void clearDb(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + HEAD_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SYSTEMAPPPKG_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SYSTEMPRIVAPPPKG_TABLE);
            createHeadTable(db);
            createSystemPrivAppPkgTable(db);
            createSystemAppPkgTable(db);
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
    boolean isWriting = false;
    public PackageDataManager(Context c) {
        mCtx = c;
        helper = new DbHelper(c, DB_VERSION);
    }


    static void fillContentValues(ContentValues cv, PackageParser.Package pkg) {
        try {
        	byte[] bytes = Helper.serializePackage(pkg);
            cv.put(PackageTableColumns._CONTENT, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addPkgParserData(PackageParser.Package pkg) {
    	initDb();
    	if (!isWriting) {
    		mDb.beginTransaction();
    		isWriting =true;
    	}
        ContentValues cv = new ContentValues();
        cv.put(PackageTableColumns._CODEPATH, pkg.codePath);
        cv.put(PackageTableColumns._PACKAGENAME, pkg.packageName);
        fillContentValues(cv,pkg);
        if (pkg.codePath.startsWith("/system/priv-app")) {
        	mDb.insert(SYSTEMPRIVAPPPKG_TABLE,null,cv);
        } else if (pkg.codePath.startsWith("/system/app")) {
        	mDb.insert(SYSTEMAPPPKG_TABLE,null,cv);
        }
        cv.clear();
        return true;
    }

    public PackageParser.Package parsePkgFromCursor(Cursor cursor) {
        PackageParser.Package pkg = null;
        try {
            byte[] content = cursor.getBlob(cursor.getColumnIndex(PackageTableColumns._CONTENT));
            pkg = Helper.parsePackage(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkg;
    }
    SQLiteDatabase mDb = null;
    private void initDb() {
    	if (mDb == null) {
    		mDb = helper.getWritableDatabase();
    		isWriting =false;
    	}
    }

    public void close() {
    	if (mDb != null) {
    		finishWriteDb();
    		mDb.close();
    		mDb = null;
    	}
    }
    
    private void finishWriteDb() {
    	if (isWriting) {
	        mDb.setTransactionSuccessful();
	        mDb.endTransaction();
	        isWriting =false;
    	}
    }
    
    
    public PackageParser.Package getPkgParserData(String apkPath) {
        //todo: package parser update point
    	initDb();
    	finishWriteDb();
        String[] args = {new String(apkPath)};
        Cursor cursor = null;
        if (apkPath.startsWith("/system/priv-app")) {
        	cursor = mDb.query(SYSTEMPRIVAPPPKG_TABLE, null , PackageTableColumns._CODEPATH + " = ?", args, null, null, null);
        } else if (apkPath.startsWith("/system/app")) {
        	cursor = mDb.query(SYSTEMAPPPKG_TABLE, null , PackageTableColumns._CODEPATH + " = ?", args, null, null, null);
        } else {
        	return null;
        }
        PackageParser.Package pkg = null;

        if (cursor.getCount()==1) {
            cursor.moveToFirst();
            pkg = parsePkgFromCursor(cursor);
        }
        return pkg;
    }

    public boolean removePkgParserData(String apkPath) {
        return false;
    }

    public boolean isUpdateNeeded(String apkPath) {
        return false;
    }

    public boolean updatePkgParserData(PackageParser.Package pkg) {
        return removePkgParserData(pkg.codePath) && addPkgParserData(pkg);
    }

    public void clear() {
    	initDb();
        helper.clearDb(mDb);
        close();
    }

    public boolean isClearNeeded() {
        return false;
    }

}
