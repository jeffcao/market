package com.tblin.market.breakdown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.AppMarketConfig;
import com.tblin.embedmarket.MobileInfoGetter;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.info.Networker;

public class MapLoadSequenceManager implements LoadSequenceManager {

	private static final int MAX_LOADERS = 2;
	private Map<Integer, DownloadManager> loaderManagers;
	private List<Integer> downingIds;
	private List<Integer> waitingIds;
	private List<Integer> errIds;
	private Context mContext;
	private static MapLoadSequenceManager INSTANCE;
	private static final String TAG = MapLoadSequenceManager.class.toString();

	private MapLoadSequenceManager(Context context) {
		loaderManagers = new HashMap<Integer, DownloadManager>();
		downingIds = new ArrayList<Integer>();
		waitingIds = new ArrayList<Integer>();
		errIds = new ArrayList<Integer>();
		mContext = context;
	}

	public static MapLoadSequenceManager getInstance(Context context) {
		return INSTANCE == null ? INSTANCE = new MapLoadSequenceManager(context)
				: INSTANCE;
	}

	public boolean isInQueue(int id) {
		boolean result = loaderManagers.get(id) != null;
		MyLog.i(TAG, "check is " + id + " in queue:" + (result));
		return result;
	}

	private boolean isInIds(int id) {
		return isDowning(id) || isWaiting(id);
	}

	/**
	 * AppItem需要有int id, String url, String name, byte[] logo, String pkg
	 */
	@Override
	public void down(final AppItem ai, final DownloadListener lsnr,
			final Handler hdlr) {
		// 当要下载的id已存于队列之中时，直接返回
		if (isInIds(ai.getId())) {
			MyLog.i(TAG, "要下载的id已存在于队列之中");
			return;
		}
		Runnable r = new Runnable() {

			@Override
			public void run() {
				DownloadManager dm = loaderManagers.get(ai.getId());
				final int id = ai.getId();
				if (dm == null) {
					final DownloadListener lsnrFinal = lsnr;
					DownloadListener lsnrFit = new DownloadListener() {

						@Override
						public void onStart(int fileSize, int compeleteSize,
								String url) {
							lsnrFinal.onStart(fileSize, compeleteSize, url);
						}

						@Override
						public void onReset(String url) {
							lsnrFinal.onReset(url);
						}

						@Override
						public void onPause(String url) {
							MyLog.i(TAG, "onPause");
							lsnrFinal.onPause(url);
						}

						@Override
						public void onLoading(String url, int length) {
							lsnrFinal.onLoading(url, length);
						}

						@Override
						public void onFinish(String url) {
							lsnrFinal.onFinish(url);
							MyLog.i(TAG, "on finish and cancel current task");
							removeFromQueue(id);
						}

						@Override
						public void onWaiting(String url) {
							lsnrFinal.onWaiting(url);
						}

						@Override
						public void onCancel(String url) {
							lsnrFinal.onCancel(url);
						}

						@Override
						public void onNetworkError(String url) {
							lsnrFinal.onNetworkError(url);
							removeIdFromQueue(id);
							errIds.add(id);
						}
					};
					dm = new DownloadManager(mContext, lsnrFit, ai);
					loaderManagers.put(ai.getId(), dm);
				}
				if (downingIds.size() < MAX_LOADERS) {
					downingIds.add(ai.getId());
					int flag = dm.down();
					if (flag == DownloadManager.LOAD_ENV_OK) {
						MyLog.i(TAG, "can download");
						if (isWaiting(ai.getId())) {
							waitingIds.remove(new Integer(ai.getId()));
						}
					} else {
						MyLog.i(TAG, "can not download");
						Intent it = new Intent(
								AppMarketConfig.APP_STS_CHANGE_BROADCAST);
						it.putExtra("status", AppItem.STATUS_PAUSED);
						it.putExtra("pkg", ai.getPacakgeName());
						mContext.sendBroadcast(it);
						ai.setStatus(AppItem.STATUS_PAUSED);
						ListRefreshStruct uninstalled = (ListRefreshStruct) SessionManager
								.getInstance(mContext).get("uninstalled_apps");
						uninstalled.notifyData();
						removeFromQueue(id);
						Message msg = hdlr.obtainMessage();
						msg.getData().putString("text",
								DownloadManager.getLoadErrorMessage(flag));
						msg.what = 1;
						hdlr.sendMessage(msg);
					}
				} else {
					if (!isWaiting(ai.getId())) {
						waitingIds.add(ai.getId());
					}
				}
			}
		};
		new Thread(r).start();
	}

	private void removeIdFromQueue(int appid) {
		if (isWaiting(appid)) {
			waitingIds.remove(new Integer(appid));
		} else if (isDowning(appid)) {
			downingIds.remove(new Integer(appid));
		}
	}

	private void removeFromQueue(int appid) {
		if (isWaiting(appid)) {
			waitingIds.remove(new Integer(appid));
		} else if (isDowning(appid)) {
			downingIds.remove(new Integer(appid));
		}
		loaderManagers.remove(appid);
		if (waitingIds.size() > 0) {
			MyLog.i(TAG, "pause get waiting task to run");
			int id = waitingIds.remove(0);
			DownloadManager dm = loaderManagers.get(id);
			if (dm != null) {
				dm.down();
				downingIds.add(id);
			}
		}
	}

	@Override
	public void cancel(final int appid) {
		MyLog.i(TAG, "cancel:" + appid);
		DownloadManager dm = loaderManagers.get(appid);
		if (dm != null || isDowning(appid)) {
			removeFromQueue(appid);
			dm.cancel();
		}
		AppItem ai = AppDBHelper.getInstance(mContext).getAppById(appid);
		if (ai != null && ai.hasUrl()) {
			AppDBHelper.getInstance(mContext).deleteByPkg(ai.getPacakgeName());
			Dao.getInstance(mContext).delete(ai.getWorkUrl());
			ListRefreshStruct lrs = (ListRefreshStruct) SessionManager
					.getInstance(mContext).get("uninstalled_apps");
			lrs.remove(appid);
			Intent it = new Intent();
			it.setAction(AppMarketConfig.APP_STS_CHANGE_BROADCAST);
			it.putExtra("status", AppItem.STATUS_INITIAL);
			it.putExtra("pkg", ai.getPacakgeName());
			mContext.sendBroadcast(it);
			String type = null;
			if (ai.getStatus() != AppItem.STATUS_LOADED) {
				type = MobileInfoGetter.TYPE_CANCEL_DOWN;
			} else {
				type = MobileInfoGetter.TYPE_DELETE_DOWN;
			}
			// notify server app operate
			try {
				Networker.getInstance(mContext).downNotify(ai, type);
			} catch (IOException e) {
				// do nothing
			}
		} else {
			MyLog.e(TAG, "something unexcepted happen");
			// this will not happen
		}
	}

	@Override
	public void pause(int appid) {
		if (isDowning(appid)) {
			MyLog.i(TAG, "pause downing task:" + appid);
			DownloadManager dm = loaderManagers.get(appid);
			dm.pause();
			downingIds.remove(new Integer(appid));
		} else if (isWaiting(appid)) {
			MyLog.i(TAG, "pause remove waiting task");
			waitingIds.remove(new Integer(appid));
		}
		if (waitingIds.size() > 0) {
			MyLog.i(TAG, "pause get waiting task to run");
			int id = waitingIds.remove(0);
			DownloadManager dm = loaderManagers.get(id);
			if (dm != null) {
				dm.down();
				downingIds.add(id);
			}
		}
	}

	private boolean isWaiting(int appid) {
		for (Integer i : waitingIds) {
			if (i.equals(appid)) {
				return true;
			}
		}
		return false;
	}

	private boolean isDowning(int appid) {
		for (Integer i : downingIds) {
			if (i.equals(appid)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void continueDown(int appid) {
		if (isDowning(appid)) {
			MyLog.i(TAG, "do not continue down");
			return;
		}
		MyLog.i(TAG, "continue down");
		DownloadManager dm = loaderManagers.get(appid);
		if (dm == null) {
			return;
		}
		if (downingIds.size() < MAX_LOADERS) {
			downingIds.add(appid);
			int result = dm.down();
			if (result == DownloadManager.LOAD_ENV_OK) {
				if (isWaiting(appid)) {
					waitingIds.remove(new Integer(appid));
				}
				MyLog.i(TAG, "continue...");
			} else {
				// TODO can not down, need to notify user
				ListRefreshStruct unins = (ListRefreshStruct) SessionManager
						.getInstance(mContext).get("uninstalled_apps");
				AppItem ai = unins.getApp(appid);
				Intent it = new Intent(AppMarketConfig.APP_STS_CHANGE_BROADCAST);
				it.putExtra("status", AppItem.STATUS_PAUSED);
				it.putExtra("pkg", ai.getPacakgeName());
				mContext.sendBroadcast(it);
				String text = DownloadManager.getLoadErrorMessage(result);
				Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
				ListRefreshStruct uninstalled = (ListRefreshStruct) SessionManager
						.getInstance(mContext).get("uninstalled_apps");
				uninstalled.getApp(appid).setStatus(AppItem.STATUS_PAUSED);
				uninstalled.notifyData();
				removeFromQueue(appid);
			}
		} else {
			if (!isWaiting(appid)) {
				waitingIds.add(appid);
			}
			MyLog.i(TAG, "waiting...");
		}
	}

	@Override
	public void pauseAll() {
		Set<Integer> set = loaderManagers.keySet();
		for (int i : set) {
			DownloadManager dm = loaderManagers.get(i);
			dm.pause();
		}
		loaderManagers.clear();
		downingIds.clear();
		waitingIds.clear();
		AppDBHelper.getInstance(mContext).deleteAllWaiting();
	}

	@Override
	public boolean hasTaskGoing() {
		return downingIds.size() != 0;
	}

	public void onNetWorkagain() {
		for (Integer id : errIds) {
			continueDown(id);
		}
		errIds.clear();
	}

	@Override
	public void tempPauseAll() {
		Set<Integer> set = loaderManagers.keySet();
		for (int i : set) {
			DownloadManager dm = loaderManagers.get(i);
			dm.pause();
			errIds.add(i);
		}
		downingIds.clear();
		waitingIds.clear();
		AppDBHelper.getInstance(mContext).deleteAllWaiting();
	}

}
