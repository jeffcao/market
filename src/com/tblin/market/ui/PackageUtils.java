package com.tblin.market.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class PackageUtils {
	public static final int TYPE_ALL = 1;
	public static final int TYPE_SYSTEM = 2;
	public static final int TYPE_THIRD = 3;
	public static final int TYPE_SDCARD = 4;
	private PackageManager pm;

	public PackageUtils(Context context) {
		this.pm = context.getPackageManager();
	}

	/**
	 * 查询获取所有的launcher
	 */
	public ArrayList<SetupApp> getLauchers() {
		ArrayList<SetupApp> appInfos = null;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> rInfos = pm.queryIntentActivities(intent,
				PackageManager.GET_INTENT_FILTERS);
		if (rInfos != null) {
			appInfos = new ArrayList<SetupApp>();
			for (ResolveInfo res : rInfos) {
				SetupApp info = new SetupApp();
				info.setIcon(res.loadIcon(pm));
				info.setLable(res.loadLabel(pm).toString());
				info.setPkgName(res.activityInfo.packageName);
				ComponentName cm = new ComponentName(
						res.activityInfo.packageName, res.activityInfo.name);
				Intent it = new Intent();
				it.setComponent(cm);
				info.setIntent(it);
				appInfos.add(info);
			}
		}
		return appInfos;
	}

	private ArrayList<SetupApp> getAppInfos(
			List<ApplicationInfo> applicationInfos, int type) {
		ArrayList<SetupApp> appInfos = null;
		if (applicationInfos != null)
			appInfos = new ArrayList<SetupApp>();

		switch (type) {
		case TYPE_ALL:
			for (ApplicationInfo info : applicationInfos) {
				SetupApp ai = new SetupApp();
				ai.setIcon(info.loadIcon(pm));
				ai.setLable(info.loadLabel(pm).toString());
				ai.setPkgName(info.packageName);

				appInfos.add(ai);
			}
			break;

		case TYPE_SYSTEM:
			for (ApplicationInfo info : applicationInfos) {
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
					SetupApp ai = new SetupApp();
					ai.setIcon(info.loadIcon(pm));
					ai.setLable(info.loadLabel(pm).toString());
					ai.setPkgName(info.packageName);

					appInfos.add(ai);
				}
			}
			break;
		case TYPE_THIRD:
			for (ApplicationInfo info : applicationInfos) {
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					SetupApp ai = new SetupApp();
					ai.setIcon(info.loadIcon(pm));
					ai.setLable(info.loadLabel(pm).toString());
					ai.setPkgName(info.packageName);

					appInfos.add(ai);
				}
			}
			break;
		}

		return appInfos;
	}

	/**
	 * 查询获取所有已安装的应用程序
	 */
	public ArrayList<SetupApp> getAllAppInfos() {
		List<ApplicationInfo> applicationInfo = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		return getAppInfos(applicationInfo, TYPE_ALL);
	}

	/**
	 * 查询所有 系统程序
	 */
	public ArrayList<SetupApp> getSystemAppInfos() {
		List<ApplicationInfo> applicationInfo = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		return getAppInfos(applicationInfo, TYPE_SYSTEM);
	}

	/**
	 * 查询所有第三方程序
	 */
	public ArrayList<SetupApp> getThirdAppInfos() {
		List<ApplicationInfo> applicationInfo = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		return getAppInfos(applicationInfo, TYPE_THIRD);
	}

}
