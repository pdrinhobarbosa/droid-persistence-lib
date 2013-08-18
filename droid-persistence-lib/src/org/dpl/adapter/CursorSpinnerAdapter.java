package org.dpl.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.dpl.entity.DplBaseEntity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CursorSpinnerAdapter<T> extends CursorAdapter {

	private Context context;

	private LayoutInflater layoutInflater;

	private Class<?> cls;

	private String columnName;

	public CursorSpinnerAdapter(Context context, Cursor cursor, Class<?> cls, String columnName) {
		super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.cls = cls;
		this.columnName = columnName;
	}

	@Override
	public void bindView(View v, Context context, Cursor cursor) {
		((TextView) v).setText(cursor.getString(cursor.getColumnIndex(columnName)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return layoutInflater.inflate(android.R.layout.simple_spinner_item, null);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null)
			v = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);

		Cursor cursor = getCursor();
		cursor.moveToPosition(position);

		((TextView) v).setText(cursor.getString(cursor.getColumnIndex(columnName)));

		return v;
	}

	@Override
	public T getItem(int position) {
		T entity = null;

		try {
			@SuppressWarnings("unchecked")
			Constructor<T> entityConstructor = (Constructor<T>) cls.getConstructor(new Class[] {Context.class});

			entity = entityConstructor.newInstance(context);
			((DplBaseEntity<?>) entity).fillObject(getCursor());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return entity;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
}