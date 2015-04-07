package com.tblin.market.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.AppMarketConfig;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.R;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.breakdown.LoadSequenceManager;
import com.tblin.market.info.AppDataProvider;
import com.tblin.market.info.DataListener;
import com.tblin.market.info.DataProvider;
import com.tblin.market.info.OnAppError;
import com.tblin.market.info.PageInfo;

public class AppListActivity extends MarketActivity {

	private ListView lv;
	private MarketViewAdapter ad;
	private List<AppItem> appItems;
	public NetWorkReceiver receiver;
	private DataProvider provider;
	private SessionManager session;
	private ImageView erriv;
	private PkgReceiver pkgReveiver;
	private List<AppItem> searchappitem;
	private int isapp = 0;
	private DataListener lsnrsearch;
	private String keyword;
	private AppStatusReceiver statusReceiver;
	private OnAppError err;
	private ProgressDialog progressDialog;
	private TextView networkerr;
	private ListStatus suggestStatus;
	private ListStatus searchStatus;
	private DataListener lsnr;
	private static final String TAG = AppListActivity.class.toString();

	/** Called when the activity is first created. */
	public void setupView() {
		erriv = (ImageView) findViewById(R.id.err_iv);
		networkerr = (TextView) findViewById(R.id.network_err_name);
		session = SessionManager.getInstance(this);
		suggestStatus = new ListStatus();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setupView();
		appItems = new ArrayList<AppItem>();
		lv = (ListView) findViewById(R.id.market_lv);
		waitNetWork();
		searchappitem = new ArrayList<AppItem>();
		provider = AppDataProvider.getInstance(this);
		provider.open();
		isapp = 0;
		initList();
		ad = new MarketViewAdapter(this, appItems);
		lv.setAdapter(ad);
	}

	private void initList() {
		// pi = new PageInfo();
		lsnr = new DataListener() {

			@Override
			public void onData(Map<String, Object> data) {
				erriv.setBackgroundColor(Color.WHITE);
				networkerr.setText("");
				if (data.containsKey("error")) {
					err = (OnAppError) data.get("error");
					switch (err.getCode()) {
					case 100:
						progressDialog.dismiss();

						erriv.setBackgroundResource(R.drawable.network_err);
						networkerr.setText("  网络异常，请检查...");
						Toast.makeText(AppListActivity.this, "网络异常，请检查.", 3000)
								.show();
						break;
					case 108:
						suggestStatus.setStatus(ListStatus.STATUS_CLOSE);
						Toast.makeText(AppListActivity.this, "暂无更多软件显示", 3000)
								.show();
						break;
					default:
						progressDialog.dismiss();

						erriv.setBackgroundResource(R.drawable.network_err);
						Toast.makeText(AppListActivity.this, "服务器维护中,请稍后再试.",
								3000).show();

					}
					return;
				}

				@SuppressWarnings("unchecked")
				List<AppItem> apps = (List<AppItem>) data.get("app_item");
				PageInfo pg = (PageInfo) data.get("page_info");
				if (pg.newThan(suggestStatus.getPage())) {
					suggestStatus.setPage(pg);
				}
				suggestStatus.setStatus(ListStatus.STATUS_READY_FOR_MORE);
				Map<String, Boolean> has = new HashMap<String, Boolean>();
				for (AppItem item : appItems) {
					has.put(item.getPacakgeName(), true);
				}
				for (AppItem ai : apps) {
					if (has.get(ai.getPacakgeName()) == null) {
						appItems.add(ai);
						if (ad != null) {
							ad.notifyDataSetChanged();
							progressDialog.dismiss();
						}
					}
				}
			}
		};
		provider.onAppItem(AppMarketConfig.APP_GROUP_ID,
				suggestStatus.getPage(), lsnr);
		lv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {
					if (view.getLastVisiblePosition() == (view.getCount() - 1))
						if (isapp == 0
								&& suggestStatus.getStatus() == ListStatus.STATUS_READY_FOR_MORE) {
							provider.onAppItem(AppMarketConfig.APP_GROUP_ID,
									suggestStatus.getPage(), lsnr);
							suggestStatus
									.setStatus(ListStatus.STATUS_WAITING_FOR_RESULT);
						} else if (isapp == 1
								&& searchStatus.getStatus() == ListStatus.STATUS_READY_FOR_MORE) {
							provider.onSearch(keyword,
									AppMarketConfig.APP_GROUP_ID,
									searchStatus.getPage(), lsnrsearch);
							searchStatus
									.setStatus(ListStatus.STATUS_WAITING_FOR_RESULT);
						}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (lastNetType == null) {
					Toast.makeText(AppListActivity.this, "网络已经断开，请检查.", 2000)
							.show();
				} else {
					ad.notifyDataSetChanged();
					Intent it = new Intent(AppListActivity.this,
							InfosActivity.class);
					AppItem app = null;
					if (isapp == 0) {
						app = appItems.get(arg2);
						session.put("appitem", appItems.get(arg2));
					} else {
						app = searchappitem.get(arg2);
						session.put("appitem", searchappitem.get(arg2));
					}
					it.putExtra("appID", app.getId());
					session.put("appname", app.getName());
					session.put("appversion", app.getVersionName());
					session.put("appsize", app.getSize());
					startActivity(it);
				}
			}
		});
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		pkgReveiver = new PkgReceiver();
		registerReceiver(pkgReveiver, filter);
		statusReceiver = new AppStatusReceiver();
		IntentFilter statusFilter = new IntentFilter();
		statusFilter.addAction(AppMarketConfig.APP_STS_CHANGE_BROADCAST);
		registerReceiver(statusReceiver, statusFilter);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		searchappitem.clear();
		networkerr.setText("");
		erriv.setBackgroundColor(Color.WHITE);
		lsnrsearch = new DataListener() {
			@Override
			public void onData(Map<String, Object> data) {
				if (data.containsKey("error")) {
					err = (OnAppError) data.get("error");
					switch (err.getCode()) {
					case 100:
						Toast.makeText(AppListActivity.this, "网络异常,请检查。", 3000)
								.show();
						break;
					case 108:
						searchStatus.setStatus(ListStatus.STATUS_CLOSE);
						if (searchappitem.isEmpty()) {

							erriv.setBackgroundResource(R.drawable.network_err);
							networkerr.setText("抱歉，未找到相关软件!");

						} else {

						}
						break;
					default:
						Toast.makeText(AppListActivity.this, "服务器维护中,请稍后再试",
								2000).show();
					}
					return;
				}

				@SuppressWarnings("unchecked")
				List<AppItem> apps = (List<AppItem>) data.get("app_item");
				PageInfo pg = (PageInfo) data.get("page_info");
				if (pg.newThan(searchStatus.getPage())) {
					searchStatus.setPage(pg);
				}
				searchStatus.setStatus(ListStatus.STATUS_READY_FOR_MORE);
				Map<String, Boolean> has = new HashMap<String, Boolean>();
				for (AppItem item : searchappitem) {
					has.put(item.getPacakgeName(), true);
				}
				for (AppItem ai : apps) {
					if (has.get(ai.getPacakgeName()) == null) {
						searchappitem.add(ai);
						if (ad != null) {
							ad.notifyDataSetChanged();
						}
					}
				}
			}
		};
		keyword = getIntent().getStringExtra("keyword");

		if (keyword == null) {
			isapp = 0;
			ad = new MarketViewAdapter(this, appItems);
		} else {
			searchStatus = new ListStatus();
			provider.onSearch(keyword, AppMarketConfig.APP_GROUP_ID,
					searchStatus.getPage(), lsnrsearch);
			isapp = 1;
			ad = new MarketViewAdapter(this, searchappitem);
		}
		lv.setAdapter(ad);
		super.onNewIntent(intent);
	}

	private class PkgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String pkg = intent.getDataString().substring(8);
				MyLog.i(TAG, "接收到安装广播:" + pkg);
				for (AppItem ai : appItems) {
					if (ai.getPacakgeName().equals(pkg)) {
						ai.setStatus(AppItem.STATUS_INSTALLED);
						if (ad != null) {
							ad.notifyDataSetChanged();
						}
						return;
					}
				}
			}
			// 接收广播：设备上删除了一个应用程序包。
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				String pkg = intent.getDataString().substring(8);
				MyLog.i(TAG, "接收到卸载广播:" + pkg);
				for (AppItem ai : appItems) {
					if (ai.getPacakgeName().equals(pkg)) {
						ai.setStatus(AppItem.STATUS_INITIAL);
						if (ad != null) {
							ad.notifyDataSetChanged();
						}
						return;
					}
				}
			}
		}

	}

	public static String lastNetType = null;

	class NetWorkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeInfo = manager.getActiveNetworkInfo();
			LoadSequenceManager lsm = (LoadSequenceManager) SessionManager
					.getInstance(context).get("load_sequence");
			if (activeInfo == null) {

				MyLog.i(TAG, "检测到网络变化:网络断开");
				lsm.tempPauseAll();
				lastNetType = null;
			} else {
				if (appItems.size() == 0) {
					provider.onAppItem(AppMarketConfig.APP_GROUP_ID,
							suggestStatus.getPage(), lsnr);
				}
				if (lastNetType != null
						&& !lastNetType.equals(activeInfo.getTypeName())) {
					MyLog.i(TAG,
							"网络模式切换:" + lastNetType + "->"
									+ activeInfo.getTypeName());
					// 网络模式切换，网络中途并没有断开
					lsm.tempPauseAll();
				} else if (lastNetType == null) {
					// 网络从断开到连接上
					MyLog.i(TAG, "检测到网络变化:网络连接上");
					lsm.onNetWorkagain();
					ad.notifyDataSetChanged();
				}
				lastNetType = activeInfo.getTypeName();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		receiver = new NetWorkReceiver();
		IntentFilter fi = new IntentFilter();
		fi.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(receiver, fi);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onDestroy() {
		MyLog.i(TAG, "AppListActivity on destroy");
		provider.close();
		if (pkgReveiver != null) {
			unregisterReceiver(pkgReveiver);
		}
		if (statusReceiver != null) {
			unregisterReceiver(statusReceiver);
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "管理");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent it = new Intent();
		it.setClass(this, NewLoadManageActivity.class);
		startActivity(it);
		return false;
	}

	private class AppStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					AppMarketConfig.APP_STS_CHANGE_BROADCAST)) {
				String pkg = intent.getStringExtra("pkg");
				int status = intent.getIntExtra("status", -1);
				MyLog.i(TAG,
						"AppListActivity accept status change broadcast: AppItem.StatusPause?"
								+ (status == AppItem.STATUS_PAUSED));
				if (pkg != null) {
					for (AppItem ai : appItems) {
						if (ai.getPacakgeName().equals(pkg)) {
							ai.setStatus(status);
							ai.setIsOutUrlWork(true);
							if (status == AppItem.STATUS_INITIAL
									|| status == AppItem.STATUS_INSTALLED) {
								ai.setComplete(0);
							}
							if (ad != null) {
								ad.notifyDataSetChanged();
							}
							return;
						}
					}
				}
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	public void waitNetWork() {
		progressDialog = new ProgressDialog(getParent());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(true);
		progressDialog.setMessage("正在连接网络，请稍候");
		progressDialog.show();
		new Thread() {
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}.start();
	}

}
