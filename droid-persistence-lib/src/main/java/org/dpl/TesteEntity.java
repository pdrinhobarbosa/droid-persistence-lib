package org.dpl;

import org.dpl.entity.DplBaseEntity;

import android.database.Cursor;


public class TesteEntity extends DplBaseEntity<TesteEntity> {

    private static final long serialVersionUID = -6593771266286238088L;

    public TesteEntity() {
        super();
    }

    public TesteEntity(Cursor cursor) {
        super(cursor);
    }


}
