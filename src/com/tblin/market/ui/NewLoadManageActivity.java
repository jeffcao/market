package com.tblin.market.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ExpandableListView;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.R;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.breakdown.ListRefreshStruct;
import com.tblin.market.breakdown.LoadSequenceManager;

public class NewLoadManageActivity extends MarketActivity {

	private ExpandableAdapter adapter;
	private ListRefreshStruct uninsLas;
	private ListRefreshStruct insLas;
	private List<List<AppItem>> manageApps;

	private static final String TAG = NewLoadManageActivity.class.toString();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		init();
	}

	private void init() {
		uninsLas = (ListRefreshStruct) SessionManager.getInstance(this).get(
				"uninstalled_apps");
		insLas = (ListRefreshStruct) SessionManager.getInstance(this).get(
				"installed_apps");
		setContentView(R.layout.new_main_load_manage);

		ExpandableListView list = (ExpandableListView) findViewById(R.id.new_main_load_manage_list);

		manageApps = new ArrayList<List<AppItem>>();
		System.out.println("unins" + uninsLas);
		System.out.println("inslas" + insLas);
		List<AppItem> unins = uninsLas.getApps();
		List<AppItem> ins = insLas.getApps();
		manageApps.add(unins);
		manageApps.add(ins);
		adapter = new ExpandableAdapter(manageApps, this);

		list.setAdapter(adapter);
		list.expandGroup(0);
		list.expandGroup(1);
		list.setGroupIndicator(null);
		uninsLas.registRefresher(adapter);
		insLas.registRefresher(adapter);

	}

	@Override
	protected void onDestroy() {
		LoadSequenceManager lsm = (LoadSequenceManager) SessionManager
				.getInstance(this).get("load_sequence");
		lsm.pauseAll();
		MyLog.i(TAG, "NewLoadManageActivity on destroy");
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

}
