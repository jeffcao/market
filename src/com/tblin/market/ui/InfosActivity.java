package com.tblin.market.ui;

import java.math.BigDecimal;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tblin.embedmarket.AppInfo;
import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.AppMarketConfig;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.R;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.breakdown.ApkFileHelper;
import com.tblin.market.breakdown.DownloadConfig;
import com.tblin.market.breakdown.DownloadListener;
import com.tblin.market.breakdown.DownloadManager;
import com.tblin.market.breakdown.GlobalDownLsnr;
import com.tblin.market.breakdown.LoadSequenceManager;
import com.tblin.market.info.AppDataProvider;
import com.tblin.market.info.DataListener;
import com.tblin.market.info.DataProvider;
import com.tblin.market.info.OnAppError;

public class InfosActivity extends Activity {
	private ImageView logo;
	private TextView type, size, version, desc, bigname, zifei;
	private Gallery ga;
	private DataProvider provider;
	private AppInfo appinfo;
	private InfosGaAdapter imad;
	private SessionManager session;
	private Button download;
	private int num;
	private static final String TAG = InfosActivity.class.toString();
	private AppItem app;
	private DownloadListener downLsnr;
	private Handler hdlr;
	private PkgReceiver pkgReveiver;
	private AppStatusReceiver statusReceiver;
	private ToggleButton tog;
	private boolean isClickDownloading = false;
	private boolean isnull = false;

	public void setupView() {
		logo = (ImageView) findViewById(R.id.infos_logo);
		bigname = (TextView) findViewById(R.id.bigname);
		type = (TextView) findViewById(R.id.infos_categoty);
		size = (TextView) findViewById(R.id.infos_size);
		version = (TextView) findViewById(R.id.infos_version);
		desc = (TextView) findViewById(R.id.infos_del_2);
		zifei = (TextView) findViewById(R.id.infos_share);
		desc.setMaxLines(5);
		ga = (Gallery) findViewById(R.id.infos_gall);
		session = SessionManager.getInstance(this);
		download = (Button) findViewById(R.id.infos_download);
		app = (AppItem) session.get("appitem");
		tog = (ToggleButton) findViewById(R.id.toggleButton1);
		tog.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				desc.setMaxLines(isChecked ? 60 : 5);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infos);
		setupView();
		provider = AppDataProvider.getInstance(this);
		appinfo = new AppInfo();
		InfosGaAdapter tempad = new InfosGaAdapter(this, appinfo.getImages());
		ga.setAdapter(tempad);

		final int infoid = this.getIntent().getIntExtra("appID", 0);

		DataListener lsnr = new DataListener() {

			@Override
			public void onData(Map<String, Object> data) {
				if (data.containsKey("error")) {
					OnAppError err = (OnAppError) data.get("error");
					MyLog.e(TAG, err.toString());
					return;
				}
				appinfo = (AppInfo) data.get("app_info");
				num = appinfo.getTotalImage();
				desc.setText("   " + appinfo.getIntroduce());
				zifei.setText("资费：" + appinfo.getZifei().toString());
				type.setText("类型：" + appinfo.getType().toString());
				DataListener lsnr2 = new DataListener() {

					@Override
					public void onData(Map<String, Object> data) {

						if (data.containsKey("error")) {
							return;
						}

						Drawable d = (Drawable) data.get("image");

						appinfo.addImage(d);

						imad.notifyDataSetChanged();
						isnull = true;

					}
				};
				imad = new InfosGaAdapter(InfosActivity.this,
						appinfo.getImages());

				ga.setAdapter(imad);

				for (int i = 0; i < num; i++) {

					provider.onAppImage(infoid, i, lsnr2);

				}
				session.put("imgs", appinfo.getImages());
			}
		};

		provider.onAppInfo(infoid, lsnr);

		DataListener lsnr3 = new DataListener() {

			@Override
			public void onData(Map<String, Object> data) {
				Drawable draw = (Drawable) data.get("logo");
				logo.setImageDrawable(AngleUitl.toRoundCorner(draw, 20));
			}
		};

		provider.onAppLogo(infoid, lsnr3);

		bigname.setText("   " + session.get("appname"));
		String k = session.get("appsize").toString();
		float f = Float.parseFloat(k);
		if (f < 1024 && f > 0) {
			size.setText("大小：" + f + "b");
		} else if (f > 1024 && f < 10240) {
			size.setText("大小：" + f / 1024 + "KB");
		} else {
			BigDecimal b = new BigDecimal(f / 1024 / 1024);
			size.setText("大小：" + b.setScale(1, BigDecimal.ROUND_FLOOR) + "MB");
		}
		version.setText("版本：" + session.get("appversion").toString());
		ga.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (isnull) {
					Intent itv = new Intent(InfosActivity.this,
							ViewBigImgActivity.class);
					itv.putExtra("viewNum", arg2);
					startActivity(itv);
				} else {

				}
			}
		});
		setButton(app, download);
		downLsnr = new DownloadListener() {

			@Override
			public void onWaiting(String url) {
				dataChange(url, AppItem.STATUS_WAITING);
			}

			@Override
			public void onStart(int fileSize, int compeleteSize, String url) {
				dataChange(url, AppItem.STATUS_LOADING);
			}

			@Override
			public void onReset(String url) {
			}

			@Override
			public void onPause(String url) {
				dataChange(url, AppItem.STATUS_PAUSED);
			}

			@Override
			public void onLoading(String url, int length) {

			}

			@Override
			public void onFinish(String url) {
				dataChange(url, AppItem.STATUS_LOADED);
			}

			@Override
			public void onCancel(String url) {
				dataChange(url, AppItem.STATUS_INITIAL);
			}

			@Override
			public void onNetworkError(String url) {
				dataChange(url, AppItem.STATUS_PAUSED);
			}
		};
		GlobalDownLsnr gdl = (GlobalDownLsnr) SessionManager.getInstance(this)
				.get("global_down");
		gdl.regist(downLsnr);
		hdlr = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					String text = msg.getData().getString("text");
					Toast.makeText(InfosActivity.this, text, Toast.LENGTH_LONG)
							.show();
				}
			}
		};
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

	private void dataChange(String url, int status) {
		if (app != null && app.isUrlEquals(url)) {
			app.setStatus(status);
			notifyDataChanged();
		}
	}

	private void notifyDataChanged() {
		hdlr.post(new Runnable() {

			@Override
			public void run() {
				setButton(app, download);
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (pkgReveiver != null) {
			unregisterReceiver(pkgReveiver);
		}
		if (statusReceiver != null) {
			unregisterReceiver(statusReceiver);
		}
		super.onDestroy();
		System.gc();
	}

	public void goList(View view) {

		finish();
	}

	private void setButton(final AppItem item, Button btn) {
		String btnText = null;
		switch (item.getStatus()) {
		case AppItem.STATUS_INITIAL:
			btnText = "下载";
			break;
		case AppItem.STATUS_INSTALLED:
			btnText = "已安装";
			break;
		case AppItem.STATUS_LOADED:
			btnText = "安装";
			break;
		case AppItem.STATUS_LOADING:
			btnText = "下载中";
			break;
		case AppItem.STATUS_PAUSED:
			btnText = "继续";
			break;
		case AppItem.STATUS_WAITING:
			btnText = "等待";
			break;
		}
		btn.setText(btnText);
		final LoadSequenceManager loadManager = (LoadSequenceManager) SessionManager
				.getInstance(InfosActivity.this).get("load_sequence");
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (item.getStatus()) {
				case AppItem.STATUS_INITIAL:
					startDown(loadManager, item);
					break;
				case AppItem.STATUS_INSTALLED:
					// to uninstall
					ApkFileHelper.startAppByPackageName(item.getPacakgeName(),
							InfosActivity.this);
					break;
				case AppItem.STATUS_LOADED:
					// to install
					ApkFileHelper.install(DownloadConfig.SECOND_LEVEL_PATH
							+ "/" + item.getName() + ".apk", InfosActivity.this);
					break;
				case AppItem.STATUS_LOADING:
					// to pause
					isClickDownloading = true;
					finish();
					break;
				case AppItem.STATUS_PAUSED:
					// to continue
					item.setStatus(AppItem.STATUS_WAITING);
					if (loadManager.isInQueue(item.getId())) {
						loadManager.continueDown(item.getId());
					} else {
						startDown(loadManager, item);
					}
					break;
				case AppItem.STATUS_WAITING:
					// do nothing now TODO
					break;
				}
			}
		});
	}

	private void startDown(final LoadSequenceManager loadManager,
			final AppItem ai) {
		final GlobalDownLsnr lsnr = (GlobalDownLsnr) SessionManager
				.getInstance(InfosActivity.this).get("global_down");
		if (ai.hasUrl()) {
			loadManager.down(ai, lsnr, hdlr);
		} else {
			DataProvider provider = AppDataProvider
					.getInstance(InfosActivity.this);
			provider.onAppUrl(ai.getId(), new DataListener() {

				@Override
				public void onData(Map<String, Object> data) {
					if (data.containsKey("error")) {
						OnAppError err = (OnAppError) data.get("error");
						Toast.makeText(InfosActivity.this,
								DownloadManager.NO_NET_NOTIFY_MESSAGE,
								Toast.LENGTH_LONG).show();
						MyLog.e(TAG, err.toString());
					} else {
						int id = (Integer) data.get("id");
						String urlNo = (String) data.get("url_normal");
						String urlSe = (String) data.get("url_ownserver");
						if (id == ai.getId()) {
							ai.setUrl1(urlNo);
							ai.setUrl2(urlSe);
						}
						MyLog.i(TAG, "url getted and start to down:" + urlNo);
						loadManager.down(ai, lsnr, hdlr);
					}
				}
			});
		}
	}

	private class AppStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					AppMarketConfig.APP_STS_CHANGE_BROADCAST)) {
				String pkg = intent.getStringExtra("pkg");
				int status = intent.getIntExtra("status", -1);
				if (pkg != null
						&& (status == AppItem.STATUS_INITIAL || status == AppItem.STATUS_INSTALLED)) {
					if (app.getPacakgeName().equals(pkg)) {
						app.setStatus(status);
						notifyDataChanged();
					}
				}
			}
		}

	}

	private class PkgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String pkg = intent.getDataString().substring(8);
				MyLog.i(TAG, "接收到安装广播:" + pkg);
				if (app.getPacakgeName().equals(pkg)) {
					app.setStatus(AppItem.STATUS_INSTALLED);
					notifyDataChanged();
					return;
				}
			}
			// 接收广播：设备上删除了一个应用程序包。
			if (intent.getAction().equals(
					"android.intent.action.PACKAGE_REMOVED")) {
				String pkg = intent.getDataString().substring(8);
				MyLog.i(TAG, "接收到卸载广播:" + pkg);
				if (app.getPacakgeName().equals(pkg)) {
					app.setStatus(AppItem.STATUS_INITIAL);
					notifyDataChanged();
					return;
				}
			}
		}

	}

	@Override
	public void finish() {
		Intent it = new Intent(this, EmbedHome.class);
		it.putExtra(NotificationPoster.NOTIFICATION_TAG, isClickDownloading);
		startActivity(it);
		super.finish();
	}

}
