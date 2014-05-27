package org.dpl.adapter;

import org.dpl.entity.DplBaseEntity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

public class BaseCursorAdapter<T> extends SimpleCursorAdapter implements LoaderCallbacks<Cursor> {

	private final Context context;
	private final Class<T> cls;

	private final Uri uri;
	private final String[] projection;
	private final String selection;
	private final String[] selectionArgs;
	private final String sortOrder;

	public BaseCursorAdapter(Context context, Class<T> cls, int layout, Cursor cursor, String[] from, int[] to, int flags, Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		super(context, layout, cursor, from, to, flags);
		this.context = context;
		this.cls = cls;
		this.uri = uri;
		this.projection = projection;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.sortOrder = sortOrder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getItem(int position) {
		Cursor cursor = (Cursor) super.getItem(position);

		try {
			T entity = cls.newInstance();

			((DplBaseEntity<T>) entity).fillObject(cursor);

			return entity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		this.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		this.swapCursor(null);
	}
}
