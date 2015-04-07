package com.tblin.embedmarket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.content.Context;

import com.tblin.market.breakdown.LoadSequenceManager;
import com.tblin.market.ui.EmbedHome;

public class MarketActivityManager {

	// private static final String TAG = MarketActivityManager.class.toString();

	public interface MarketCloseListener {
		void onClose();
	}

	private Queue<Activity> activitys;
	private MarketCloseListener closeListener;
	private static MarketActivityManager INSTANCE;

	private MarketActivityManager() {
		activitys = new ConcurrentLinkedQueue<Activity>();
	}

	public static MarketActivityManager getInstance() {
		return INSTANCE != null ? INSTANCE
				: (INSTANCE = new MarketActivityManager());
	}

	public boolean registActivity(Activity activity) {
		if (activitys.contains(activity)) {
			return false;
		}
		return activitys.add(activity);
	}

	public boolean unregistActivity(Activity activity) {
		return activitys.remove(activity);
	}

	public static boolean hasTaskGoing(Context context) {
		SessionManager manager = SessionManager.getInstance(context);
		LoadSequenceManager lsm = (LoadSequenceManager) manager
				.get("load_sequence");
		return lsm != null && lsm.hasTaskGoing();
	}

	public void postCloseAllActivity() {
		if (activitys.size() == 0 && closeListener != null) {
			closeListener.onClose();
			return;
		}
		for (Activity activity : activitys) {
			if (activity instanceof EmbedHome) {
				((EmbedHome) activity).finishChild();
				break;
			}
		}
	}

	public void notifyActivityClosed(Activity activity) {
		unregistActivity(activity);
		if (activitys.size() == 1) {
			activitys.peek().finish();
		} else if (activitys.size() == 0 && closeListener != null) {
			closeListener.onClose();
		}
	}

	public void setCloseListener(MarketCloseListener closeListener) {
		this.closeListener = closeListener;
	}

}
