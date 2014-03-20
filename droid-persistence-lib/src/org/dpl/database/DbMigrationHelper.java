package org.dpl.database;

import java.io.IOException;
import java.io.InputStream;

import org.dpl.util.IOUtils;
import org.dpl.util.ResourcesUtil;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbMigrationHelper {

	private static final String STATEMENT_SEPARATOR = ";\n";

	private static final String TAG = DbMigrationHelper.class.getSimpleName();

	private final Context context;
	private final Class<?> mR;

	public DbMigrationHelper(Context context, Class<?> r) {
		this.context = context;
		this.mR = r;
	}

	/**
	 * Used when the database is <b>created</b> (on application first execution)
	 * This method read all scripts of database from the first version until
	 * <code>currentVersion</code>
	 * 
	 * @param db
	 * 
	 * @param currentVersion
	 * 
	 * @param databaseName
	 *            This attribute should follow the following pattern<br>
	 *            -> <databaseName>%version%<br>
	 * 
	 *            The substring %version% will be replaced for the database version (1 to
	 *            currentVersion).
	 * 
	 * @see SQLiteOpenHelper#onCreate(SQLiteDatabase)
	 * 
	 */
	public void createDatabase(SQLiteDatabase db, int currentVersion, String databaseName) {
		loadAndExecute(db, 1, currentVersion, databaseName);
	}

	/**
	 * Used when the database is <b>updated</b> (on application first execution)
	 * This method read all scripts of database from the <code>(oldVersion + 1)</code> until
	 * <code>currentVersion</code>
	 * 
	 * @param db
	 * 
	 * @param oldVersion
	 * 
	 * @param newVersion
	 * 
	 * @param databaseName
	 *            This attribute should follow the following pattern:<br>
	 *            -> <databaseName>%version%<br>
	 * 
	 *            The substring %version% will be replaced for the database version (1 to
	 *            currentVersion).
	 * 
	 * @see SQLiteOpenHelper#onCreate(SQLiteDatabase)
	 */
	public void upgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion, String databaseName) {
		loadAndExecute(db, (oldVersion + 1), newVersion, databaseName);
	}

	private void loadAndExecute(SQLiteDatabase db, int initialScriptVersion, int finalScriptVersion, String databaseName) {
		for (int i = initialScriptVersion; i <= finalScriptVersion; i++) {
			String scriptFileName = getScriptFileName(i, databaseName);
			String scriptContent = loadScriptFile(scriptFileName);

			if (scriptContent.length() > 0) {
				executeScript(db, scriptContent, scriptFileName);
			}
		}
	}

	private void executeScript(SQLiteDatabase db, String scriptContent, String scriptFileName) {
		String[] statements = scriptContent.split(STATEMENT_SEPARATOR);

		for (int i = 0; i < statements.length; i++) {
			String statement = statements[i];
			try {
				db.execSQL(statement);
			} catch (SQLException e) {
				Log.w(TAG, String.format("Error on execute line %d file %s (%s)", i, scriptFileName, statement), e);
			}
		}
	}

	private String loadScriptFile(String scriptFileName) {
		String script = "";

		try {
			int rawFileId = ResourcesUtil.getResourceByName(mR, "raw", scriptFileName);
			script = loadScriptFile(rawFileId);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		return script;
	}

	private String getScriptFileName(int currentVersion, String databaseName) {
		return databaseName.replaceFirst("%version%", String.valueOf(currentVersion));
	}

	private String loadScriptFile(int rawId) {
		InputStream is = context.getResources().openRawResource(rawId);
		String script = "";

		try {
			script = IOUtils.toString(is);
		} catch (IOException e) {
			Log.e(TAG, "Erro ao ler arquivo de script", e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		return script;
	}
}
