package com.tblin.embedmarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;

import com.tblin.market.breakdown.AppDBHelper;
import com.tblin.market.breakdown.Dao;
import com.tblin.market.breakdown.DownloadManager;
import com.tblin.market.breakdown.GlobalDownLsnr;
import com.tblin.market.breakdown.ListRefreshStruct;
import com.tblin.market.breakdown.LoadInfo;
import com.tblin.market.breakdown.LoadSequenceManager;
import com.tblin.market.breakdown.MapLoadSequenceManager;
import com.tblin.market.pkg.PkgInfoGetter;
import com.tblin.market.pkg.PkgInfoGetter.PkgListener;

/**
 * SessionManager保存有Session对象，对于数据的存储操作会直接转发给 Session,
 * 而SessionManager则要负责一些初始化的工作 未安装应用：uninstalled_apps 下载队列管理器：load_sequence
 * 应用列表:app_list 全局下载监听器:global_down 已安装应用：installed_apps
 */

public class SessionManager {

	private static SessionManager INSTANCE;
	private Session session;
	private Context mContext;
	private static final String TAG = SessionManager.class.toString();

	private SessionManager(Context context) {
		session = new Session();
		mContext = context;
		deleteExceedUninstalledApp();
		initUninstalled();
		initLoadSequence();
		initGlobalLsnr();
		initInstalled();
		MyLog.i(TAG, "new session manager");
	}

	public void clearSession() {
		MyLog.i(TAG, "clear session manager");
		INSTANCE = null;
	}

	/**
	 * 每次进入软件，删除数据库中已经超时的未安装应用
	 */
	private void deleteExceedUninstalledApp() {
		List<String> urls = AppDBHelper.getInstance(mContext).deleteAllExceed();
		for (String url : urls) {
			MyLog.i(TAG, "delete url:" + url);
			Dao.getInstance(mContext).delete(url);
		}
	}

	/**
	 * 第一次打开软件，要把所有已安装的软件写入到数据库 由于这个操作是在线程里面操作，即使一打开软件就退出也能保证 可以将所有已安装软件写入数据库
	 */
	private void initInstalled() {
		final PreferenceManager pm = new PreferenceManager(mContext);
		final ListRefreshStruct las = new ListRefreshStruct(mContext);
		final List<AppItem> installedApp = new ArrayList<AppItem>();
		las.setApps(installedApp);
		put("installed_apps", las);
		if (pm.isFirstOpenApp()) {
			PkgInfoGetter.getAllInstalledPkg(mContext, new PkgListener() {

				@Override
				public void onPkgGetted(List<AppItem> items) {
					if (items.size() > 0) {
						for (AppItem ai : items) {
							installedApp.add(ai);
							AppDBHelper.getInstance(mContext).insertInstalled(
									ai);
							las.notifyData();
							Intent it = new Intent();
							it.setAction(AppMarketConfig.APP_STS_CHANGE_BROADCAST);
							it.putExtra("pkg", ai.getPacakgeName());
							it.putExtra("status", AppItem.STATUS_INSTALLED);
							mContext.sendBroadcast(it);
						}
					} else {
						MyLog.i(TAG, "installed app is:0");
					}
					pm.setIsFirstOpenApp(false);
				}
			});
		} else {
			MyLog.i(TAG, "not first open");
			las.setApps(AppDBHelper.getInstance(mContext).getAllInstalledApp());
			MyLog.i(TAG, "installed app size:" + las.getApps().size());
		}
	}

	private void initGlobalLsnr() {
		GlobalDownLsnr lsnr = new GlobalDownLsnr(mContext);
		put("global_down", lsnr);
	}

	private void initLoadSequence() {
		LoadSequenceManager manager = MapLoadSequenceManager
				.getInstance(mContext);
		put("load_sequence", manager);
	}

	private void initUninstalled() {
		ListRefreshStruct las = new ListRefreshStruct(mContext);
		// AppItem的信息来源于两部分，一般信息存储于app数据库中，大小和完成度信息存储于
		// down.db中
		List<AppItem> uninstalledApp = AppDBHelper.getInstance(mContext)
				.getAllUninstalledApp();
		for (AppItem ai : uninstalledApp) {
			LoadInfo li = DownloadManager.getDownloadInfo(mContext, ai);
			if (ai.getStatus() != AppItem.STATUS_LOADED) {
				ai.setStatus(AppItem.STATUS_PAUSED);
			}
			if (li != null || ai.getStatus() != AppItem.STATUS_LOADED) {
				if (li != null) {
					ai.setComplete(li.getComplete());
					ai.setSize(li.fileSize);
				} else {
					ai.setComplete(0);
				}
			} else {
				ai.setComplete(ai.getSize());
			}
		}
		las.setApps(uninstalledApp);
		put("uninstalled_apps", las);
	}

	public static SessionManager getInstance(Context context) {
		return INSTANCE == null ? INSTANCE = new SessionManager(context)
				: INSTANCE;
	}

	public void put(String key, Object value) {
		if (value == null || key == null) {
			return;
		}
		session.put(key, value);
	}

	public Object get(String key) {
		if (key == null) {
			return null;
		}
		return session.get(key);
	}

	/**
	 * session类仅仅用来保存数据，是属于SessionManager的私有类 不对外公开
	 * 
	 */
	private static class Session {
		private Map<String, Object> _data;

		public Session() {
			_data = new HashMap<String, Object>();
		}

		public void put(String key, Object value) {
			if (value == null || key == null) {
				return;
			}
			_data.put(key, value);
		}

		public Object get(String key) {
			if (key == null) {
				return null;
			}
			return _data.get(key);
		}
	}

}
