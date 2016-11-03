package net.manhica.dss.explorer.database;

import android.content.ContentValues;

public interface Table {
	
	public String getTableName();
	
	public ContentValues getContentValues();
	
	public String[] getColumnNames();
}
