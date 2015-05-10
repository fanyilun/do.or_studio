package com.geeker.door.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author liushao
 * @version 2.0
 * @since 2013-3-13
 */
public class DbHelper extends SQLiteOpenHelper{
	
	private static final String DBNAME = "clock.db";
	
	public DbHelper(Context c){
		super(c, DBNAME, null, 1);
	} 

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql1 = "CREATE TABLE IF NOT EXISTS clock " +
				"(id integer primary key autoincrement ,hour integer ,minute integer ,requestCode integer ,onlineID integer," +
				"day integer ,ring text ,volume integer,tag text,reminder1 integer,reminder2 integer,reminder3 integer,reminder4 integer," +
				"cancelType integer,saveTime integer )";
		db.execSQL(sql1);
		
		String sql2 = "CREATE TABLE IF NOT EXISTS memo (id integer primary key autoincrement,title text,content text,time1 integer,time2 integer" +
				",ring text,vol integer,companies text,tags text,requestCode integer,onlineID integer,saveTime integer,switch1 integer,switch2 integer,switch3 integer,switch4 integer,switch5 integer)";
		db.execSQL(sql2);
		
		String sql3 = "CREATE TABLE IF NOT EXISTS request " +
				"(id integer primary key autoincrement,title text,content text,time integer,companies text,tags text,requestCode integer,onlineID integer,saveTime integer,switch1 integer,switch2 integer,switch3 integer,switch4 integer)";
		
		db.execSQL(sql3);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
