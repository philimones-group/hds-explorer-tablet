package net.manhica.dss.explorer.database;

import java.util.Collection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Database {
	
	public static final String DATABASE_NAME = "clip_explorer.db";
	public static final int DATABASE_VERSION = 3;
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;
	
	public Database(Context context) {
		dbHelper = new DatabaseHelper(context);
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}

    public void close() {
	    dbHelper.close();
	}

	public boolean isOpen(){
		return database.isOpen();
	}

	public void beginTransaction(){
		database.beginTransaction();
	}

	public void endTransaction(){
		database.endTransaction();
	}

	public void setTransactionSuccessful(){
		database.setTransactionSuccessful();
	}

	public void dropAllTables(){
		for (String t : DatabaseHelper.ALL_TABLES){
			database.execSQL("DROP TABLE IF EXISTS " + t );
		}
	}
    
    public long insert(Table entity){  	    	
    	long insertId = -1;
    	
    	insertId = database.insert(entity.getTableName(), null,  entity.getContentValues());
    	
    	return insertId;
    }
    
    public long insert(Collection<? extends Table> entities){  	    	
    	long insertId = -1;
    	
    	for (Table entity : entities){
    		insertId = database.insert(entity.getTableName(), null,  entity.getContentValues());
    	}
    	
    	return insertId;
    }
    
    public int delete(Class<? extends Table> table, String whereClause, String[] whereArgs){
    	Table entity = newInstance(table);
    	
    	int deleteRows = database.delete(entity.getTableName(), whereClause, whereArgs);
    	return deleteRows;
    }
    
    public int update(Class<? extends Table> table, ContentValues values, String whereClause, String[] whereArgs){    	
    	Table entity = newInstance(table);
    	
    	int rows = database.update(entity.getTableName(), values, whereClause, whereArgs);
    	
    	return rows;
    }
    
    public Cursor query(Class<? extends Table> table, String selection, String[] selectionArgs, String groupBy, String having, String orderBy){    	
    	Table entity = newInstance(table);
    	
    	Cursor cursor = database.query(entity.getTableName(), entity.getColumnNames(), selection, selectionArgs, groupBy, having, orderBy);
        	
    	return cursor;
    }
    
    public Cursor query(Class<? extends Table> table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy){
    	Table entity = newInstance(table);
    	
    	Cursor cursor = database.query(entity.getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy);
        	
    	return cursor;
    }
    
    private Table newInstance(Class<? extends Table> entity){
    	try {
			Table obj =  entity.newInstance();
			return obj;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
    }

}
