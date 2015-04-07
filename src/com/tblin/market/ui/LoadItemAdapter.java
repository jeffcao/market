package com.tblin.market.ui;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.DrawableGetter;
import com.tblin.embedmarket.DrawableGetter.OnDrawableListener;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.R;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.breakdown.ApkFileHelper;
import com.tblin.market.breakdown.DownloadConfig;
import com.tblin.market.breakdown.DownloadManager;
import com.tblin.market.breakdown.GlobalDownLsnr;
import com.tblin.market.breakdown.LoadSequenceManager;
import com.tblin.market.breakdown.Refresher;
import com.tblin.market.info.AppDataProvider;
import com.tblin.market.info.DataListener;
import com.tblin.market.info.DataProvider;
import com.tblin.market.info.OnAppError;

public class LoadItemAdapter extends BaseAdapter implements Refresher {

	private Context mContext;
	private List<AppItem> apps;
	private LayoutInflater inflater;
	private LoadSequenceManager loadManager;
	private Map<Integer, ProgressBar> pbs;
	private Map<Integer, AppItem> items;
	private Handler hdlr;
	private DrawableGetter picGetter;

	private static final String TAG = LoadItemAdapter.class.toString();

	private class ViewHolder {
		public ImageView logo;
		public TextView name;
		public ProgressBar progress;
		public Button btn;
	}

	public LoadItemAdapter(Context context, List<AppItem> apps) {
		mContext = context;
		this.apps = apps;
		inflater = LayoutInflater.from(mContext);
		loadManager = (LoadSequenceManager) SessionManager.getInstance(context)
				.get("load_sequence");
		pbs = new HashMap<Integer, ProgressBar>();
		items = new HashMap<Integer, AppItem>();
		picGetter = new DrawableGetter(context);
		hdlr = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					String text = msg.getData().getString("text");
					Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
				}
			}
		};
	}

	@Override
	public int getCount() {
		return apps.size();
	}

	@Override
	public Object getItem(int position) {
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.download_item, null);
			holder.btn = (Button) convertView
					.findViewById(R.id.download_item_btn);
			holder.logo = (ImageView) convertView
					.findViewById(R.id.download_item_image);
			holder.name = (TextView) convertView
					.findViewById(R.id.download_item_text);
			holder.progress = (ProgressBar) convertView
					.findViewById(R.id.download_item_pgbar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();

		}
		final AppItem ai = (AppItem) getItem(position);
		MyLog.i(TAG, "item package:" + ai.getPacakgeName());
		holder.logo.setImageDrawable(AngleUitl.toRoundCorner(
				picGetter.getDefaultLogo(), 20));
		final ViewHolder vh = holder;
		picGetter.onLogo(ai.getPacakgeName(), new OnDrawableListener() {

			@Override
			public void onDrawable(Drawable d, String pkg) {
				if (ai.getPacakgeName().equals(pkg) && d != null) {
					vh.logo.setImageDrawable(d);
				}
			}

			@Override
			public void onDrawable(Drawable d, int appid) {
			}
		});
		holder.name.setText(ai.getName());
		holder.progress.setMax(ai.getSize());
		holder.progress.setProgress(ai.getComplete());
		pbs.put(ai.getId(), holder.progress);
		items.put(ai.getId(), ai);
		String btnText = null;
		holder.btn.setTag(ai);
		switch (ai.getStatus()) {
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
			btnText = "暂停";
			break;
		case AppItem.STATUS_PAUSED:
			btnText = "继续";
			break;
		case AppItem.STATUS_WAITING:
			btnText = "等待";
			break;
		}
		final int pos = position;
		holder.btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AppItem ai = (AppItem) v.getTag();
				MyLog.i(TAG,
						"ai is:" + pos + "|" + ai.getName()
								+ ai.getPacakgeName());
				switch (ai.getStatus()) {
				case AppItem.STATUS_INITIAL:
					// will not apear at this position
					break;
				case AppItem.STATUS_INSTALLED:
					// to uninstall
					ApkFileHelper.uninstall(ai.getPacakgeName(), mContext);
					break;
				case AppItem.STATUS_LOADED:
					// to install
					MyLog.i(TAG,
							"安装:" + ai.getName() + "|" + ai.getPacakgeName());
					ApkFileHelper.install(DownloadConfig.SECOND_LEVEL_PATH
							+ "/" + ai.getName() + ".apk", mContext);
					break;
				case AppItem.STATUS_LOADING:
					// to pause
					loadManager.pause(ai.getId());
					break;
				case AppItem.STATUS_PAUSED:
					// to continue
					// ai.setStatus(AppItem.STATUS_WAITING);
					if (loadManager.isInQueue(ai.getId())) {
						loadManager.continueDown(ai.getId());
					} else {
						startDown(loadManager, ai);
					}
					notifyDataSetChanged();
					break;
				case AppItem.STATUS_WAITING:
					// do nothing now TODO
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
					DataProvider provider = AppDataProvider
							.getInstance(mContext);
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
		holder.btn.setText(btnText);
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		return convertView;
	}

	protected void resetViewHolder(ViewHolder p_ViewHolder) {
		p_ViewHolder.name.setText(null);
		p_ViewHolder.logo.setImageDrawable(null);
		p_ViewHolder.btn.setOnClickListener(null);
		p_ViewHolder.progress.setProgress(0);
	}

	@Override
	public void notifyAllData() {
		notifyDataSetChanged();
	}

	@Override
	public void notifyDirytyData(int pos) {
		ProgressBar pb = pbs.get(new Integer(pos));
		AppItem ai = items.get(new Integer(pos));
		if (pb != null && ai != null) {
			pb.setMax(ai.getSize());
			pb.setProgress(ai.getComplete());
		}
	}

}
