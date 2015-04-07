package com.tblin.market.breakdown;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.Context;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.MobileInfoGetter;
import com.tblin.embedmarket.MyLog;
import com.tblin.market.info.Networker;

/**
 * 注意：一次下载只能对应一个下载管理器
 * 
 */
public class DownloadManager extends BreakDown {

	private Context mContext;
	private int threadCount;
	private Downloader downLoader;
	private AppDBHelper appDb;
	private AppItem app;
	private DownloadListener lsnr;
	private static final int DEFAULT_THREAD_COUNT = 1;
	public static final int LOAD_ENV_OK = 1;
	public static final int LOAD_ENV_NO_NET = 2;
	public static final int LOAD_ENV_NO_SDCARD = 3;
	public static final int LOAD_ENV_LINK_ERROR = 4;
	public static final String NO_NET_NOTIFY_MESSAGE = "当前无网络连接，请检查";
	private static final String TAG = DownloadManager.class.toString();

	public DownloadManager(Context context, DownloadListener lsnr, AppItem app) {
		this(context, DEFAULT_THREAD_COUNT, lsnr, app);
	}

	public DownloadManager(Context context, int threadCount,
			DownloadListener lsnr, AppItem app) {
		this.mContext = context;
		this.threadCount = threadCount;
		appDb = AppDBHelper.getInstance(mContext);
		this.app = app;
		this.app.setStatus(AppItem.STATUS_WAITING);
		this.lsnr = lsnr;
		MyLog.i(TAG, "APP URL IS:" + app.getUrl1());
		if (!appDb.hasUrl(app.getUrl1())) {
			appDb.insert(app);
		}
		lsnr.onWaiting(app.getUrl1());
	}

	private int initLoadEnv() {
		int flag = init();
		if (flag == LOAD_ENV_LINK_ERROR) {
			MyLog.w(TAG, "out url can not download:set out url not work");
			app.setIsOutUrlWork(false);
			flag = init();
		}
		return flag;
	}

	public AppItem getApp() {
		return app;
	}

	public int getAppid() {
		return app.getId();
	}

	public static String getLoadErrorMessage(int type) {
		String result = null;
		switch (type) {
		case LOAD_ENV_NO_NET:
			result = NO_NET_NOTIFY_MESSAGE;
			break;
		case LOAD_ENV_LINK_ERROR:
			//result = NO_NET_NOTIFY_MESSAGE;
			result = "链接错误，无法下载";
			break;
		case LOAD_ENV_NO_SDCARD:
			result = "当前无SD卡，请检查";
			break;
		default:
			result = "";
			break;
		}
		return result;
	}

	public static LoadInfo getDownloadInfo(Context context, AppItem item) {
		if (item == null || !item.hasUrl()) {
			return null;
		}
		LoadInfo li = getDownloadInfo(context, item.getUrl1());
		return li != null ? li : getDownloadInfo(context, item.getUrl2());
	}

	private static LoadInfo getDownloadInfo(Context context, String url) {
		if (url == null) {
			return null;
		}
		Dao dao = Dao.getInstance(context);
		MyLog.i(TAG, "getDownloadInfo:" + url);
		if (dao.isHasInfors(url)) {
			List<DownloadInfo> infos = dao.getInfos(url);
			LoadInfo info = new LoadInfo();
			for (DownloadInfo in : infos) {
				info.fileSize += (in.getEndPos() - in.getStartPos() + 1);
				info.setComplete(info.getComplete() + in.getCompeleteSize());
				MyLog.i(TAG, "SIZE:" + info.fileSize + "/" + info.getComplete());
			}
			return info;
		} else {
			MyLog.i(TAG, "can't find url:" + url);
			return null;
		}
	}

	/**
	 * 记住，若是用服务器url下载，则要先把App的urlnormal和urlserver替换
	 * 开始下载，返回true表示下载器启动，返回false代表下载环境不正确 可能是没有网络，也可能是没有SD卡
	 */
	private int init() {
		int flag = init(app.getWorkUrl(), app.getName() + ".apk", lsnr);
		if (flag == LOAD_ENV_OK) {
			if (!app.isOutUrlWork()) {
				MyLog.i(TAG, "delete url1:" + app.getUrl1());
				appDb.deleteByPkg(app.getPacakgeName());
			}
			if (!appDb.hasUrl(app.getWorkUrl())) {
				MyLog.i(TAG, "insert app item");
				long result = appDb.insert(app);
				MyLog.i(TAG, "insert app result is:" + result);
			} else {
				MyLog.i(TAG, "do not insert app item");
			}
			lsnr.onWaiting(app.getWorkUrl());
		}
		return flag;
	}

	/**
	 * 开始下载，返回true表示下载器启动，返回false代表下载环境不正确 可能是没有网络，也可能是没有SD卡
	 */
	private int init(String url, String fileName, DownloadListener lsnr) {
		int chkResult = DownEnvChecker.checkDownEnviroment(mContext);
		if (chkResult == DownEnvChecker.ENV_NORMAL) {
			if (checkUrl(url)) {
				String filePath = DownloadConfig.SECOND_LEVEL_PATH + "/"
						+ fileName;
				downLoader = new Downloader(url, filePath, threadCount,
						mContext);
				downLoader.setDownloadListener(lsnr);
				// downLoader.download();
				return LOAD_ENV_OK;
			} else {
				// notify server link error
				String type = app.isOutUrlWork() ? MobileInfoGetter.TYPE_OUT_LINK_BAD
						: MobileInfoGetter.TYPE_IN_LINK_BAD;
				try {
					Networker.getInstance(mContext).downNotify(app, type);
				} catch (IOException e) {
					// do nothing
				}
				MyLog.w(TAG, "url can not download");
			}
			return LOAD_ENV_LINK_ERROR;
		} else {
			MyLog.w(TAG, "download enviroment is not ok");
			return chkResult == DownEnvChecker.ENV_NONET ? LOAD_ENV_NO_NET
					: LOAD_ENV_NO_SDCARD;
		}
	}

	/**
	 * 检查连接是否有效，连接无效返回false
	 * 
	 * @param url
	 * @return
	 */
	private boolean checkUrl(String url) {
		boolean result = false;
		URL u = null;
		HttpURLConnection connection = null;
		try {
			u = new URL(url);
			connection = (HttpURLConnection) u.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			int fileSize = connection.getContentLength();
			if (fileSize > 0) {
				result = true;
			}
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return result;
	}

	public int down() {
		int flag = initLoadEnv();
		if (flag == LOAD_ENV_OK) {
			downLoader.download();
		}
		return flag;
	}

	/**
	 * 暂停下载
	 */
	public void pause() {
		downLoader.pause();
	}

	public void cancel() {
		downLoader.cancel();
	}

}
