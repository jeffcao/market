package com.tblin.market.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.tblin.market.breakdown.DownloadConfig;
import com.tblin.market.breakdown.DownloadManager;
import com.tblin.market.breakdown.GlobalDownLsnr;
import com.tblin.market.breakdown.LoadSequenceManager;
import com.tblin.market.breakdown.Refresher;
import com.tblin.market.info.AppDataProvider;
import com.tblin.market.info.DataListener;
import com.tblin.market.info.DataProvider;
import com.tblin.market.info.OnAppError;

public class ExpandableAdapter extends BaseExpandableListAdapter implements
		Refresher {

	private List<String> groupTitle;
	private List<List<AppItem>> manageApps;
	private LayoutInflater inflater;
	private Context mContext;
	private LoadSequenceManager loadManager;
	private Map<Integer, ProgressBar> pbs;
	private Map<Integer, AppItem> items;
	private Handler hdlr;
	private DrawableGetter picGetter;
	private static final String TAG = ExpandableAdapter.class.toString();

	private class ViewHolder {
		public ImageView logo,btim;
		public TextView name;
		public ProgressBar progress;
		public Button btn;
		public TextView version;
		public TextView size;
		public int groupPosition;
		public LinearLayout btdown;
	}

	public ExpandableAdapter(List<List<AppItem>> manageApps, Context context) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		this.manageApps = manageApps;
		loadManager = (LoadSequenceManager) SessionManager.getInstance(context)
				.get("load_sequence");
		pbs = new HashMap<Integer, ProgressBar>();
		items = new HashMap<Integer, AppItem>();
		initGroupTitle();
		picGetter = new DrawableGetter(mContext);
		hdlr = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					toast(DownloadManager.NO_NET_NOTIFY_MESSAGE);
				}
			}
		};
	}

	private void initGroupTitle() {

		groupTitle = new ArrayList<String>();
		groupTitle.add("下载管理");
		groupTitle.add("已安装");
	}

	@Override
	public void notifyDataSetChanged() {
		initGroupTitle();
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		initGroupTitle();
		super.notifyDataSetInvalidated();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return manageApps.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	private int gcFlag = 0;
	private static final int GC_VALUE = 50;

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final AppItem ai = (AppItem) getChild(groupPosition, childPosition);
		ViewHolder holder = null;
		gcFlag++;
		if (gcFlag > GC_VALUE) {
			System.gc();
			gcFlag = 0;
		}
		if (convertView == null
				|| ((ViewHolder) convertView.getTag()).groupPosition != groupPosition) {
			convertView = null;
			holder = new ViewHolder();
			if (groupPosition == 0) {
				convertView = inflater.inflate(R.layout.download_item, null);
				holder.btn = (Button) convertView
						.findViewById(R.id.download_item_btn);
				holder.logo = (ImageView) convertView
						.findViewById(R.id.download_item_image);
				holder.name = (TextView) convertView
						.findViewById(R.id.download_item_text);
				holder.progress = (ProgressBar) convertView
						.findViewById(R.id.download_item_pgbar);
				holder.btdown=(LinearLayout) convertView.findViewById(R.id.download_item_bt);
				holder.btim=(ImageView) convertView.findViewById(R.id.download_app_down_im);
			} else {
				convertView = inflater.inflate(R.layout.installed_app_item,
						null);
				holder.btn = (Button) convertView
						.findViewById(R.id.installed_app_item_btn);
				holder.logo = (ImageView) convertView
						.findViewById(R.id.installed_app_item_logo);
				holder.name = (TextView) convertView
						.findViewById(R.id.installed_app_item_name);
				holder.size = (TextView) convertView
						.findViewById(R.id.installed_app_item_size);
				holder.version = (TextView) convertView
						.findViewById(R.id.installed_app_item_version);
				holder.btdown=(LinearLayout) convertView.findViewById(R.id.linearLayout1);
				holder.btim=(ImageView) convertView.findViewById(R.id.installed_app_down_im);
			}
			holder.groupPosition = groupPosition;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// holder.logo.setImageDrawable(ai.getLogo());
		Drawable d = null;
		if (groupPosition == 0) {
			d = picGetter.getLogo(ai.getId());
		} else {
			d = picGetter.getLogo(ai.getPacakgeName());
		}
		holder.logo.setImageDrawable(d);
		if (d == DrawableGetter.DEFAULT_DRAWABLE) {
			final ViewHolder vh = holder;
			OnDrawableListener lsnr = null;
			if (groupPosition == 0) {
				lsnr = new OnDrawableListener() {

					@Override
					public void onDrawable(Drawable d, String pkg) {
					}

					@Override
					public void onDrawable(Drawable d, int appid) {
						if (ai.getId() == appid) {
							vh.logo.setImageDrawable(d);
						}
					}
				};
				picGetter.onLogo(ai.getId(), lsnr);
			} else {
				lsnr = new OnDrawableListener() {

					@Override
					public void onDrawable(Drawable d, String pkg) {
						if (ai.getPacakgeName().equals(pkg) && d != null) {
							vh.logo.setImageDrawable(d);
						}
					}

					@Override
					public void onDrawable(Drawable d, int appid) {
					}
				};
				picGetter.onLogo(ai.getPacakgeName(), lsnr);
			}
		}
		holder.name.setText(ai.getName());
		if (groupPosition == 0) {
			holder.progress.setMax(ai.getSize());
			holder.progress.setProgress(ai.getComplete());
			pbs.put(ai.getId(), holder.progress);
		} else {
			holder.size.setText(SizeTextGetter.getSize(ai.getSize()));
			holder.version.setText("版本：" + ai.getVersionName().toString());
		}
		items.put(ai.getId(), ai);
		Drawable btdrawable = null;
		String btnText = null;
		switch (ai.getStatus()) {
		case AppItem.STATUS_INITIAL:
			btnText = "下载";
			btdrawable = convertView.getResources().getDrawable(
					R.drawable.download);
			break;
		case AppItem.STATUS_INSTALLED:
			btnText = "卸载";
			btdrawable = convertView.getResources().getDrawable(
					R.drawable.uninsta);
			break;
		case AppItem.STATUS_LOADED:
			btnText = "安装";
			btdrawable = convertView.getResources().getDrawable(
					R.drawable.setup);
			break;
		case AppItem.STATUS_LOADING:
			btnText = "暂停";
			btdrawable = convertView.getResources().getDrawable(
					R.drawable.pause);
			break;
		case AppItem.STATUS_PAUSED:
			btnText = "继续";
			btdrawable = convertView.getResources().getDrawable(
					R.drawable.contine);
			break;
		case AppItem.STATUS_WAITING:
			btnText = "等待";
			btdrawable = convertView.getResources()
					.getDrawable(R.drawable.wait);
			break;
		}
		holder.btdown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
					ai.setStatus(AppItem.STATUS_WAITING);
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
//		holder.btn.setCompoundDrawablesWithIntrinsicBounds(null,
//				AngleUitl.toRoundCorner(btdrawable, 5), null, null);
		holder.btim.setBackgroundDrawable(btdrawable);
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (ai.getStatus()) {
				case AppItem.STATUS_INSTALLED:
					// to open or uninstall
					final QuickActions qa = new QuickActions(v);
					ActionItem open = new ActionItem();
					open.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							boolean result = ApkFileHelper
									.startAppByPackageName(ai.getPacakgeName(),
											mContext);
							if (!result) {
								toast("程序无法直接打开");
							}
							qa.dismiss();
						}
					});
					open.setTitle("打开");
					ActionItem uninstall = new ActionItem();
					uninstall.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							ApkFileHelper.uninstall(ai.getPacakgeName(),
									mContext);
							qa.dismiss();
						}
					});
					uninstall.setTitle("卸载");
					qa.addActionItem(open);
					qa.addActionItem(uninstall);
					qa.show();
					break;
				case AppItem.STATUS_LOADED:
					final QuickActions qaLoaded = new QuickActions(v);
					ActionItem install = new ActionItem();
					install.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							ApkFileHelper.install(
									DownloadConfig.SECOND_LEVEL_PATH + "/"
											+ ai.getName() + ".apk", mContext);
							qaLoaded.dismiss();
						}
					});
					install.setTitle("安装");
					ActionItem delete = new ActionItem();
					delete.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							File file = new File(
									DownloadConfig.SECOND_LEVEL_PATH + "/"
											+ ai.getName() + ".apk");
							if (file.exists()) {
								file.delete();
							}
							int id = ai.getId();
							LoadSequenceManager manager = (LoadSequenceManager) SessionManager
									.getInstance(mContext).get("load_sequence");
							manager.cancel(id);
							qaLoaded.dismiss();
						}
					});
					delete.setTitle("删除");
					qaLoaded.addActionItem(delete);
					qaLoaded.addActionItem(install);
					qaLoaded.show();
					break;
				default:
					final QuickActions qaDefault = new QuickActions(v);
					ActionItem cancel = new ActionItem();
					cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							int id = ai.getId();
							LoadSequenceManager manager = (LoadSequenceManager) SessionManager
									.getInstance(mContext).get("load_sequence");
							manager.cancel(id);
							qaDefault.dismiss();
						}
					});
					cancel.setTitle("取消下载");
					ActionItem info = new ActionItem();
					info.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							SessionManager session = SessionManager
									.getInstance(mContext);

							if (AppListActivity.lastNetType == null) {
								Toast.makeText(mContext, "网络已经断开，请检查.", 2000)
										.show();
							} else {
								Intent it = new Intent(mContext,
										InfosActivity.class);
								it.putExtra("appID", ai.getId());
								session.put("appitem", ai);
								session.put("appname", ai.getName());
								session.put("appversion", ai.getVersionName());
								session.put("appsize", ai.getSize());
								session.put("cat", ai.getCompany());
								mContext.startActivity(it);
							}
							qaDefault.dismiss();
						}
					});
					info.setTitle("查看");
					qaDefault.addActionItem(cancel);
					qaDefault.addActionItem(info);
					qaDefault.show();
					break;
				}
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return manageApps.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupTitle.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupTitle.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		convertView =  inflater.inflate(R.layout.group, null);
		
		ImageView image = (ImageView) convertView.findViewById(R.id.test_imge);
		TextView text = (TextView) convertView.findViewById(R.id.textid);
		text.setTextSize(16);
		TextView textsize = (TextView) convertView.findViewById(R.id.textsize);
		text.setText(groupTitle.get(groupPosition));
		textsize.setText("[" + manageApps.get(groupPosition).size() + "]");
		if (isExpanded) {
			image.setImageDrawable(convertView.getResources().getDrawable(
					R.drawable.opened));
		} else {
			image.setImageDrawable(convertView.getResources().getDrawable(
					R.drawable.closed));
		}
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
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

	private void toast(String str) {
		if (str == null) {
			return;
		}
		Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
	}
}