package com.tblin.market.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.tblin.embedmarket.R;

public class InfosGaAdapter extends BaseAdapter {
	private List<Drawable> draws;
	private LayoutInflater inflater;

	public InfosGaAdapter(Context context, List<Drawable> draws) {
		if (draws == null) {
			draws = new ArrayList<Drawable>();
		} else {
			this.draws = draws;
		}
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if (draws.size() == 0) {
			return 5;
		}
		return draws.size();
	}

	@Override
	public Object getItem(int position) {
		return draws.get(position);
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
			convertView = inflater.inflate(R.layout.infos_ga_ad, null);
			holder.iv = (ImageView) convertView.findViewById(R.id.infos_ga_im);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (draws.size() == 0) {
			holder.iv.setImageResource(R.drawable.defalut_img);
		} else {
			holder.iv.setImageDrawable(draws.get(position));
		}
		return convertView;
	}

	class ViewHolder {
		private ImageView iv;
	}

}
