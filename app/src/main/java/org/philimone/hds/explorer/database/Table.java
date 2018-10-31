package org.philimone.hds.explorer.database;

import android.content.ContentValues;

public interface Table {

	int getId();

	String getTableName();
	
	ContentValues getContentValues();
	
	String[] getColumnNames();
}
