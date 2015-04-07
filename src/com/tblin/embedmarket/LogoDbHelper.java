package com.tblin.embedmarket;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;

import com.tblin.market.breakdown.ImageUtil;

public class LogoDbHelper extends SQLiteOpenHelper {

	private static final String TABLE_NAME = "app_logo";
	private static LogoDbHelper INSTANCE;

	// private static final String TAG = LogoDbHelper.class.toString();

	/**
	 * table key is _id-appid-pkg-logo-live_time
	 * 
	 * @param context
	 */
	private LogoDbHelper(Context context) {
		super(context, TABLE_NAME, null, 1);
	}
	
	public static LogoDbHelper getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new LogoDbHelper(context);
		}
		return INSTANCE;
	}
	
	public void clearTemp() {
		String sql = "delete from " + TABLE_NAME + " where appid <> 0";
		getWritableDatabase().execSQL(sql);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE "
				+ TABLE_NAME
				+ "(_id INTEGER DEFAULT '1' NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "appid INTEGER, pkg TEXT, logo BLOB  NOT NULL, live_time LONG NOT NULL)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "drop table " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public long insertLogo(int appid, Drawable logo) {
		if (appid == 0 || logo == null) {
			throw new IllegalArgumentException("pkg or logo must have value!");
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id from " + TABLE_NAME
				+ " where appid = ?", new String[] { Integer.toString(appid) });
		try {
			if (c.moveToNext()) {
				return -1;
			}
			return insertNewLogo(appid, logo);
		} finally {
			c.close();
		}
	}

	public long insertLogo(String pkg, Drawable logo) {
		if (pkg == null || logo == null) {
			throw new IllegalArgumentException("pkg or logo must have value!");
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id from " + TABLE_NAME
				+ " where pkg = ?", new String[] { pkg });
		try {
			if (c.moveToNext()) {
				return -1;
			}
			return insertNewLogo(pkg, logo);
		} finally {
			c.close();
		}
	}

	private long insertNewLogo(int appid, Drawable logo) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("appid", appid);
		contentValues.put("logo", ImageUtil.drawableToByte(logo));
		contentValues.put("live_time", System.currentTimeMillis());
		return db.insert(TABLE_NAME, null, contentValues);
	}

	private long insertNewLogo(String pkg, Drawable logo) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("pkg", pkg);
		contentValues.put("logo", ImageUtil.drawableToByte(logo));
		contentValues.put("live_time", System.currentTimeMillis());
		return db.insert(TABLE_NAME, null, contentValues);
	}

	public Drawable getLogoByPkg(String pkg) {
		Drawable d = null;
		if (pkg == null) {
			return d;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id, logo from " + TABLE_NAME
				+ " where pkg = ?", new String[] { pkg });
		try {
			if (c.moveToNext()) {
				int _id = c.getInt(0);
				byte[] logo = c.getBlob(1);
				updateLiveTime(_id);
				d = ImageUtil.byteToDrawable(logo);
			}
		} finally {
			c.close();
		}
		return d;
	}

	public Drawable getLogoByAppid(int appid) {
		Drawable d = null;
		if (appid == 0) {
			return d;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id, logo from " + TABLE_NAME
				+ " where appid = ?", new String[] { Integer.toString(appid) });
		try {
			if (c.moveToNext()) {
				int _id = c.getInt(0);
				byte[] logo = c.getBlob(1);
				updateLiveTime(_id);
				d = ImageUtil.byteToDrawable(logo);
			}
		} finally {
			c.close();
		}
		return d;
	}

	private void updateLiveTime(int _id) {
		/*
		 * SQLiteDatabase db = getWritableDatabase(); ContentValues
		 * contentValues = new ContentValues(); contentValues.put("live_time",
		 * System.currentTimeMillis()); db.update(TABLE_NAME, contentValues,
		 * "_id=?", new String[] { Integer.toString(_id) });
		 */
	}
}
