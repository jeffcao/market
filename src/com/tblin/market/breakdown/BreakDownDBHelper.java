package com.tblin.market.breakdown;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 建立一个数据库帮助类
 */
public class BreakDownDBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "download.db";

	public BreakDownDBHelper(Context context) {
		super(context, DB_NAME, null, 1);
	}

	/**
	 * 在download.db数据库下创建一个download_info表存储下载信息
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table download_info(_id integer PRIMARY KEY AUTOINCREMENT, thread_id integer, "
				+ "start_pos integer, end_pos integer, compelete_size integer,url char, file_path char)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "drop table " + DB_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

}
