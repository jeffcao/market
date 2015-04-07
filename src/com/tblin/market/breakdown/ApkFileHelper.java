package com.tblin.market.breakdown;

import java.io.File;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class ApkFileHelper {

	public static void uninstall(String pkg, Context context) {
		if (pkg == null) {
			return;
		}
		Uri packageURI = Uri.parse("package:" + pkg);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(uninstallIntent);
	}

	public static boolean startAppByPackageName(String pkg, Context context) {
		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(pkg, 0);
		} catch (NameNotFoundException e) {

		}
		Intent it = new Intent(Intent.ACTION_MAIN, null);
		it.addCategory(Intent.CATEGORY_LAUNCHER);
		it.setPackage(pi.packageName);
		List<ResolveInfo> apps = context.getPackageManager()
				.queryIntentActivities(it, 0);
		if (apps.size() == 0) {
			return false;
		}
		ResolveInfo ri = apps.iterator().next();
		if (ri != null) {
			String packageName1 = ri.activityInfo.packageName;
			String className = ri.activityInfo.name;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName cn = new ComponentName(packageName1, className);
			intent.setComponent(cn);
			context.startActivity(intent);
			return true;
		}
		return false;
	}

	/**
	 * 安装file path指定路径的APK，若APK不存在返回false
	 * 
	 * @param filePath
	 * @param context
	 * @return
	 */
	public static boolean install(String filePath, Context context) {
		if (null == filePath) {
			throw new IllegalArgumentException("file path can't be null");
		}
		File file = new File(filePath);
		if (file.exists()) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
			context.startActivity(intent);
			return true;
		}
		return false;
	}
}
