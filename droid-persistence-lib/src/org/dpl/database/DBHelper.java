package org.dpl.database;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	public static final String CATEGORY = DBHelper.class.getSimpleName();

	private Context mContext;
	
	private DbMigrationHelper migrationHelper;

	public DBHelper(Context context, Class<?> R) {
		super(context, getDBName(context) + ".db", null, getDBVersion(context));
		this.mContext = context;
		
		migrationHelper = new DbMigrationHelper(mContext, R);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		migrationHelper.createDatabase(db, getDBVersion(mContext), getDBName(mContext) + "%version%");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		migrationHelper.upgradeDatabase(db, oldVersion, newVersion, getDBName(mContext) + "%version%");
	}
	

	private static String getDBName(Context context) {
		String dbName = getMetaDataString(context, "DB_NAME");

		if (dbName == null) {
			dbName = "Application";
		}

		return dbName;
	}

	private static int getDBVersion(Context context) {
		Integer dbVersion = getMetaDataInteger(context, "DB_VERSION");

		if ((dbVersion == null) || (dbVersion.intValue() == 0)) {
			dbVersion = Integer.valueOf(1);
		}

		return dbVersion.intValue();
	}

	private static String getMetaDataString(Context context, String name) {
		String value = null;

		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 128);
			value = ai.metaData.getString(name);
		} catch (Exception e) {
			Log.w(CATEGORY, "Couldn't find meta data string: " + name);
		}

		return value;
	}

	private static Integer getMetaDataInteger(Context context, String name) {
		Integer value = null;

		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 128);
			value = Integer.valueOf(ai.metaData.getInt(name));
		} catch (Exception e) {
			Log.w(CATEGORY, "Couldn't find meta data string: " + name);
		}

		return value;
	}
}