package com.tblin.market.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.DrawableGetter;
import com.tblin.embedmarket.DrawableGetter.OnDrawableListener;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.R;
import com.tblin.embedmarket.SessionManager;
import com.tblin.embedmarket.SizeTextGetter;
import com.tblin.market.breakdown.ApkFileHelper;
import com.tblin.market.breakdown.AppDBHelper;
import com.tblin.market.breakdown.DownloadConfig;
import com.tblin.market.breakdown.DownloadListener;
import com.tblin.market.breakdown.DownloadManager;
import com.tblin.market.breakdown.GlobalDownLsnr;
import com.tblin.market.breakdown.ListRefreshStruct;
import com.tblin.market.breakdown.LoadSequenceManager;
import com.tblin.market.breakdown.Refresher;
import com.tblin.market.info.AppDataProvider;
import com.tblin.market.info.DataListener;
import com.tblin.market.info.DataProvider;
import com.tblin.market.info.OnAppError;

public class MarketViewAdapter extends BaseAdapter implements Refresher {

	private static final String TAG = MarketViewAdapter.class.toString();

	private List<AppItem> appItems;
	private LayoutInflater inflater;
	private Context mContext;
	private DataProvider provider;
	private Map<Integer, Button> bts;
	private Map<Integer, AppItem> items;
	private Handler hdlr;
	private List<ViewStatus> cangos;
	private DrawableGetter picGetter;

	public MarketViewAdapter(Context context, List<AppItem> appItems) {
		this.appItems = appItems;
		this.inflater = LayoutInflater.from(context);
		provider = AppDataProvider.getInstance(context);
		mContext = context;
		bts = new HashMap<Integer, Button>();
		items = new HashMap<Integer, AppItem>();
		ListRefreshStruct lrs = new ListRefreshStruct(mContext);
		cangos = new ArrayList<ViewStatus>();
		picGetter = new DrawableGetter(context);
		refreshCangos();
		lrs.setApps(appItems);
		lrs.registRefresher(this);
		SessionManager.getInstance(context).put("app_list", lrs);
		hdlr = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					String text = msg.getData().getString("text");
					Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
				}
			}
		};
		initGlobalDown();
	}

	private void refreshCangos() {
		MyLog.i(TAG, "refreshCangos");
		for (int i = cangos.size(); i < appItems.size(); i++) {
			MyLog.i(TAG, "refreshCangos:" + i);
			cangos.add(new ViewStatus());
		}
	}

	private void initGlobalDown() {
		GlobalDownLsnr lsnr = (GlobalDownLsnr) SessionManager.getInstance(
				mContext).get("global_down");
		lsnr.registGlobalListener();
		DownloadListener dl = new DownloadListener() {

			@Override
			public void onWaiting(String url) {
				MyLog.i(TAG, "onWaiting");
				refreshStatus(url, AppItem.STATUS_WAITING);
			}

			@Override
			public void onStart(int fileSize, int compeleteSize, String url) {
				MyLog.i(TAG, "onStart");
				AppItem ai = getbyurl(url);
				if (ai == null) {
					return;
				}
				ai.setStatus(AppItem.STATUS_LOADING);
				ai.setSize(fileSize);
				refreshStatus(url, AppItem.STATUS_LOADING);
			}

			@Override
			public void onReset(String url) {
				refreshStatus(url, AppItem.STATUS_LOADED);
				MyLog.i(TAG, "onReset");
			}

			@Override
			public void onPause(String url) {
				refreshStatus(url, AppItem.STATUS_PAUSED);
				MyLog.i(TAG, "onPause");
			}

			@Override
			public void onLoading(String url, int length) {
				MyLog.i(TAG, "onLoading");
				AppItem ai = getbyurl(url);
				if (ai == null) {
					return;
				}
				ai.setComplete(ai.getComplete() + length);
				final int id = ai.getId();
				hdlr.post(new Runnable() {

					@Override
					public void run() {
						notifyDirytyData(id);
					}
				});
			}

			@Override
			public void onFinish(String url) {
				refreshStatus(url, AppItem.STATUS_LOADED);
				MyLog.i(TAG, "onFinish");
			}

			/*
			 * 如果只是状态有所改变，就整体刷新
			 */
			private void refreshStatus(String url, int status) {
				AppItem ai = getbyurl(url);
				MyLog.i(TAG, "url is:" + url);
				if (ai == null) {
					AppItem item = (AppItem) ((ListRefreshStruct) SessionManager
							.getInstance(mContext).get("uninstalled_apps"))
							.getApp(url);
					ai = getbyid(item.getId());
					if (ai == null) {
						return;
					}
					ai.setUrl1(item.getUrl1());
					ai.setUrl2(item.getUrl2());
				}

				ai.setStatus(status);
				hdlr.post(new Runnable() {

					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onCancel(String url) {
				AppItem ai = getbyurl(url);
				if (ai != null) {
					ai.setStatus(AppItem.STATUS_INITIAL);
					ai.setComplete(0);
					hdlr.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();
						}
					});
				}
			}

			@Override
			public void onNetworkError(String url) {
				refreshStatus(url, AppItem.STATUS_PAUSED);
				MyLog.i(TAG, "on network eror");
			}
		};
		lsnr.regist(dl);
	}

	private AppItem getbyid(int id) {
		for (AppItem ai : appItems) {
			if (ai.getId() == id) {
				return ai;
			}
		}
		return null;
	}

	private AppItem getbyurl(String url) {
		for (AppItem ai : appItems) {
			if (ai.isUrlEquals(url)) {
				return ai;
			}
		}
		return null;
	}

	@Override
	public int getCount() {
		return appItems.size();
	}

	@Override
	public Object getItem(int position) {
		return appItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return appItems.get(position).getId();
	}

	private class ViewHolder {
		TextView name, company, version, size;
		ImageView imageview,btim;
		Button button;
		RelativeLayout btdown;
	}
	private int gcFlag = 0;
	private static final int GC_VALUE = 50;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		gcFlag++;
		if (gcFlag > GC_VALUE) {
			System.gc();
			gcFlag = 0;
		}
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.appitem, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.company = (TextView) convertView.findViewById(R.id.cat);
			holder.version = (TextView) convertView.findViewById(R.id.version);
			holder.size = (TextView) convertView.findViewById(R.id.size);
			holder.imageview = (ImageView) convertView
					.findViewById(R.id.imageView1);
			holder.button = (Button) convertView
					.findViewById(R.id.appitem_download);
			holder.btim=(ImageView) convertView.findViewById(R.id.appitem_down_im);
			holder.btdown=(RelativeLayout) convertView.findViewById(R.id.bt_down);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final AppItem app = appItems.get(position);
		holder.name.setText(app.getName());
		holder.company.setText(app.getCompany());
		holder.size.setText(SizeTextGetter.getSize(app.getSize()));
		holder.version.setText("版本：" + app.getVersionName());
		Drawable logo = new DrawableGetter(mContext).getLogo(app.getId());
		final ViewStatus vs = cangos.get(position);
		MyLog.i(TAG, "logo is:" + logo);
		holder.imageview.setImageDrawable(logo);
		if (logo == DrawableGetter.DEFAULT_DRAWABLE) {
			final ViewHolder vh = holder;
			picGetter.onLogo(app.getId(), new OnDrawableListener() {
	
				@Override
				public void onDrawable(Drawable d, String pkg) {
				}
	
				@Override
				public void onDrawable(Drawable d, int appid) {
					if (appid == app.getId()) {
						vh.imageview.setImageDrawable(d);
					}
				}
			});
		}
		/*if (logo == null) {
			holder.imageview.setImageResource(R.drawable.defalut_logo);
			DataListener lsnr = new DataListener() {

				@Override
				public void onData(Map<String, Object> data) {
					if (data.containsKey("error")) {
						OnAppError err = (OnAppError) data.get("error");
						MyLog.e(TAG, err.toString());
						return;
					}
					Drawable draw = (Drawable) data.get("logo");
					int k = (Integer) data.get("id");
					for (AppItem ai : appItems) {
						if (ai.getId() == k) {
							ai.setLogo(draw);
							notifyDataSetChanged();
						}
					}
				}
			};
			if (!vs.isLogoUpdated) {
				provider.onAppLogo(app.getId(), lsnr);
				vs.isLogoUpdated = true;
			}
		} else {
			holder.imageview
					.setImageDrawable(AngleUitl.toRoundCorner(logo, 20));
			vs.isLogoUpdated = true;
		}*/

		if (!vs.isStatusUpdated) {
			MyLog.i(TAG, "status updating:" + app.getPacakgeName());
			AppItem ai = AppDBHelper.getInstance(mContext).getAppByPkg(
					app.getPacakgeName());
			if (ai != null) {
				app.setStatus(ai.getStatus());
				app.setSize(ai.getSize());
				app.setComplete(ai.getComplete());
				notifyDataSetChanged();
			}
			vs.isStatusUpdated = true;
		}

		String btnText = null;
		Drawable btnDrawable = null;
		switch (app.getStatus()) {
		case AppItem.STATUS_INITIAL:
			btnText = "下载";
			btnDrawable = mContext.getResources().getDrawable(
					R.drawable.download);
			break;
		case AppItem.STATUS_INSTALLED:
			btnText = "已安装";
			btnDrawable = mContext.getResources().getDrawable(R.drawable.setup);
			break;
		case AppItem.STATUS_LOADED:
			btnText = "安装";
			btnDrawable = mContext.getResources().getDrawable(R.drawable.setup);
			break;
		case AppItem.STATUS_LOADING:
			/*
			 * int per = (app.getComplete() * 100 / app.getSize()); btnText =
			 * per + "%";
			 */
			btnText = "暂停";
			btnDrawable = mContext.getResources().getDrawable(R.drawable.pause);
			break;
		case AppItem.STATUS_PAUSED:
			btnText = "继续";
			btnDrawable = mContext.getResources().getDrawable(
					R.drawable.contine);
			break;
		case AppItem.STATUS_WAITING:
			btnText = "等待中";
			btnDrawable = mContext.getResources().getDrawable(R.drawable.wait);
			break;
		}

		holder.button.setText(btnText);
//		holder.button.setCompoundDrawablesWithIntrinsicBounds(null,
//				AngleUitl.toRoundCorner(btnDrawable, 5), null, null);
		holder.btim.setBackgroundDrawable(btnDrawable);
		final LoadSequenceManager loadManager = (LoadSequenceManager) SessionManager
				.getInstance(mContext).get("load_sequence");
		final AppItem ai = app;
		holder.btdown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (ai.getStatus()) {
				case AppItem.STATUS_INITIAL:
					startDown(loadManager, ai);
					break;
				case AppItem.STATUS_INSTALLED:
					ApkFileHelper.startAppByPackageName(ai.getPacakgeName(),
							mContext);
					break;
				case AppItem.STATUS_LOADED:
					ApkFileHelper.install(DownloadConfig.SECOND_LEVEL_PATH
							+ "/" + ai.getName() + ".apk", mContext);
					break;
				case AppItem.STATUS_LOADING:
					loadManager.pause(ai.getId());
					break;
				case AppItem.STATUS_PAUSED:
					if (loadManager.isInQueue(ai.getId())) {
						loadManager.continueDown(ai.getId());
					} else {
						startDown(loadManager, ai);
					}
					break;
				case AppItem.STATUS_WAITING:
					break;
				}

			}

			private void startDown(final LoadSequenceManager loadManager,
					final AppItem ai) {
				final GlobalDownLsnr lsnr = (GlobalDownLsnr) SessionManager
						.getInstance(mContext).get("global_down");
				if (ai.hasUrl()) {
					loadManager.down(ai, lsnr, hdlr);
				} else {
					provider.onAppUrl(ai.getId(), new DataListener() {

						@Override
						public void onData(Map<String, Object> data) {
							if (data.containsKey("error")) {
								OnAppError err = (OnAppError) data.get("error");
								MyLog.e(TAG, err.toString());
								Toast.makeText(mContext,
										DownloadManager.NO_NET_NOTIFY_MESSAGE,
										Toast.LENGTH_LONG).show();
							} else {
								int id = (Integer) data.get("id");
								String urlNo = (String) data.get("url_normal");
								String urlSe = (String) data
										.get("url_ownserver");
								if (id == ai.getId()) {
									ai.setUrl1(urlNo);
									ai.setUrl2(urlSe);
								}
								MyLog.i(TAG, "url getted and start to down:"
										+ urlNo);
								loadManager.down(ai, lsnr, hdlr);
							}
						}
					});
				}
			}
		});
		items.put(new Integer(app.getId()), app);
		bts.put(new Integer(app.getId()), holder.button);
		return convertView;
	}

	private class ViewStatus {
		public volatile boolean isStatusUpdated = false;
	}

	@Override
	public void notifyDataSetChanged() {
		refreshCangos();
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		refreshCangos();
		super.notifyDataSetInvalidated();
	}

	@Override
	public void notifyAllData() {
		refreshCangos();
		notifyDataSetChanged();
	}

	@Override
	public void notifyDirytyData(int pos) {
		/*
		 * AppItem item = items.get(new Integer(pos)); Button dirty =
		 * bts.get(new Integer(pos)); if (dirty != null && item != null) {
		 * MyLog.i(TAG, "dirty and item is not null"); int per =
		 * (item.getComplete() * 100 / item.getSize()); dirty.setText(per +
		 * "%"); dirty.refreshDrawableState(); }
		 */
	}

}
