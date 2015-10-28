package org.dpl.entity;

import org.dpl.interfaces.EnumInterface;

public enum DataBaseAction implements EnumInterface{
	NONE(0,0),
	NEW(1,0),
	UPDATE(2,0),
	DELETE(3,0);

	private int id;
	
	private int i18n;
	
	private DataBaseAction(int id, int i18n) {
		this.id = id;
		this.i18n = i18n;
	}

	public int getId() {
		return id;
	}

	public int getI18nKey() {
		return i18n;
	}
}
