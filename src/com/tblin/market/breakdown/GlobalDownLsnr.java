package com.tblin.market.breakdown;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.MobileInfoGetter;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.info.Networker;
import com.tblin.market.ui.EmbedHome;
import com.tblin.market.ui.NotificationPoster;

public class GlobalDownLsnr implements DownloadListener {

	private Context mContext;
	private Queue<DownloadListener> observers;
	private final String TAG = GlobalDownLsnr.class.toString();
	private boolean isGlobalRegisted;

	public GlobalDownLsnr(Context context) {
		mContext = context;
		observers = new ConcurrentLinkedQueue<DownloadListener>();
	}

	/**
	 * 加入一个全局的监视器，用于刷新数据库等一些必须要刷新的东西
	 */
	public void registGlobalListener() {
		if (isGlobalRegisted) {
			return;
		}
		GlobalListener gl = new GlobalListener();
		regist(gl);
		MyLog.i(TAG, "registGlobalListener");
		isGlobalRegisted = true;
	}

	private class GlobalListener implements DownloadListener {

		private AppDBHelper appdb;
		private ListRefreshStruct las;

		// private final String TAG = GlobalListener.class.toString();

		public GlobalListener() {
			appdb = AppDBHelper.getInstance(mContext);
			las = (ListRefreshStruct) SessionManager.getInstance(mContext).get(
					"uninstalled_apps");
		}

		@Override
		public void onLoading(String url, int length) {
			AppItem ai = las.getApp(url);
			ai.setComplete(ai.getComplete() + length);
			int id = ai.getId();
			las.notifyDirtyData(id);
		}

		@Override
		public void onFinish(String url) {
			AppItem ai = las.getApp(url);
			NotificationPoster.postToNotification(mContext, ai.getName()
					+ "下载完成，请安装", EmbedHome.class, "EmbedMarket",
					EmbedHome.NOTIFICATION_TAG);
			appdb.updateStatus(url, AppItem.STATUS_LOADED);
			ai.setStatus(AppItem.STATUS_LOADED);
			las.notifyData();
			// notify server end down
			String type = ai.isOutUrlWork() ? MobileInfoGetter.TYPE_OUT_LINK_END
					: MobileInfoGetter.TYPE_INNER_LINK_END;
			try {
				Networker.getInstance(mContext).downNotify(ai, type);
			} catch (IOException e) {
				// do nothing
			}
			// 下载完成之后要安装
			ApkFileHelper.install(
					DownloadConfig.SECOND_LEVEL_PATH + "/" + ai.getName()
							+ ".apk", mContext);
		}

		@Override
		public void onStart(int fileSize, int compeleteSize, String url) {
			MyLog.i(TAG, "START DOWNLOAD:" + compeleteSize);
			AppItem ai = las.getApp(url);
			NotificationPoster.postToNotification(mContext, ai.getName()
					+ "开始下载", EmbedHome.class, "EmbedMarket",
					EmbedHome.NOTIFICATION_TAG);
			appdb.updateStatus(url, AppItem.STATUS_LOADING);
			appdb.updateSize(url, fileSize);
			ai.setSize(fileSize);
			ai.setComplete(compeleteSize);
			ai.setStatus(AppItem.STATUS_LOADING);
			if (compeleteSize == 0) {
				// notify server start down
				String type = ai.isOutUrlWork() ? MobileInfoGetter.TYPE_OUT_LINK_START
						: MobileInfoGetter.TYPE_INNER_LINK_START;
				try {
					Networker.getInstance(mContext).downNotify(ai, type);
				} catch (IOException e) {
					// do nothing
				}
			}
			las.notifyData();
		}

		@Override
		public void onPause(String url) {
			appdb.updateStatus(url, AppItem.STATUS_PAUSED);
			AppItem ai = las.getApp(url);
			NotificationPoster.postToNotification(mContext, ai.getName()
					+ "已暂停下载，请继续", EmbedHome.class, "EmbedMarket",
					EmbedHome.NOTIFICATION_TAG);
			ai.setStatus(AppItem.STATUS_PAUSED);
			las.notifyData();
		}

		@Override
		public void onReset(String url) {
			appdb.updateStatus(url, AppItem.STATUS_LOADED);
			AppItem ai = las.getApp(url);
			ai.setStatus(AppItem.STATUS_LOADED);
			las.notifyData();
		}

		@Override
		public void onWaiting(String url) {
			MyLog.i(TAG, "on waiting:" + url);
			appdb.updateStatus(url, AppItem.STATUS_WAITING);
			AppItem ai = las.getApp(url);
			final String u = url;
			if (ai == null) {
				AppItem cu = appdb.getAppByUrl(u);
				AppItem aii = las.getApp(cu.getId());
				if (aii != null) {
					las.remove(aii);
					MyLog.i(TAG,
							"remove:" + aii.getName() + ";" + aii.getUrl1());
				}
				MyLog.i(TAG, "las.add(cu):" + cu.getUrl1() + "/" + cu.getUrl2());
				las.add(cu);

			} else {
				ai.setStatus(AppItem.STATUS_WAITING);
			}
			las.notifyData();
		}

		@Override
		public void onCancel(String url) {
			MyLog.i(TAG, "onCancel");
			appdb.delteByUrl(url);
			final AppItem ai = las.getApp(url);
			if (ai != null) {
				las.remove(ai);
			}
			LoadSequenceManager lsm = (LoadSequenceManager) SessionManager
					.getInstance(mContext).get("load_sequence");
			if (!lsm.hasTaskGoing()) {
				EmbedHome.clearNotification(mContext);
			}
		}

		@Override
		public void onNetworkError(String url) {
			appdb.updateStatus(url, AppItem.STATUS_PAUSED);
			AppItem ai = las.getApp(url);
			NotificationPoster.postToNotification(mContext, ai.getName()
					+ "下载未完成", EmbedHome.class, "EmbedMarket",
					EmbedHome.NOTIFICATION_TAG);
			ai.setStatus(AppItem.STATUS_PAUSED);
			las.notifyData();
		}

	}

	public void regist(DownloadListener lsnr) {
		if (lsnr != null) {
			observers.add(lsnr);
		}
	}

	public void unregist(DownloadListener lsnr) {
		if (lsnr != null) {
			observers.remove(lsnr);
		}
	}

	@Override
	public void onLoading(String url, int length) {
		for (DownloadListener lsnr : observers) {
			lsnr.onLoading(url, length);
		}
	}

	@Override
	public void onFinish(String url) {
		for (DownloadListener lsnr : observers) {
			lsnr.onFinish(url);
		}
	}

	@Override
	public void onStart(int fileSize, int compeleteSize, String url) {
		for (DownloadListener lsnr : observers) {
			lsnr.onStart(fileSize, compeleteSize, url);
		}
	}

	@Override
	public void onPause(String url) {
		for (DownloadListener lsnr : observers) {
			lsnr.onPause(url);
		}
	}

	@Override
	public void onReset(String url) {
		for (DownloadListener lsnr : observers) {
			lsnr.onReset(url);
		}
	}

	@Override
	public void onWaiting(String url) {
		for (DownloadListener lsnr : observers) {
			lsnr.onWaiting(url);
		}
	}

	@Override
	public void onCancel(String url) {
		for (DownloadListener lsnr : observers) {
			lsnr.onCancel(url);
		}
	}

	@Override
	public void onNetworkError(String url) {
		for (DownloadListener lsnr : observers) {
			lsnr.onNetworkError(url);
		}
	}

}
