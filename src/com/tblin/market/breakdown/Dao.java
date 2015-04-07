package com.tblin.market.breakdown;

import java.util.ArrayList;
import java.util.List;

import com.tblin.embedmarket.MyLog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Dao {

	private BreakDownDBHelper dbHelper;
	private static Dao INSTANCE;
	private static final String TAG = Dao.class.toString();

	private Dao(Context context) {
		dbHelper = new BreakDownDBHelper(context);
	}

	public static Dao getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new Dao(context);
		}
		return INSTANCE;
	}

	/**
	 * 查看数据库中是否有数据
	 */
	public synchronized boolean isHasInfors(String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select count(*)  from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		MyLog.i(TAG, "isHasInfors:" + urlstr + "?" + (count == 0));
		MyLog.i(TAG, "count:" + count);
		return count != 0;
	}

	public synchronized List<String> getAllUrl() {
		List<String> urls = new ArrayList<String>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("select url from " + "download_info",
				new String[] {});
		try {
			while (c.moveToNext()) {
				String r = c.getString(0);
				boolean flag = true;
				for (String u : urls) {
					if (r != null && r.equals(u)) {
						flag = false;
						break;
					}
				}
				if (flag) {
					urls.add(r);
				}
			}
		} finally {
			c.close();
		}
		return urls;
	}

	/**
	 * 保存 下载的具体信息
	 */
	public synchronized void saveInfos(List<DownloadInfo> infos) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		for (DownloadInfo info : infos) {
			String sql = "insert into download_info(thread_id,start_pos, end_pos,compelete_size,url,file_path) values (?,?,?,?,?,?)";
			Object[] bindArgs = { info.getThreadId(), info.getStartPos(),
					info.getEndPos(), info.getCompeleteSize(), info.getUrl(),
					info.getFilePath() };
			database.execSQL(sql, bindArgs);
		}
	}

	/**
	 * 得到下载具体信息
	 */
	public synchronized List<DownloadInfo> getInfos(String urlstr) {
		List<DownloadInfo> list = new ArrayList<DownloadInfo>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select thread_id, start_pos, end_pos,compelete_size,url, file_path from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		while (cursor.moveToNext()) {
			DownloadInfo info = new DownloadInfo(cursor.getInt(0),
					cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
					cursor.getString(4), cursor.getString(5));
			list.add(info);
		}
		cursor.close();
		return list;
	}

	/**
	 * 更新数据库中的下载信息
	 */
	public synchronized void updataInfos(int threadId, int compeleteSize,
			String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "update download_info set compelete_size=? where thread_id=? and url=?";
		Object[] bindArgs = { compeleteSize, threadId, urlstr };
		database.execSQL(sql, bindArgs);
	}

	/**
	 * 关闭数据库
	 */
	public synchronized void closeDb() {
		dbHelper.close();
	}

	/**
	 * 下载完成后删除数据库中的数据
	 */
	public synchronized void delete(String url) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		database.delete("download_info", "url=?", new String[] { url });
		// database.close();
	}
}
