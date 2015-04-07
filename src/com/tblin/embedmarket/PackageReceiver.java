package com.tblin.embedmarket;

import com.tblin.market.breakdown.PackageService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReceiver extends BroadcastReceiver {

	public static final int TYPE_INSTALL = 1;
	public static final int TYPE_UNINSTALL = 2;
	private static final String TAG = PackageReceiver.class.toString();

	@Override
	public void onReceive(Context context, Intent intent) {
		// 接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			String pkg = intent.getDataString().substring(8);
			MyLog.i(TAG, "接收到安装广播:" + pkg);
			sendMsg(context, TYPE_INSTALL, pkg);
		}
		// 接收广播：设备上删除了一个应用程序包。
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
			String pkg = intent.getDataString().substring(8);
			MyLog.i(TAG, "接收到卸载广播:" + pkg);
			sendMsg(context, TYPE_UNINSTALL, pkg);
		}
	}

	private void sendMsg(Context context, int type, String pkg) {
		Intent intent = new Intent();
		intent.setClass(context, PackageService.class);
		intent.putExtra("pkg", pkg);
		intent.putExtra("type", type);
		context.startService(intent);
	}
}
