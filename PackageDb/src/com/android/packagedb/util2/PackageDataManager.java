package com.android.packagedb.util2;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;


public class PackageDataManager {

    static final String TAG = "PkgDataM";
    private static PackageDataManager instance;

    private static final int MAX_DUMP_THREADS = 2;
    private static final int GET_PKG_TIMEOUT = 3000;
    
    
    
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
        //public static final String _PACKAGENAME = "packageName";
        public static final String _LASTMODEFY = "modify";
        public static final String _SIZE = "size";
        public static final String _CONTENT = "content";
        

    }

    class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context, int version) {
            super(context, DB_NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createHeadTable(db);
            createPkgTable(db,SYSTEMPRIVAPPPKG_TABLE);
            createPkgTable(db,SYSTEMAPPPKG_TABLE);
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
        private void createPkgTable( SQLiteDatabase db, String tableName) {

            StringBuilder sb = new StringBuilder("CREATE TABLE " + tableName + " (");
            sb.append(PackageTableColumns._CODEPATH + " TEXT PRIMARY KEY");
            //sb.append(", " + PackageTableColumns._PACKAGENAME + " TEXT");
            sb.append(", " + PackageTableColumns._LASTMODEFY + " LONG");
            sb.append(", " + PackageTableColumns._SIZE + " LONG");
            sb.append(", " + PackageTableColumns._CONTENT + " TEXT");
            sb.append(");");
            db.execSQL(sb.toString());
        }

        public void clearDb(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + HEAD_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SYSTEMAPPPKG_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SYSTEMPRIVAPPPKG_TABLE);
            createHeadTable(db);
            createPkgTable(db,SYSTEMPRIVAPPPKG_TABLE);
            createPkgTable(db,SYSTEMAPPPKG_TABLE);
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
    SQLiteDatabase mDb = null;
    boolean isWriting = false;

    ArrayMap<String, PkgWraper> appDataToRead = new ArrayMap<String, PkgWraper>();
    ArrayMap<String, PkgWraper> appDataToWrite = new ArrayMap<String, PkgWraper>();
    
    ExecutorService readTasks = null;
    
    
    private PackageDataManager(Context c) {
        mCtx = c;
        helper = new DbHelper(c, DB_VERSION);
        CertificateGhost g = new CertificateGhost(null);
        ManifestDigestGhost m = new ManifestDigestGhost(null);
    }

    public static PackageDataManager getInstance(Context c) {
    	if (instance == null) {
    		instance = new PackageDataManager(c);
    	}
    	return instance;
    }
    
    public static class PkgWraper {
    	long modifyTime;
    	long size;
    	String path;
    	byte[] content;
    	PackageParser.Package pkg;
    	boolean beScaned = false;
    }
    
    private void initDb() {
    	if (mDb == null) {
    		mDb = helper.getWritableDatabase();
    		isWriting =false;
    	}
    }

    public void closeDb() {
    	if (mDb != null) {
    		mDb.close();
    		mDb = null;
    	}
    }
    
    private static final boolean isApkFile(File file) {
        return isApkPath(file.getName());
    }

    private static boolean isApkPath(String path) {
        return path.endsWith(".apk");
    }
    
    private static File getApkFile(String path) {
    	File apkPath = new File(path);
    	if (apkPath.isDirectory()) {
    	 final File[] files = apkPath.listFiles();
         for (File file : files) {
             if (isApkFile(file)) {
            	 return file;
             }
         }
    	} else if (isApkFile(apkPath)) {
    		return apkPath;
    	}
         return null;
    }
    
    public boolean addPkgParserData(PackageParser.Package pkg) {
    	if (pkg == null) {
    		return false;
    	}
        if (pkg.baseCodePath == null) {
        	return false;
        }
        if (isWriting) {
        	return false;
        }
    	PkgWraper pw = new PkgWraper();
        pw.path = pkg.codePath;
        File apkFile = new File(pkg.baseCodePath);
        pw.modifyTime = apkFile.lastModified();
        pw.size = apkFile.length();
        pw.content = Helper.serializePackage(pkg);
        appDataToWrite.put(pw.path, pw);
        return true;
    }
    
    public void saveDataToDb() {
    	if (isWriting) {
    		return;
    	}
    	isWriting = true;
    	initDb();
    	ContentValues cv = new ContentValues();
        Set<String> keySet = appDataToWrite.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            PkgWraper pw = appDataToWrite.get(key);
	            if (pw != null && pw.content != null) {
			        cv.put(PackageTableColumns._CODEPATH, pw.path);
			        //cv.put(PackageTableColumns._PACKAGENAME, pw.pkg.packageName);
			        cv.put(PackageTableColumns._LASTMODEFY, pw.modifyTime);
			        cv.put(PackageTableColumns._SIZE, pw.size);
			        cv.put(PackageTableColumns._CONTENT, pw.content);
		        if (pw.path.startsWith("/system/priv-app")) {
		        	mDb.insertWithOnConflict(SYSTEMPRIVAPPPKG_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
		        } else if (pw.path.startsWith("/system/app")) {
		        	mDb.insertWithOnConflict(SYSTEMAPPPKG_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
		        } else {
		        	//do notthing
		        }
            }
	        cv.clear();
        }
        closeDb();
        appDataToWrite.clear();
        isWriting = false;
    }
      
    public PackageParser.Package getPkgParserData(String apkPath) {
    	PkgWraper pw = appDataToRead.get(apkPath);
    	if (pw == null) {
    		return null;
    	}
    	File f = getApkFile(apkPath);
    	if (f.lastModified() == pw.modifyTime && f.length() == pw.size) {
        	synchronized (pw){
        		if (pw.pkg != null) {
        			pw.beScaned = true;
        			return pw.pkg;
        		}
        		try {
					pw.wait(GET_PKG_TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
        		if (pw.pkg != null) {
        			pw.beScaned = true;
        			return pw.pkg;
        		}
        	}
    	}
    	return null;
    }

    static class DumpPkgFromGhost implements Runnable {
    	final PkgWraper mPw;
    	public DumpPkgFromGhost(PkgWraper pw) {
    		mPw = pw;
    	}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mPw == null) return;
			synchronized (mPw){
				mPw.pkg = Helper.parsePackage(mPw.content);
				mPw.notify();
			}
		}
    }
    public void readDbData() {
    	if (appDataToRead.size()>0 ) {
    		return;
    	}
    	initDb();
    	Cursor cursor = mDb.query(SYSTEMPRIVAPPPKG_TABLE, null , null, null, null, null, null);
    	int pathPos = cursor.getColumnIndex(PackageTableColumns._CODEPATH);
    	int lmPos = cursor.getColumnIndex(PackageTableColumns._LASTMODEFY);
    	int sizePos = cursor.getColumnIndex(PackageTableColumns._SIZE);
    	int ctPos = cursor.getColumnIndex(PackageTableColumns._CONTENT);
    	
    	readTasks = Executors.newFixedThreadPool(MAX_DUMP_THREADS);
    	
        while (cursor.moveToNext()) {
        	PkgWraper pw = new PkgWraper();
            pw.path = cursor.getString(pathPos);
        	pw.modifyTime = cursor.getLong(lmPos);
            pw.size = cursor.getLong(sizePos);
            pw.content = cursor.getBlob(ctPos);
            appDataToRead.put(pw.path, pw);
            readTasks.submit(new DumpPkgFromGhost(pw));
        }
        cursor = mDb.query(SYSTEMAPPPKG_TABLE, null , null, null, null, null, null);
        while (cursor.moveToNext()) {
        	PkgWraper pw = new PkgWraper();
            pw.path = cursor.getString(pathPos);
        	pw.modifyTime = cursor.getLong(lmPos);
            pw.size = cursor.getLong(sizePos);
            pw.content = cursor.getBlob(ctPos);
            appDataToRead.put(pw.path, pw);
            readTasks.submit(new DumpPkgFromGhost(pw));
        }
        readTasks.shutdown();
		mDb.close();
		mDb = null;
    }
    
    //just for test
    public void waitForReadTasksFinish() {
    	if (readTasks != null) {
	    	try {
				readTasks.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    //it should be called after pkg scan
    public void cleanNotScanedPkg() {
    	//check readTasks finish or not just for Debug
    	if (readTasks != null && (!readTasks.isTerminated())) {
    		Log.w(TAG, " read task not finish !!!");
    	}
    	Collection<PkgWraper> set = appDataToRead.values();
    	if (set.size() > 0) {
    		initDb();
    	}
    	for (PkgWraper pw : set) {
    		if (!pw.beScaned) {
    			Log.i(TAG, " clean not scan pkg " + pw.path );
		        final String whereClause = PackageTableColumns._CODEPATH + " = ?";
		        String[] whereArgs = {pw.path};
		        if (pw.path.startsWith("/system/priv-app")) {
		        	mDb.delete(SYSTEMPRIVAPPPKG_TABLE, whereClause, whereArgs); 
		        } else if (pw.path.startsWith("/system/app")) {
		        	mDb.delete(SYSTEMAPPPKG_TABLE, whereClause, whereArgs); 
		        } else {
		        	//do notthing
		        }
    		}
    	}
    	closeDb();
    	appDataToRead.clear();
    }
    
    public void clear() {
    	initDb();
        helper.clearDb(mDb);
        closeDb();
    }

    public boolean isClearNeeded() {
        return false;
    }

}
