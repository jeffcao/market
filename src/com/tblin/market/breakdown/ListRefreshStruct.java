package com.tblin.market.breakdown;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.os.Handler;

import com.tblin.embedmarket.AppItem;

/**
 * 这个类用来控制list的刷新，必须要保证不要直接对apps进行add操作， add()操作只能在主线程里面进行
 * 
 */
public class ListRefreshStruct {

	private List<AppItem> apps;
	private Queue<Refresher> adapters;
	private Handler hdlr;

	// private final String TAG = ListRefreshStruct.class.toString();

	public ListRefreshStruct(Context context) {
		hdlr = new Handler(context.getMainLooper());
		adapters = new ConcurrentLinkedQueue<Refresher>();
	}

	public void registRefresher(Refresher ref) {
		if (ref != null) {
			adapters.add(ref);
		}
	}

	public void unregistRefresher(Refresher ref) {
		if (ref != null) {
			adapters.remove(ref);
		}
	}

	public boolean add(int pos, AppItem ai) {
		if (hasItem(ai)) {
			return false;
		}
		apps.add(pos, ai);
		notifyData();
		return true;
	}

	public boolean add(AppItem ai) {
		if (hasItem(ai)) {
			return false;
		}
		apps.add(ai);
		notifyData();
		return true;
	}

	public boolean remove(int appid) {
		return remove(getApp(appid));
	}

	public boolean remove(AppItem item) {
		if (item == null) {
			return false;
		}
		boolean result = apps.remove(item);
		if (result) {
			notifyData();
		}
		return result;
	}

	public AppItem getAppByPkg(String pkg) {
		if (pkg == null) {
			return null;
		}
		for (AppItem item : apps) {
			if (item.getPacakgeName().equals(pkg)) {
				return item;
			}
		}
		return null;
	}

	public AppItem getApp(String url) {
		if (url == null) {
			return null;
		}
		for (AppItem item : apps) {
			if (item.hasUrl() && item.isUrlEquals(url)) {
				return item;
			}
		}
		return null;
	}

	public AppItem getApp(int id) {
		for (AppItem item : apps) {
			if (item.getId() == id) {
				return item;
			}
		}
		return null;
	}

	public boolean hasItem(AppItem ai) {
		for (AppItem item : apps) {
			if (item.getPacakgeName().equals(ai.getPacakgeName())) {
				return true;
			}
		}
		return false;
	}

	public void notifyData() {
		hdlr.post(new Runnable() {

			@Override
			public void run() {
				for (Refresher adapter : adapters) {
					if (adapter != null) {
						adapter.notifyAllData();
					}
				}
			}
		});
	}

	public void notifyDirtyData(final int id) {
		hdlr.post(new Runnable() {

			@Override
			public void run() {
				for (Refresher adapter : adapters) {
					if (adapter != null) {
						adapter.notifyDirytyData(id);
					}
				}
			}
		});
	}

	public List<AppItem> getApps() {
		return apps;
	}

	public void setApps(List<AppItem> apps) {
		this.apps = apps;
	}

}
