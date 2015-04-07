package com.tblin.market.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tblin.embedmarket.AppMarketConfig;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.R;

public class NotificationPoster {

	public static final String NOTIFICATION_TAG = "notification_tag";
	public static final String TAG = NotificationPoster.class.toString();

	public static void postToNotification(Context mContext, String message,
			Class<?> cls, String title, String tag) {
		if (!AppMarketConfig.IS_ACTIVITY_ALIVE) {
			MyLog.w(TAG, "activity is not alive, so don't show notification");
			return;
		}
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService("notification");
		Notification notification = new Notification();
		notification.icon = R.drawable.notification_logo;
		notification.tickerText = message;
		notification.defaults = Notification.DEFAULT_SOUND;
		Intent intent = new Intent();
		intent.putExtra(NOTIFICATION_TAG, true);
		intent.setClass(mContext, cls);
		PendingIntent m_PendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, 0);
		notification.setLatestEventInfo(mContext, title, message,
				m_PendingIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(tag, 0, notification);
	}

}
