package com.tblin.market.ui;

import com.tblin.embedmarket.MarketActivityManager;

import android.app.Activity;
import android.os.Bundle;

public class MarketActivity extends Activity {

	private MarketActivityManager activityManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		activityManager = MarketActivityManager.getInstance();
		activityManager.registActivity(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		activityManager.notifyActivityClosed(this);
		super.onDestroy();
	}

}
