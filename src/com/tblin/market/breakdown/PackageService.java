package com.tblin.market.breakdown;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.MobileInfoGetter;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.PackageReceiver;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.info.Networker;
import com.tblin.market.pkg.PkgInfoGetter;

public class PackageService extends Service {

	private static final String TAG = PackageService.class.toString();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		MyLog.i(TAG, "onStart");
		super.onStart(intent, startId);
		MyLog.i(TAG, "onStart, after super.onStart ");
		if (intent != null) {
			Bundle data = intent.getExtras();
			MyLog.i(TAG, "onStart, after intent.getExtras");
			String pkg = data.getString("pkg");
			int type = data.getInt("type");
			refresh(pkg, type);
		}
	
	}

	/**
	 * 安装广播：1.插入item到数据库；2.插入item到已安装列表；3.假如未安装列表有下载完的相同项，删除
	 * 卸载广播：1.从数据库删除；2.加入已安装项有，删除
	 */
	private void refresh(String pkg, int type) {
		ListRefreshStruct uninsLrf = (ListRefreshStruct) SessionManager
				.getInstance(getApplicationContext()).get("uninstalled_apps");
		AppItem uninsItm = uninsLrf.getAppByPkg(pkg);
		ListRefreshStruct insLrf = (ListRefreshStruct) SessionManager
				.getInstance(getApplicationContext()).get("installed_apps");
		AppItem insItm = insLrf.getAppByPkg(pkg);
		if (type == PackageReceiver.TYPE_INSTALL) {
			MyLog.i(TAG, "接收到安装广播:" + pkg + ",进行处理");
			AppItem item = PkgInfoGetter.getAppItemByPkg(
					getApplicationContext(), pkg);
			AppDBHelper.getInstance(getApplicationContext()).insertInstalled(
					item);
			insLrf.add(0, item);
			if (insItm != null) {
				MyLog.i(TAG, "移除已安装列表之前存在的项目");
				insLrf.remove(insItm);
			}
			if (uninsItm != null
					&& uninsItm.getStatus() == AppItem.STATUS_LOADED) {
				MyLog.i(TAG, "移除未安装列表存在的已下载项目");
				uninsLrf.remove(uninsItm);
				String opType = MobileInfoGetter.TYPE_INSTALL_DOWN;
				// notify server app operate
				try {
					Networker.getInstance(getApplicationContext()).downNotify(
							uninsItm, opType);
				} catch (IOException e) {
					// do nothing
				}
			} else {
				MyLog.i(TAG, "未安装列表不存在相同的已下载项目，为空？" + (uninsItm == null));
			}
		} else {
			AppDBHelper.getInstance(getApplicationContext()).deleteByPkg(pkg);
			if (insItm != null) {
				insLrf.remove(insItm);
			}
		}
	}

}
