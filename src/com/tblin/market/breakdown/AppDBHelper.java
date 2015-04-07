package com.tblin.market.breakdown;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.AppMarketConfig;
import com.tblin.embedmarket.MyLog;

public class AppDBHelper extends SQLiteOpenHelper {

	private static final String TABLE_NAME = "app";
	private static AppDBHelper INSTANCE;

	private static final String TAG = AppDBHelper.class.toString();

	/**
	 * table key is _id--appid--url--name--size--status--package
	 * --version_code--version_name--live_time--out_work
	 * 
	 * @param context
	 */
	private AppDBHelper(Context context) {
		super(context, TABLE_NAME, null, 1);
	}

	public static AppDBHelper getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new AppDBHelper(context);
		}
		return INSTANCE;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE "
				+ TABLE_NAME
				+ "(_id INTEGER DEFAULT '1' NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "appid INTEGER, url TEXT,"
				+ "name TEXT  NOT NULL, size INTEGER, "
				+ "status INTEGER NOT NULL, complete INTEGER, package TEXT "
				+ "NOT NULL, version_code INTEGER NOT NULL, version_name "
				+ "TEXT NOT NULL, live_time LONG NOT NULL, out_work INTEGER NOT NULL)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "drop table " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public synchronized long deleteAllWaiting() {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_NAME, "status=?",
				new String[] { Integer.toString(AppItem.STATUS_WAITING) });
	}

	public synchronized List<String> deleteAllExceed() {
		SQLiteDatabase db = getReadableDatabase();
		long exceedTime = System.currentTimeMillis()
				- AppMarketConfig.DOWNLOAD_SAVE_TIME;
		Cursor c = db.rawQuery(
				"select _id, url from " + TABLE_NAME
						+ " where live_time < ? and status <> ?",
				new String[] { Long.toString(exceedTime),
						Integer.toString(AppItem.STATUS_INSTALLED) });
		List<String> urls = new ArrayList<String>();
		try {
			while (c.moveToNext()) {
				int _id = c.getInt(0);
				urls.add(c.getString(1));
				MyLog.i(TAG, "delete exceed:" + _id);
				delete(_id);
			}
		} finally {
			c.close();
		}
		return urls;
	}

	public synchronized boolean hasUrl(String url) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id from " + TABLE_NAME
				+ " where url = ?", new String[] { url });
		boolean flag = false;
		try {
			c.moveToFirst();
			if (c.getCount() > 0) {
				flag = true;
			}
		} finally {
			c.close();
		}
		return flag;
	}

	/**
	 * 插入已安装项，先查看是否有已下载完的未安装项 有就先删除，插入已安装项必须是标准项
	 */
	public synchronized long insertInstalled(AppItem item) {
		MyLog.i(TAG, "insert installed:" + item.getName());
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery(
				"select _id from " + TABLE_NAME
						+ " where package =? and (status=? or status=?)",
				new String[] { item.getPacakgeName(),
						Integer.toString(AppItem.STATUS_LOADED), Integer.toString(AppItem.STATUS_INSTALLED) });
		try {
			while (c.moveToNext()) {
				int _id = c.getInt(0);
				MyLog.i(TAG,
						"delete:" + _id + "when insert installed:"
								+ item.getName());
				delete(_id);
			}
		} finally {
			c.close();
		}
		return insert(item);
	}

	public synchronized long insert(AppItem item) {
		if (item == null || item.getName() == null) {
			return -1;
		}
		if (item.getStatus() == AppItem.STATUS_INSTALLED) {
			return insertInstalledItem(item);
		} else {
			return insertWaitingItem(item);
		}
	}

	public synchronized long insertInstalledItem(AppItem item) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("name", item.getName());
		contentValues.put("status", AppItem.STATUS_INSTALLED);
		contentValues.put("package", item.getPacakgeName());
		contentValues.put("size", item.getSize());
		contentValues.put("version_code", item.getVersionCode());
		contentValues.put("version_name", item.getVersionName());
		contentValues.put("live_time", System.currentTimeMillis());
		contentValues.put("out_work", 1);
		return db.insert(TABLE_NAME, null, contentValues);
	}

	public synchronized long insertWaitingItem(AppItem item) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("appid", item.getId());
		contentValues.put("url", item.getWorkUrl());
		MyLog.i(TAG, "INSERT url is:" + item.getWorkUrl());
		contentValues.put("name", item.getName());
		contentValues.put("size", 0);
		contentValues.put("status", AppItem.STATUS_WAITING);
		contentValues.put("complete", 0);
		contentValues.put("package", item.getPacakgeName());
		contentValues.put("version_code", item.getVersionCode());
		contentValues.put("version_name", item.getVersionName());
		contentValues.put("live_time", System.currentTimeMillis());
		contentValues.put("out_work", item.isOutUrlWork() ? 1 : 0);
		return db.insert(TABLE_NAME, null, contentValues);
	}

	public synchronized long delteByUrl(String url) {
		if (url == null || url.length() == 0) {
			return -1;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id from " + TABLE_NAME
				+ " where url = ?", new String[] { url });
		int _id = -1;
		try {
			if (c.moveToFirst()) {
				_id = c.getInt(0);
			}
		} finally {
			c.close();
		}
		return delete(_id);
	}

	public synchronized long deleteById(int id) {
		if (id <= 0) {
			return -1;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id from " + TABLE_NAME
				+ " where appid = ?", new String[] { Integer.toString(id) });
		int _id = -1;
		try {
			if (c.moveToFirst()) {
				_id = c.getInt(0);
			}
		} finally {
			c.close();
		}
		return delete(_id);
	}

	public synchronized int delete(int _id) {
		if (_id <= 0) {
			return -1;
		}
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_NAME, "_id=?",
				new String[] { Integer.toString(_id) });
	}

	public synchronized List<AppItem> getAllApp() {
		return getAllApp(3);
	}

	public synchronized List<AppItem> getAllInstalledApp() {
		return getAllApp(2);
	}

	public synchronized List<AppItem> getAllUninstalledApp() {
		return getAllApp(1);
	}

	/**
	 * type为1代表获取未安装的，为2代表获取已安装的
	 */
	private List<AppItem> getAllApp(int type) {
		List<AppItem> apps = new ArrayList<AppItem>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		if (type == 1) {
			c = db.rawQuery("select package from " + TABLE_NAME
					+ " where status <> ?",
					new String[] { Integer.toString(AppItem.STATUS_INSTALLED) });
		} else if (type == 2) {
			c = db.rawQuery("select package from " + TABLE_NAME
					+ " where status = ?",
					new String[] { Integer.toString(AppItem.STATUS_INSTALLED) });
		} else {
			c = db.rawQuery("select package from " + TABLE_NAME,
					new String[] {});
		}
		MyLog.i(TAG, c.toString());
		try {
			while (c.moveToNext()) {
				MyLog.i(TAG, "move to next");
				AppItem ai = getAppByPkg(c.getString(0));
				MyLog.i(TAG, "getappbypkg:" + c.getString(0) + "|" + ai);
				if (ai != null) {
					if (ai.getStatus() != AppItem.STATUS_INSTALLED) {
						apps.add(0, ai);
					} else {
						apps.add(ai);
					}
				}
			}
		} finally {
			c.close();
		}
		MyLog.i(TAG, "return");
		return apps;
	}

	public synchronized AppItem getAppByPkg(String pkg) {
		if (pkg == null) {
			return null;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db
				.rawQuery(
						"select appid, url, name, size, status, complete, package, version_name, out_work, version_code from "
								+ TABLE_NAME + " where package = ?",
						new String[] { pkg });
		AppItem it = new AppItem();
		try {
			if (c.moveToFirst()) {
				it.setName(c.getString(2));
				it.setStatus(c.getInt(4));
				it.setPacakgeName(c.getString(6));
				it.setSize(c.getInt(3));
				it.setVersionName(c.getString(7));
				it.setVersionCode(c.getInt(8));
				if (it.getStatus() != AppItem.STATUS_INSTALLED) {
					it.setId(c.getInt(0));
					it.setUrl1(c.getString(1));
					it.setComplete(c.getInt(5));
					MyLog.i(TAG,
							"filesize:" + it.getSize() + ",filecom:"
									+ it.getComplete() + ", filename:"
									+ it.getName());
				}
			} else {
				it = null;
			}
		} finally {
			c.close();
		}
		return it;
	}

	public synchronized AppItem getAppById(int id) {
		if (id <= 0) {
			return null;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db
				.rawQuery(
						"select appid, url, name, size, status, complete, package , out_work, version_code from "
								+ TABLE_NAME + " where appid = ?",
						new String[] { Integer.toString(id) });
		AppItem it = new AppItem();
		try {
			if (c.moveToFirst()) {
				it.setName(c.getString(2));
				it.setStatus(c.getInt(4));
				it.setPacakgeName(c.getString(6));
				it.setVersionCode(c.getInt(7));
				if (it.getStatus() != AppItem.STATUS_INSTALLED) {
					it.setId(c.getInt(0));
					it.setUrl1(c.getString(1));
					/*
					 * if (c.getInt(8) == 1) { it.setUrl1(c.getString(1)); }
					 * else { it.setOutUrlNotWork(); it.setUrl2(c.getString(1));
					 * }
					 */
					it.setSize(c.getInt(3));
					it.setComplete(c.getInt(5));
					MyLog.i(TAG,
							"filesize:" + it.getSize() + ",filecom:"
									+ it.getComplete() + ", filename:"
									+ it.getName());
				}
			} else {
				it = null;
			}
		} finally {
			c.close();
		}
		return it;
	}

	public synchronized AppItem getAppByUrl(String url) {
		if (url == null || url.length() == 0) {
			return null;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select appid from " + TABLE_NAME
				+ " where url = ?", new String[] { url });
		int id = -1;
		try {
			if (c.moveToFirst()) {
				id = c.getInt(0);
			}
		} finally {
			c.close();
		}
		return getAppById(id);
	}

	public synchronized int deleteByPkg(String pkg) {
		if (pkg == null) {
			return -1;
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select _id from " + TABLE_NAME
				+ " where package = ?", new String[] { pkg });
		try {
			if (c.moveToNext()) {
				return delete(Integer.toString(c.getInt(0)));
			}
		} finally {
			c.close();
		}
		return -1;
	}

	public int delete(String _id) {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_NAME, "_id=?", new String[] { _id });
	}

	public synchronized int updateCompelteAdd(String url, int addLength) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		Cursor c = db.rawQuery("select complete from " + TABLE_NAME
				+ " where url = ?", new String[] { url });
		int size = addLength;
		try {
			if (c.moveToFirst()) {
				size += c.getInt(0);
			}
		} finally {
			c.close();
		}
		MyLog.i(TAG, "size update to:" + size + "url:" + url);
		values.put("complete", size);
		values.put("live_time", System.currentTimeMillis());
		int result = db.update(TABLE_NAME, values, "url=?",
				new String[] { url });
		return result;
	}

	public synchronized int updateStatusByPkg(String pkg, int status) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("status", status);
		values.put("live_time", System.currentTimeMillis());
		int result = db.update(TABLE_NAME, values, "package=?",
				new String[] { pkg });
		return result;
	}

	public synchronized int updateStatus(String url, int status) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("status", status);
		values.put("live_time", System.currentTimeMillis());
		int result = db.update(TABLE_NAME, values, "url=?",
				new String[] { url });
		return result;
	}

	public synchronized int updateSize(String url, int size) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("size", size);
		values.put("live_time", System.currentTimeMillis());
		int result = db.update(TABLE_NAME, values, "url=?",
				new String[] { url });
		return result;
	}

}
