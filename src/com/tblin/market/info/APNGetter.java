package com.tblin.market.info;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class APNGetter {

	private static final Uri PREFERRED_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");
	public static final String WAP = "wap";
	public static final String NET = "net";
	public static final String UNKNOW = "unknow";

	public static String getApnType(Context context) {
		Cursor c = context.getContentResolver().query(PREFERRED_APN_URI, null,
				null, null, null);
		if (c == null) {
			return UNKNOW;
		}
		try {
			String user = null;
			if (c.moveToFirst())
				user = c.getString(c.getColumnIndex("apn"));
			if (user == null) {
				return UNKNOW;
			}
			if (user.contains(WAP) || user.contains(WAP.toUpperCase())) {
				return WAP;
			} else if (user.contains(NET) || user.contains(NET.toUpperCase())) {
				return NET;
			}
			return UNKNOW;
		} finally {
			c.close();
		}
	}
}
