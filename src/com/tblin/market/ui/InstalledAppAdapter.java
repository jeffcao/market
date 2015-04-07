package com.tblin.market.ui;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.DrawableGetter;
import com.tblin.embedmarket.DrawableGetter.OnDrawableListener;
import com.tblin.embedmarket.R;
import com.tblin.market.breakdown.ApkFileHelper;

public class InstalledAppAdapter extends BaseAdapter {

	private List<AppItem> items;
	private Context mContext;
	private LayoutInflater inflater;
	private DrawableGetter picGetter;

	private class ViewHolder {
		public ImageView logo;
		public TextView name;
		public TextView verison;
		public Button btn;
	}

	public InstalledAppAdapter(Context context, List<AppItem> items) {
		this.items = items;
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		picGetter = new DrawableGetter(mContext);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder = null;
		if (view == null) {
			holder = new ViewHolder();
			view = inflater.inflate(R.layout.installed_app_item, null);
			holder.logo = (ImageView) view
					.findViewById(R.id.installed_app_item_logo);
			holder.name = (TextView) view
					.findViewById(R.id.installed_app_item_name);
			holder.verison = (TextView) view
					.findViewById(R.id.installed_app_item_version);
			holder.btn = (Button) view
					.findViewById(R.id.installed_app_item_btn);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		final AppItem item = (AppItem) getItem(position);
		holder.name.setText(item.getName());
		holder.verison.setText("版本：" + item.getVersionName());
		holder.logo.setImageDrawable(picGetter.getDefaultLogo());
		final ViewHolder vh = holder;
		picGetter.onLogo(item.getPacakgeName(), new OnDrawableListener() {

			@Override
			public void onDrawable(Drawable d, String pkg) {
				if (item.getPacakgeName().equals(pkg) && d != null) {
					vh.logo.setImageDrawable(d);
				}
			}

			@Override
			public void onDrawable(Drawable d, int appid) {
			}
		});
		// holder.logo.setImageDrawable(item.getLogo());
		holder.btn.setText("卸载");
		holder.btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ApkFileHelper.uninstall(item.getPacakgeName(), mContext);

			}
		});
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
		return view;
	}

}
