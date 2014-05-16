package org.dpl;

import org.dpl.database.DBHelper;
import org.dpl.entity.DplBaseEntity;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

public abstract class DplProvider extends ContentProvider {

	/**
	 * @return "content://"
	 */
	public static final String CONTENT = "content://";

	/**
	 * @return "/"
	 */
	public static final String SEPARATOR = "/";

	/**
	 * @return "/#"
	 */
	public static final String SEPARATOR_FOR_ID_URI = SEPARATOR + '#';

	/**
	 * @return "/*"
	 */
	public static final String SEPARATOR_FOR_PATH_URI = SEPARATOR + '*';

	/**
	 * @return " = ?"
	 */
	public static final String SELECTION_EQUALS = " = ?";

	/**
	 * @return " != ?"
	 */
	public static final String SELECTION_NOT_EQUALS = " != ?";

	/**
	 * @return " AND "
	 */
	public static final String SELECTION_AND = " AND ";

	/**
	 * @return " OR "
	 */
	public static final String SELECTION_OR = " OR ";

	/**
	 * @return " IS NULL"
	 */
	public static final String SELECTION_IS_NULL = " IS NULL";

	/**
	 * @return " IS NOT NULL"
	 */
	public static final String SELECTION_IS_NOT_NULL = " IS NOT NULL";

	/**
	 * @return " IN(?)"
	 */
	public static final String SELECTION_IN = " IN(?)";

	public static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	/**
	 * Fill UriMatcher with URIs from your application.
	 * Use the static final attribute 'URI_MATCHER'.
	 * 
	 * Ex.: URI_MATCHER.addURI(getAuthority(), getTable(Entity.CONTENT_URI), ENTITY_CODE);
	 * URI_MATCHER.addURI(getAuthority(), getTable(Entity.CONTENT_URI) + SEPARATOR_FOR_ID_URI,
	 * ENTITY_CODE_ID);
	 * 
	 * When 'Entity.CONTENT_URI' is a URI static reference for your Entity URI.
	 * And 'ENTITY_CODE' and 'ENTITY_CODE_ID' ara static references for your Entity URI code;
	 */
	public abstract void fillUriMatcher();

	/**
	 * Register the observers to your tables that will be sent to server
	 */
	protected abstract void registerContentObeservers();

	/**
	 * Get Table name
	 * 
	 * @param uri
	 * @return Table name from your Entity registered by URI_MATCHER for this URI.
	 */
	public abstract String getTable(Uri uri);

	/**
	 * You can notify another URIs that data change
	 * 
	 * @param uri
	 */
	public abstract void notifyExtraUris(Uri uri);

	/**
	 * Called before delete a register.
	 * You can map to delete relationships from register that would be deleted.
	 */
	public abstract void deleteRalationships(Uri uri, String selection, String[] selectionArgs);

	/**
	 * R class from your application
	 * 
	 * @return R class from your application
	 */
	public abstract Class<?> getRClass();

	private DBHelper mSQLiteHelper;

	@Override
	public boolean onCreate() {
		mSQLiteHelper = new DBHelper(getContext(), getRClass());
		fillUriMatcher();
		registerContentObeservers();
		return true;
	}

	public int match(Uri uri) {
		return URI_MATCHER.match(uri);
	}

	/**
	 * By default, the will return the follow String:
	 * "<your_app_package_name> + .provider"
	 * 
	 * @param context
	 * @return String name from your authority.
	 */
	public static String getAuthority(Context context) {
		return context.getPackageName() + ".provider";
	}

	/**
	 * By default, the lib will return the follow Stirng:
	 * "content://<your_app_package_name>.provider/<your_entity_simple_name>"
	 * 
	 * @param context
	 * @param cls
	 *            - Your Entity
	 * @return String URI from your Entity.
	 */
	public static Uri getContentUri(Context context, Class<?> cls) {
		return Uri.parse(getContentUriBase(context) + cls.getSimpleName());
	}

	public static String getContentUriBase(Context context) {
		return CONTENT + getAuthority(context) + SEPARATOR;
	}

	/**
	 * By default, the lib will return the follow Stirng:
	 * "content://<your_app_package_name>.provider/
	 * <table>
	 * "
	 * 
	 * @param context
	 * @param table
	 *            - Your Enum table name
	 * @return String URI from your Entity.
	 */
	public static Uri getContentUri(Context context, String table) {
		return Uri.parse(CONTENT + getAuthority(context) + SEPARATOR + table);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = getTable(uri);

		selection = fillSelection(uri, selection);

		deleteRalationships(uri, selection, selectionArgs);

		try {
			int count = mSQLiteHelper.getWritableDatabase().delete(table, selection, selectionArgs);

			if (count > 0) {
				Log.i(DBHelper.CATEGORY, String.format("%d rows of %s deleted!", count, table));

				getContext().getContentResolver().notifyChange(uri, null);
				notifyExtraUris(uri);
			}

			return count;
		} catch (Exception e) {
			// TODO: Colocar msg de erro ao deletar
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		String table = getTable(uri);

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if ((uri == null) || ((table == null) || (table.length() <= 0))) {
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		long id = mSQLiteHelper.getWritableDatabase().insert(table, null, values);
		if (id > 0) {
			Log.i(DBHelper.CATEGORY, "New register " + table + " created. Id = " + id);
			Uri notifyUri = ContentUris.withAppendedId(uri, id);
			getContext().getContentResolver().notifyChange(notifyUri, null);
			notifyExtraUris(uri);
			return notifyUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		String table = getTable(uri);

		int numInserts = 0;
		for (ContentValues contentValues : values) {
			mSQLiteHelper.getWritableDatabase().insert(table, null, contentValues);
		}

		numInserts = values.length;
		Log.i(DBHelper.CATEGORY, numInserts + " new register created.");

		getContext().getContentResolver().notifyChange(uri, null);
		notifyExtraUris(uri);

		return numInserts;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String table = getTable(uri);

		selection = fillSelection(uri, selection);

		Cursor cursor = mSQLiteHelper.getWritableDatabase().query(table, projection, selection, selectionArgs, null, null, sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	/* Usar:
	 * Uri uri = Uri.withAppendedPath(CaraterGeralProvider.CONTENT_URI, "getByIndex/"+args.getInt("indice"));
	 * */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String table = getTable(uri);

		selection = fillSelection(uri, selection);

		int updatedRows = mSQLiteHelper.getWritableDatabase().update(table, values, selection, selectionArgs);

		Log.i(DBHelper.CATEGORY, String.format("%d rows of %s updated!", updatedRows, table));

		getContext().getContentResolver().notifyChange(uri, null);
		notifyExtraUris(uri);

		return updatedRows;
	}

	private String fillSelection(Uri uri, String selection) {
		Integer id = null;
		try {
			id = Integer.valueOf(uri.getLastPathSegment());
		} catch (NumberFormatException nfe) {}

		if (id != null) {
			selection = ((selection != null) ? selection + " AND " : "") + DplBaseEntity._ID + " = " + id;
		}

		return selection;
	}

	public DBHelper getSQLiteHelper() {
		return mSQLiteHelper;
	}

	public void setSQLiteHelper(DBHelper mSQLiteHelper) {
		this.mSQLiteHelper = mSQLiteHelper;
	}
}
