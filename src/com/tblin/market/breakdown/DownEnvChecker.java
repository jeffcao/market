package com.tblin.market.breakdown;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class DownEnvChecker {

	public static final int ENV_NORMAL = 1;// 正常
	public static final int ENV_NONET = 2;// 没有网络
	public static final int ENV_NOSD = 3;// 没有SD卡

	/**
	 * 检测下载环境是否正常
	 */
	public static int checkDownEnviroment(Context context) {
		if (isNetConnected(context)) {
			if (isSDMounted()) {
				return ENV_NORMAL;
			} else {
				return ENV_NOSD;
			}
		} else {
			return ENV_NONET;
		}
	}

	private static boolean isNetConnected(Context context) {
		boolean result = false;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			NetworkInfo.State state = info.getState();
			if (state == NetworkInfo.State.CONNECTED) {
				result = true;
			}
		}
		return result;
	}

	private static boolean isSDMounted() {
		String status = Environment.getExternalStorageState();
		return status.equals(Environment.MEDIA_MOUNTED);
	}

}
