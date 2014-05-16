package org.dpl.entity;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.dpl.DplProvider;
import org.dpl.annotation.DplColumn;
import org.dpl.annotation.DplEnumList;
import org.dpl.annotation.DplIgnore;
import org.dpl.annotation.DplList;
import org.dpl.annotation.DplObject;
import org.dpl.annotation.DplTable;
import org.dpl.database.DBHelper;
import org.dpl.interfaces.EnumInterface;
import org.dpl.util.EnumUtils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public abstract class DplBaseEntity<T> implements BaseColumns, Serializable {

	private static final long serialVersionUID = -837161836030151140L;

	public static final String DATABASE_ACTION = "dataBaseAction";

	private Long _id;

	@DplIgnore
	private DataBaseAction dataBaseAction = DataBaseAction.NONE;

	private transient Context context;

	/**
	 * This lib always need a context to run the methods (save(), delete(), fillObject()...)
	 * If you use this constructor, call setContext() after call this.
	 */
	public DplBaseEntity() {

	}

	public DplBaseEntity(Context context) {
		this.context = context;
	}

	public DplBaseEntity(Context context, Cursor cursor) {
		this.context = context;
		fillObject(cursor);
	}

	public Long get_id() {
		return _id;
	}

	public void set_id(Long _id) {
		this._id = _id;
	}

	public DataBaseAction getDataBaseAction() {
		return dataBaseAction;
	}

	public void setDataBaseAction(DataBaseAction dataBaseAction) {
		this.dataBaseAction = dataBaseAction;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Uri getContentUri() {
		return Uri.parse(getContentUriBase() + getClass().getSimpleName());
	}

	public ContentValues toContentValues() {
		return getContentFiller(getClass(), true).getValues();
	}

	public ContentValues toContentValues(boolean withNullValues) {
		return getContentFiller(getClass(), withNullValues).getValues();
	}

	public String getContentUriBase() {
		return DplProvider.CONTENT + DplProvider.getAuthority(context) + DplProvider.SEPARATOR;
	}

	private boolean verifySuperClassTablesAndSave(Uri uri, ContentFiller contentFiller, boolean saveNullValues) {
		if ((contentFiller == null) || (contentFiller.getValues() == null) || (uri == null)) {
			throw new IllegalArgumentException();
		}

		boolean returnFlag = true;

		boolean isNewRegister = isNewRegister(uri);

		if (contentFiller.getSuperClass() != null) {
			ContentFiller superContentFiller = getContentFiller(contentFiller.getSuperClass(), saveNullValues);
			Uri superUri = DplProvider.getContentUri(context, contentFiller.getSuperClass());

			returnFlag = verifySuperClassTablesAndSave(superUri, superContentFiller, saveNullValues);
		}

		if (returnFlag) {
			if ((get_id() != null) && isNewRegister) {
				contentFiller.getValues().put(_ID, get_id());
			}

			/**
			 * Save if have some field to save in this table.
			 * This is used when have more than one table on this object (hierarchical tables).
			 * So if don't have any field in this ContentValue, maybe it was called to save some
			 * field in other table.
			 */
			if (contentFiller.getValues().size() > 0) {
				returnFlag = save(uri, contentFiller.getValues());
			}

			if (returnFlag && isNewRegister && (contentFiller.getLists() != null) && !contentFiller.getLists().isEmpty()) {
				for (Field field : contentFiller.getLists()) {
					DplEnumList dlpEnumList = null;

					String table = null;
					String objColumn = null;
					String enumColumn = null;
					if (field.isAnnotationPresent(DplEnumList.class)) {
						dlpEnumList = field.getAnnotation(DplEnumList.class);

						table = dlpEnumList.table();
						objColumn = dlpEnumList.objColumn();
						enumColumn = dlpEnumList.enumColumn();

						delete(table, objColumn, get_id());
					}

					try {
						Collection<?> list = (Collection<?>) getClass().getMethod(getGetter(field.getName())).invoke(this);

						if (list != null) {
							for (Object obj : list) {
								if (obj instanceof DplBaseEntity) {
									((DplBaseEntity<?>) obj).save();
								} else if ((obj instanceof EnumInterface) && field.isAnnotationPresent(DplEnumList.class)) {
									saveOnRelationshipTable(table, objColumn, get_id(), enumColumn, Long.valueOf(((EnumInterface) obj).getId()));
								}
							}
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return returnFlag;
	}

	public boolean save(Uri uri, ContentValues values) {
		return save(uri, values, null, null);
	}

	public boolean save(boolean saveNullValues) {
		ContentFiller contentFiller = getContentFiller(this.getClass(), saveNullValues);

		return verifySuperClassTablesAndSave(getContentUri(), contentFiller, saveNullValues);
	}

	/**
	 * Save only the values ​​reported by ContentValues​​.
	 * If you need to save values in more than one table, for hierarchical tables for example,
	 * use saveLoadedAttributesOnly() method to don't save values not loaded on object.
	 *
	 * @param values
	 * @return A boolean if was success or not the operation.
	 */
	public boolean save(ContentValues values) {
		return save(getContentUri(), values);
	}

	/**
	 * Save only the values ​​reported by ContentValues and use where clause​​.
	 * If you need to save values in more than one table, for hierarchical tables for example,
	 * use saveLoadedAttributesOnly() method to don't save values not loaded on object.
	 *
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 * @return A boolean if was success or not the operation.
	 */
	public boolean save(ContentValues values, String selection, String[] selectionArgs) {
		return save(getContentUri(), values, selection, selectionArgs);
	}

	/**
	 * Save all attributes of this object.
	 * If you need to save only some attributes, use saveLoadedAttributesOnly() method.
	 *
	 * @return
	 */
	public boolean save() {
		return save(true);
	}

	public boolean save(Uri uri, ContentValues values, String selection, String[] selectionArgs) throws IllegalArgumentException {
		if ((values == null) || (uri == null)) {
			throw new IllegalArgumentException();
		}

		if (isNewRegister(uri)) {
			Uri newUri = context.getContentResolver().insert(uri, values);

			try {
				long id = ContentUris.parseId(newUri);

				set_id(id);

				setDataBaseAction(DataBaseAction.NONE);

				return true;
			} catch (NumberFormatException e) {
				Log.e(DBHelper.CATEGORY, e.getMessage());
			} catch (UnsupportedOperationException e) {
				Log.e(DBHelper.CATEGORY, e.getMessage());
			}
		} else {
			if (selection == null) {
				selectionArgs = null;
				uri = ContentUris.withAppendedId(uri, get_id());
			}

			try {
				getContext().getContentResolver().update(uri, values, selection, selectionArgs);

				setDataBaseAction(DataBaseAction.NONE);

				return true;
			} catch (Exception e) {
				Log.e(DBHelper.CATEGORY, e.getMessage());
			}
		}

		return false;
	}

	/**
	 * Used for save the relationship from main object and the enum of list
	 *
	 * @param table
	 *            The table name from relationship entity mapped on provider
	 * @param objColumn
	 *            Column name of object.
	 * @param objId
	 *            Id of object.
	 * @param enumColumn
	 *            Column name of enum.
	 * @param enumId
	 *            Id of enum.
	 */
	private boolean saveOnRelationshipTable(String table, String objColumn, Long objId, String enumColumn, Long enumId) {
		Uri uri = getContentUri();

		if (uri != null) {
			ContentValues values = new ContentValues();

			values.put(objColumn, objId);
			values.put(enumColumn, enumId);

			Uri newUri = getContext().getContentResolver().insert(uri, values);

			long id = ContentUris.parseId(newUri);

			setDataBaseAction(DataBaseAction.NONE);

			return (id != -1);
		}

		return false;
	}

	/**
	 * used for delete all relationships registers of enum relationship table by objId
	 *
	 * @param target
	 *            The class from relationship entity mapped on provider
	 * @param mainObjColumn
	 *            Column name of main object.
	 * @param mainObjId
	 *            Id of main object.
	 */
	private void delete(String table, String objColumn, Long objId) {
		Uri uri = getContentUri();

		if (uri != null) {
			String where = objColumn + DplProvider.SELECTION_EQUALS;
			String[] selectionArgs = new String[] {objId.toString()};

			getContext().getContentResolver().delete(uri, where, selectionArgs);
		}
	}

	/**
	 * Delete all register to this table
	 *
	 * @return The number of rows deleted.
	 */
	public int deleteAll() {
		return delete(null, null, false);
	}

	/**
	 * Delete this register to database
	 *
	 * @return The number of rows deleted.
	 */
	public int delete() {
		return delete(null, null, true);
	}

	/**
	 * Delete from this table with a condition.
	 *
	 * @param where
	 * @param selectionArgs
	 * @return The number of rows deleted.
	 */
	public int delete(String where, String[] selectionArgs) {
		return delete(where, selectionArgs, false);
	}

	private int delete(String where, String[] selectionArgs, boolean byIdFlag) {
		Uri uri = getContentUri();

		if (byIdFlag) {
			uri = ContentUris.withAppendedId(uri, get_id());
		}

		Class<?> supeClass = getClass().getSuperclass();
		do {
			if (supeClass.isAnnotationPresent(DplTable.class)) {
				Uri uriSuperClass = DplProvider.getContentUri(getContext(), supeClass);

				getContext().getContentResolver().delete(uriSuperClass, DplBaseEntity._ID + DplProvider.SELECTION_EQUALS, new String[] {get_id().toString()});
			}

			supeClass = supeClass.getSuperclass();
		} while (supeClass != Object.class);

		return getContext().getContentResolver().delete(uri, where, selectionArgs);
	}

	/**
	 * List all
	 *
	 * @return ArrayList with all registers on this table
	 */
	public ArrayList<T> query() {
		return query(null, null, null, null);
	}

	public ArrayList<T> query(String selection, String[] selectionArgs) {
		return query(null, selection, selectionArgs, null);
	}

	public ArrayList<T> query(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		ArrayList<T> entities = new ArrayList<T>();
		Uri uri = getContentUri();

		Cursor cursor = null;
		try {
			cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

			if (cursor.moveToFirst()) {
				try {
					@SuppressWarnings("unchecked")
					final Constructor<T> entityConstructor = (Constructor<T>) getClass().getConstructor(new Class[] {Context.class});

					do {
						try {
							T entity = entityConstructor.newInstance(getContext());
							((DplBaseEntity<?>) entity).fillObject(cursor);

							entities.add(entity);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					} while (cursor.moveToNext());
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return entities;
	}

	public static int count(Context context, Class<?> cls) {
		return count(context, cls, null, null);
	}

	public static int count(Context context, Class<?> cls, String selection, String[] selectionArgs) {
		return count(context, DplProvider.getContentUri(context, cls), new String[] {"count(*) AS count"}, selection, selectionArgs);
	}

	public static int count(Context context, Uri uri, String selection, String[] selectionArgs) {
		return count(context, uri, new String[] {"count(*) AS count"}, selection, selectionArgs);
	}

	public static int count(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs) {
		int count = 0;

		Cursor countCursor = null;
		try {
			countCursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

			countCursor.moveToFirst();
			count = countCursor.getInt(0);
		} finally {
			if (countCursor != null) {
				countCursor.close();
			}
		}

		return count;
	}

	public boolean isNewRegister() {
		return isNewRegister(getContentUri());
	}

	public boolean isNewRegister(Uri uri) {
		int count = 0;
		if (get_id() != null) {
			String selection = DplBaseEntity._ID + DplProvider.SELECTION_EQUALS;
			String[] selectionArgs = new String[] {get_id().toString()};

			count = count(getContext(), uri, selection, selectionArgs);

			if (count <= 0) {
				setDataBaseAction(DataBaseAction.NEW);
			}
		}

		return ((get_id() == null) || (getDataBaseAction() == DataBaseAction.NEW));
	}

	public void fillObject(long _id) {
		set_id(_id);

		fillObject();
	}

	public void fillObject() {
		fillObject(null, null, null, null);
	}

	public void fillObject(String[] projection) {
		fillObject(projection, null, null, null);
	}

	public void fillObject(long _id, String[] projection) {
		set_id(_id);

		fillObject(projection, null, null, null);
	}

	public void fillObject(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Uri uri = getContentUri();
		if (selection == null) {
			uri = ContentUris.withAppendedId(uri, get_id());
		}

		Cursor cursor = null;
		try {
			cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

			if (cursor.moveToFirst()) {
				fillObject(cursor);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public ContentFiller getContentFiller(Class<?> cls, boolean saveNullValues) {
		ContentFiller contentFiller = new ContentFiller();
		contentFiller.setValues(new ContentValues());
		contentFiller.setLists(new ArrayList<Field>());

		Field fields[];
		ArrayList<Field> fieldsArray = new ArrayList<Field>();

		fieldsArray.addAll(Arrays.asList(cls.getDeclaredFields()));

		Class<?> superClass = cls.getSuperclass();
		while (!superClass.equals(Object.class)) {
			if (superClass.isAnnotationPresent(DplTable.class)) {
				/**
				 * Add DplBaseEntity Declared Fields to get _id on fields for this child table.
				 */
				fieldsArray.addAll(Arrays.asList(DplBaseEntity.class.getDeclaredFields()));

				contentFiller.setSuperClass(superClass);

				break;
			} else {
				fieldsArray.addAll(Arrays.asList(superClass.getDeclaredFields()));

				superClass = superClass.getSuperclass();
			}
		}

		fields = fieldsArray.toArray(new Field[fieldsArray.size()]);

		return fillContentFiller(fields, contentFiller, saveNullValues);
	}

	public ContentFiller fillContentFiller(Field[] fields, ContentFiller contentFiller, boolean saveNullValues) {

		for (Integer i = 0; i < fields.length; i++) {
			if (Modifier.isStatic(fields[i].getModifiers()) || fields[i].isAnnotationPresent(DplIgnore.class)) {
				continue;
			}

			String fieldName = fields[i].getName();
			String columnName = fieldName;

			if (fields[i].isAnnotationPresent(DplColumn.class)) {
				DplColumn dplColumn = fields[i].getAnnotation(DplColumn.class);
				String dplColumnName = dplColumn.name();
				if ((dplColumnName != null) && (dplColumnName.length() > 0)) {
					columnName = dplColumnName;
				}
			}

			if (fields[i].isAnnotationPresent(DplObject.class)) {
				DplObject dlpObject = fields[i].getAnnotation(DplObject.class);
				boolean saveFlag = dlpObject.save();

				try {
					Object objetoInterno = this.getClass().getMethod(getGetter(fieldName)).invoke(this);

					if ((objetoInterno != null) && (objetoInterno instanceof DplBaseEntity)) {
						DplBaseEntity<?> ent = (DplBaseEntity<?>) objetoInterno;

						if (saveFlag) {
							ent.save(saveNullValues);
						}

						Long idDoObjeto = ent.get_id();
						if ((idDoObjeto != null) && (idDoObjeto != -1)) {
							contentFiller.getValues().put(columnName, idDoObjeto);
						}
					}
				} catch (Exception e) {
					continue;
				}

				continue;

			} else {
				try {
					if (fields[i].isAnnotationPresent(DplList.class)) {
						contentFiller.getLists().add(fields[i]);

					} else if ((fields[i].getType() == String.class) || fields[i].getType().getName().equalsIgnoreCase("string")) {
						String value = (String) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if ((fields[i].getType() == Integer.class) || fields[i].getType().getName().equalsIgnoreCase("int")) {
						Integer value = (Integer) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if ((fields[i].getType() == Double.class) || fields[i].getType().getName().equalsIgnoreCase("double")) {
						Double value = (Double) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if ((fields[i].getType() == Float.class) || fields[i].getType().getName().equalsIgnoreCase("float")) {
						Float value = (Float) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if ((fields[i].getType() == Long.class) || fields[i].getType().getName().equalsIgnoreCase("long")) {
						Long value = (Long) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if ((fields[i].getType() == Boolean.class) || fields[i].getType().getName().equalsIgnoreCase("boolean")) {
						Boolean value = (Boolean) this.getClass().getMethod(getGetter(fieldName, true)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if (fields[i].getType() == Date.class) {
						Date date = (Date) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if (date != null) {
							contentFiller.getValues().put(columnName, date.getTime());
						} else if (saveNullValues) {
							String value = null;
							contentFiller.getValues().put(columnName, value);
						}
					} else if (fields[i].getType() == Calendar.class) {
						Calendar calendar = (Calendar) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if (calendar != null) {
							contentFiller.getValues().put(columnName, calendar.getTimeInMillis());
						} else if (saveNullValues) {
							String values = null;
							contentFiller.getValues().put(columnName, values);
						}

					} else if ((fields[i].getType() == Byte.class) || fields[i].getType().getName().equalsIgnoreCase("byte")) {
						Byte value = (Byte) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if (fields[i].getType().isArray() && fields[i].getType().getCanonicalName().equalsIgnoreCase("byte[]")) {
						byte[] value = (byte[]) this.getClass().getMethod(getGetter(fieldName)).invoke(this);

						if ((value != null) || saveNullValues) {
							contentFiller.getValues().put(columnName, value);
						}
					} else if (fields[i].getType().isEnum()) {
						Enum<?> enumObject = ((Enum<?>) this.getClass().getMethod(getGetter(fieldName)).invoke(this));

						if (enumObject != null) {
							if (enumObject instanceof EnumInterface) {
								EnumInterface enumInterface = (EnumInterface) enumObject;

								Integer enumId = (enumInterface != null) ? enumInterface.getId() : null;

								contentFiller.getValues().put(columnName, enumId);
							} else {
								contentFiller.getValues().put(columnName, enumObject.toString());
							}
						} else if (saveNullValues) {
							String values = null;
							contentFiller.getValues().put(columnName, values);
						}
					}
				} catch (Exception e) {
					Log.v(DplProvider.class.getName(), "Can't get object field '" + fieldName + "', from class " + this.getClass().getName(), e);
				}
			}
		}

		return contentFiller;
	}

	public void fillObject(Cursor cursor) {
		fillObject(cursor, true);
	}

	@SuppressWarnings("rawtypes")
	public void fillObject(Cursor cursor, boolean withJoins) {
		if ((this == null) || (cursor == null)) {
			return;
		}

		Field fields[];
		ArrayList<Field> fieldsArray = new ArrayList<Field>();

		fieldsArray.addAll(Arrays.asList(getClass().getDeclaredFields()));

		Class<?> superClass = getClass().getSuperclass();
		while (!superClass.equals(Object.class)) {
			fieldsArray.addAll(Arrays.asList(superClass.getDeclaredFields()));

			superClass = superClass.getSuperclass();
		}

		fields = fieldsArray.toArray(new Field[fieldsArray.size()]);

		for (Integer i = 0; i < fields.length; i++) {
			if (Modifier.isStatic(fields[i].getModifiers()) || fields[i].isAnnotationPresent(DplIgnore.class)) {
				continue;
			}

			String fieldName = fields[i].getName();
			String columnName = fieldName;

			if (fields[i].isAnnotationPresent(DplColumn.class)) {
				DplColumn dplColumn = fields[i].getAnnotation(DplColumn.class);
				String dplColumnName = dplColumn.name();
				if ((dplColumnName != null) && (dplColumnName.length() > 0)) {
					columnName = dplColumnName;
				}
			}

			try {
				if (fields[i].isAnnotationPresent(DplObject.class)) {
					Long id = cursor.getLong(cursor.getColumnIndex(columnName));

					DplBaseEntity entity = null;
					if ((id != null) && (id != 0)) {
						@SuppressWarnings("unchecked")
						final Constructor<? extends DplBaseEntity<?>> ec = (Constructor<DplBaseEntity<?>>) fields[i].getType().getConstructor(new Class[] {Context.class});

						entity = ec.newInstance(context);
						entity.set_id(id);
					}

					getClass().getMethod(getSetter(fieldName), fields[i].getType()).invoke(this, entity);

				} else if ((fields[i].getType() == String.class) || fields[i].getType().getName().equalsIgnoreCase("string")) {
					String value = cursor.getString(cursor.getColumnIndex(columnName));

					getClass().getMethod(getSetter(fieldName), String.class).invoke(this, value);

				} else if ((fields[i].getType() == Integer.class) || fields[i].getType().getName().equalsIgnoreCase("int")) {
					Integer value = null;
					if (!cursor.isNull(cursor.getColumnIndex(columnName))) {
						value = cursor.getInt(cursor.getColumnIndex(columnName));
					}

					getClass().getMethod(getSetter(fieldName), Integer.class).invoke(this, value);
				} else if ((fields[i].getType() == Double.class) || fields[i].getType().getName().equalsIgnoreCase("double")) {
					Double value = null;
					if (!cursor.isNull(cursor.getColumnIndex(columnName))) {
						value = cursor.getDouble(cursor.getColumnIndex(columnName));
					}

					getClass().getMethod(getSetter(fieldName), Double.class).invoke(this, value);
				} else if ((fields[i].getType() == Float.class) || fields[i].getType().getName().equalsIgnoreCase("float")) {
					Float value = null;
					if (!cursor.isNull(cursor.getColumnIndex(columnName))) {
						value = cursor.getFloat(cursor.getColumnIndex(columnName));
					}

					getClass().getMethod(getSetter(fieldName), Float.class).invoke(this, value);
				} else if ((fields[i].getType() == Long.class) || fields[i].getType().getName().equalsIgnoreCase("long")) {
					Long value = null;
					if (!cursor.isNull(cursor.getColumnIndex(columnName))) {
						value = cursor.getLong(cursor.getColumnIndex(columnName));
					}

					getClass().getMethod(getSetter(fieldName), Long.class).invoke(this, value);
				} else if ((fields[i].getType() == Boolean.class) || fields[i].getType().getName().equalsIgnoreCase("boolean")) {
					Boolean value = null;
					if (!cursor.isNull(cursor.getColumnIndex(columnName))) {
						value = (cursor.getInt(cursor.getColumnIndex(columnName)) > 0);
					}

					getClass().getMethod(getSetter(fieldName), Boolean.class).invoke(this, value);
				} else if ((fields[i].getType() == Byte.class) || fields[i].getType().getName().equalsIgnoreCase("byte")) {
					byte value = (byte) cursor.getInt(cursor.getColumnIndex(columnName));

					getClass().getMethod(getSetter(fieldName), Byte.class).invoke(this, value);
				} else if (fields[i].getType().isArray() && fields[i].getType().getCanonicalName().equalsIgnoreCase("byte[]")) {
					byte[] value = cursor.getBlob(cursor.getColumnIndex(columnName));

					getClass().getMethod(getSetter(fieldName), byte[].class).invoke(this, value);
				} else if (fields[i].getType() == Date.class) {
					Long long_date = cursor.getLong(cursor.getColumnIndex(columnName));

					if ((long_date != null) && (long_date > 0)) {
						Calendar c = Calendar.getInstance();
						c.setTimeInMillis(long_date);
						getClass().getMethod(getSetter(fieldName), Date.class).invoke(this, c.getTime());
					} else {
						getClass().getMethod(getSetter(fieldName), Date.class).invoke(this, long_date);
					}
				} else if (fields[i].getType() == Calendar.class) {
					Long long_date = cursor.getLong(cursor.getColumnIndex(columnName));

					if ((long_date != null) && (long_date > 0)) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(long_date);
						getClass().getMethod(getSetter(fieldName), Calendar.class).invoke(this, calendar);
					} else {
						getClass().getMethod(getSetter(fieldName), Calendar.class).invoke(this, long_date);
					}

				} else if (fields[i].getType().isEnum()) {

					Integer value = cursor.getInt(cursor.getColumnIndex(columnName));
					Enum[] enumConstants = (Enum[]) fields[i].getType().getEnumConstants();
					EnumInterface enumInt = EnumUtils.getById(value, (EnumInterface[]) enumConstants);

					getClass().getMethod(getSetter(fieldName), enumInt.getClass()).invoke(this, enumInt);

				}
			} catch (Exception e) {
				Log.v(DplProvider.class.getName(), "Can't fill object field '" + fieldName + "', from class " + getClass().getName());
			}
		}
	}

	@SuppressLint("DefaultLocale")
	private static String getSetter(String fieldName) {
		return "set" + fieldName.replaceFirst("" + fieldName.charAt(0), ("" + fieldName.charAt(0)).toUpperCase());
	}

	public static String getGetter(String fieldName) {
		return getGetter(fieldName, false);
	}

	@SuppressLint("DefaultLocale")
	private static String getGetter(String fieldName, boolean booleanField) {

		if (booleanField) {
			String methodName = "is" + fieldName.replaceFirst("" + fieldName.charAt(0), ("" + fieldName.charAt(0)).toUpperCase());
			return methodName;
		} else {
			String methodName = "get" + fieldName.replaceFirst("" + fieldName.charAt(0), ("" + fieldName.charAt(0)).toUpperCase());
			return methodName;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " id: " + get_id();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		DplBaseEntity<?> other = (DplBaseEntity<?>) obj;
		if (_id == null) {
			if (other._id != null) {
				return false;
			}
		} else if (!_id.equals(other._id)) {
			return false;
		}

		return true;
	}

}
