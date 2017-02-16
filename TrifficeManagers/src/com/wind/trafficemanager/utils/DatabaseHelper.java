package com.wind.trafficemanager.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wind.trafficemanager.entity.Appinfo;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper  extends SQLiteOpenHelper
{

    private static final String TABLE_NAME="test";
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version)
	{

		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub
		 // TODO Auto-generated method stub
        String sql = "CREATE TABLE " + TABLE_NAME + "("
                + "uid INTEGER(10),"
                + "wifi INTEGER(2),"
                + "mobile INTEGER(2)"
                + ")";
        db.execSQL(sql);
        Log.e("db","数据库创建成功");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
		
	}


	public Appinfo  queryByUid(int uid)
	{

		Cursor cursor = this.getWritableDatabase().query("test", new String[]{"uid","wifi","mobile"},
			"uid=?", new String[]{String.valueOf(uid)}, null, null, null);
		Log.e("db","uid"+uid);
		Appinfo appinfo = new Appinfo();
		if (cursor.moveToNext()){
			int wifi = cursor.getInt(cursor.getColumnIndex("wifi"));
			int mobile = cursor.getInt(cursor.getColumnIndex("mobile"));
			appinfo.setUid(uid);
			appinfo.setMobile(mobile);
			appinfo.setWifi(wifi);
		}
		cursor.close();
		Log.e("db","查询成功");
		Log.e("db",appinfo.toString());
		return appinfo;
	}
	public void insert(int uid,int type,int value)
	{
		ContentValues values = new ContentValues();
		values.put("uid", uid);
		if (type==1) {
			values.put("wifi", value);
		}else {
			values.put("mobile", value);
		}
		this.getWritableDatabase().insert("test", null,values);
		Log.e("db","插入成功");
	}
	public void update(int uid,int type,int value)
	{
		ContentValues values = new ContentValues();
		if (type==1) {
			values.put("wifi", value);
		}else {
			values.put("mobile", value);
		}
		this.getWritableDatabase().update("test",values,"uid=?",new String[]{String.valueOf(uid)});
		queryByUid(uid);
		Log.e("db","更新");
	}
	public List  queryByWifi(int wifi)
	{
		List list = new ArrayList();
		Cursor cursor = this.getWritableDatabase().query("test", new String[]{"uid","wifi"},
				"wifi=?", new String[]{String.valueOf(wifi)}, null, null, null);
		Log.e("db","wifi"+wifi);
		if (cursor.moveToNext()){
			int uid = cursor.getInt(cursor.getColumnIndex("uid"));
			list.add(uid);
		}
		cursor.close();
		return list;
	}
	public List queryBymobile(int mobile)
	{
		List list = new ArrayList();
		Cursor cursor = this.getWritableDatabase().query("test", new String[]{"uid","mobile"},
				"mobile=?", new String[]{String.valueOf(mobile)}, null, null, null);
		Log.e("db","mobile"+mobile);
		if (cursor.moveToNext()){
			int uid = cursor.getInt(cursor.getColumnIndex("uid"));
			list.add(uid);
		}
		cursor.close();
		return list;
	}

}
