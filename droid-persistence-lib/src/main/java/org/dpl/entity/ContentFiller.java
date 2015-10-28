package org.dpl.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.ContentValues;

public class ContentFiller {

	private ContentValues values;
	private ArrayList<Field> lists;
	private Class<?> superClass;

	public ContentValues getValues() {
		return values;
	}

	public void setValues(ContentValues values) {
		this.values = values;
	}

	public ArrayList<Field> getLists() {
		return lists;
	}

	public void setLists(ArrayList<Field> lists) {
		this.lists = lists;
	}

	public Class<?> getSuperClass() {
		return superClass;
	}

	public void setSuperClass(Class<?> superClass) {
		this.superClass = superClass;
	}

}