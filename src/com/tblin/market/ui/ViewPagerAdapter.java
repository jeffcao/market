package com.tblin.market.ui;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.support.v4.view.ViewPager;

public class ViewPagerAdapter extends PagerAdapter {
	private List<View> lists;

	public ViewPagerAdapter(List<View> lists) {
		this.lists = lists;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {

		((ViewPager) arg0).removeView(lists.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {

	}

	@Override
	public int getCount() {

		return lists.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(lists.get(arg1), 0);

		return lists.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {

	}

	@Override
	public Parcelable saveState() {

		return null;
	}

	@Override
	public void startUpdate(View arg0) {

	}

	@Override
	public int getItemPosition(Object object) {

		return POSITION_NONE;
	}

}
