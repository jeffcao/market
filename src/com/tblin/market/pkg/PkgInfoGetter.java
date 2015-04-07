package com.tblin.market.pkg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.LogoDbHelper;
import com.tblin.embedmarket.MyLog;

public class PkgInfoGetter {

	private static final String TAG = PkgInfoGetter.class.toString();

	/**
	 * onPkgGetted方法将在线程里面调用，若是要刷新界面，要用handler
	 */
	public interface PkgListener {
		void onPkgGetted(List<AppItem> items);
	}

	/**
	 * 获取包名对应的软件的如下信息 包名，应用名，版本号，版本名称，logo
	 */
	public static AppItem getAppItemByPkg(Context context, String pkg) {
		if (pkg == null) {
			throw new IllegalArgumentException("pkg name can not be null");
		}
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(pkg, 0);
			AppItem item = new AppItem();
			String sourceDir = packageInfo.applicationInfo.publicSourceDir;
			int size = (int) new File(sourceDir).length();
			item.setSize(size);
			item.setName(packageInfo.applicationInfo.loadLabel(
					context.getPackageManager()).toString());
			item.setPacakgeName(packageInfo.packageName);
			item.setVersionName(packageInfo.versionName);
			item.setVersionCode(packageInfo.versionCode);
			Drawable logo = packageInfo.applicationInfo.loadIcon(context
					.getPackageManager());
			LogoDbHelper ldh = LogoDbHelper.getInstance(context);
			ldh.insertLogo(item.getPacakgeName(), logo);
			item.setStatus(AppItem.STATUS_INSTALLED);
			return item;
		} catch (NameNotFoundException e) {
			MyLog.e(TAG, "package:" + pkg + "do not exist");
			return null;
		}
	}

	/**
	 * 获取已安装的apk，并插入到数据库表
	 */
	public static void getAllInstalledPkg(final Context context) {

	}

	/**
	 * 这个方法将另起线程调用，不会占用主线程的资源 获取手机已安装的软件 获取到的已安装软件具有如下信息 包名，应用名，版本号，版本名称，logo, 大小
	 */
	public static void getAllInstalledPkg(final Context context,
			final PkgListener lsnr) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				List<AppItem> appList = new ArrayList<AppItem>();
				List<PackageInfo> packages = context.getPackageManager()
						.getInstalledPackages(0);
				for (int i = 0; i < packages.size(); i++) {
					PackageInfo packageInfo = packages.get(i);
					AppItem item = new AppItem();
					String sourceDir = packageInfo.applicationInfo.publicSourceDir;
					int size = (int) new File(sourceDir).length();
					item.setSize(size);
					item.setName(packageInfo.applicationInfo.loadLabel(
							context.getPackageManager()).toString());
					item.setPacakgeName(packageInfo.packageName);
					item.setVersionName(packageInfo.versionName);
					item.setVersionCode(packageInfo.versionCode);
					Drawable logo = packageInfo.applicationInfo.loadIcon(context
							.getPackageManager());
					LogoDbHelper ldh = LogoDbHelper.getInstance(context);;
					ldh.insertLogo(item.getPacakgeName(), logo);
					item.setStatus(AppItem.STATUS_INSTALLED);
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						appList.add(item);// 如果非系统应用，则添加至appList
					}
				}
				lsnr.onPkgGetted(appList);
			}
		};
		new Thread(r).start();
	}

}
