package com.tblin.market.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.LocalActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tblin.embedmarket.AppMarketConfig;
import com.tblin.embedmarket.LogoDbHelper;
import com.tblin.embedmarket.MyLog;
import com.tblin.embedmarket.R;
import com.tblin.embedmarket.SessionManager;
import com.tblin.market.breakdown.LoadSequenceManager;

/**
 * when start this activity, should put Class in extra with keyword start_class
 */
public class EmbedHome extends MarketActivity {
	private TextView softpage;
	private TextView downloadpage;
	private ViewPager mviewpager;
	private List<View> listviews;
	private EditText ed;
	private int offset = 0; // 偏移量
	private int bmpW; // 图片宽度
	private int screenW;
	private ImageView cursor; // 滑动图片
	private LocalActivityManager manager;
	private boolean viewnum = false;
	private SessionManager session;
	private Intent it1, it2;
	private ViewPagerAdapter adapter;
	private Class<?> startClass;
	private static final String TAG = EmbedHome.class.toString();
	public static final String NOTIFICATION_TAG = EmbedHome.class.toString();
	public static final String CLOSE_BROADCAST = "com.tblin.market.ui.EmbedHome.close";
	private boolean isInListPage = true;

	public void setupView() {
		softpage = (TextView) findViewById(R.id.softwarepage);
		downloadpage = (TextView) findViewById(R.id.downloadpage);
		softpage.setOnClickListener(new MyOnClickListener(0));
		downloadpage.setOnClickListener(new MyOnClickListener(1));
		ed = (EditText) findViewById(R.id.ed_serach);
		
		session = SessionManager.getInstance(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.embedhome);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		it1 = new Intent(this, AppListActivity.class);
		it2 = new Intent(this, NewLoadManageActivity.class);
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);
		setupView();
	//	setTextLight(softpage, "精品推荐");
		initViewPager();
		InitImageView();
		initmviewPager();
		AppMarketConfig.IS_ACTIVITY_ALIVE = true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent it = getIntent();
		if (it.hasExtra("start_class")) {
			startClass = (Class<?>) it.getSerializableExtra("start_class");
			it.removeExtra("start_class");
		}
		viewnum = getIntent().getBooleanExtra(
				NotificationPoster.NOTIFICATION_TAG, false);
		MyLog.i(TAG, "on resume has notification_tag?" + viewnum);
		if (viewnum) {
			mviewpager.setCurrentItem(1);
			LoadSequenceManager lsm = (LoadSequenceManager) session
					.get("load_sequence");
			if (!lsm.hasTaskGoing()) {
				clearNotification();
			}
		}
		InitImageView();

	}

	class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mviewpager.setCurrentItem(index);
			switch (index) {

			case 0:
				softpage.setBackgroundResource(R.drawable.embed_soft);
				downloadpage.setBackgroundResource(R.drawable.embed_dl2);
			//	setTextLight(softpage, "精品推荐");
			//	setTextBack(downloadpage, "下载管理");
				break;
			case 1:
			//	setTextLight(downloadpage, "下载管理");
			//	setTextBack(softpage, "精品推荐");
				softpage.setBackgroundResource(R.drawable.embed_soft2);
				downloadpage.setBackgroundResource(R.drawable.embed_dl);
				break;
			}
		}

	}

	public void initmviewPager() {

		if (viewnum) {
			mviewpager.setCurrentItem(1);
		//	setTextLight(downloadpage, "下载管理");
		} else {
			mviewpager.setCurrentItem(0);
		}
		mviewpager.setOnPageChangeListener(new OnPageChangeListener() {
			int one = offset * 2 + bmpW;
			Animation animation = null;

			@Override
			public void onPageSelected(int arg0) {

				switch (arg0) {
				case 0:
				//	setTextLight(softpage, "精品推荐");
				//	setTextBack(downloadpage, "下载管理");
					softpage.setBackgroundResource(R.drawable.embed_soft);
					downloadpage.setBackgroundResource(R.drawable.embed_dl2);
					if (viewnum) {
						animation = new TranslateAnimation(0, -screenW / 2, 0,
								0);
					} else {
						animation = new TranslateAnimation(one + 5, 0, 0, 0);
					}
					break;
				case 1:
				//	setTextLight(downloadpage, "下载管理");
				//	setTextBack(softpage, "精品推荐");
					softpage.setBackgroundResource(R.drawable.embed_soft2);
					downloadpage.setBackgroundResource(R.drawable.embed_dl);
					if (viewnum) {
						animation = new TranslateAnimation(-screenW / 2, 0, 0,
								0);
					} else {
						animation = new TranslateAnimation(offset, one + 2, 0,
								0);
					}
					break;
				}

				animation.setFillAfter(true);// True:图片停在动画结束位置
				animation.setDuration(300);
				cursor.startAnimation(animation);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	private void initViewPager() {
		mviewpager = (ViewPager) findViewById(R.id.viewpager);
		listviews = new ArrayList<View>();
		listviews.add(getView("soft", it1));
		listviews.add(getView("down", it2));
		adapter = new ViewPagerAdapter(listviews);
		mviewpager.setAdapter(adapter);
	}

	public void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		//将游标隐藏了。
		cursor.setVisibility(View.GONE);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.cursor)
				.getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		if (viewnum) {
			matrix.postTranslate(screenW / 2 + offset, 0);
		} else {
			matrix.postTranslate(offset, 0);
		}
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	public void goBack(View view) {
		if (isInListPage) {
			if (startClass != null) {
				Intent it = new Intent(this, startClass);
				startActivity(it);
			} else {
				finish();
			}
		} else {
			Intent its = new Intent(this, AppListActivity.class);
			String srt = null;
			its.putExtra("keyword", srt);
			manager.startActivity("soft", its);
			mviewpager.setCurrentItem(0);
			isInListPage = true;
		}
	}

	private void clearNotification() {
		clearNotification(this);
	}

	public static void clearNotification(Context context) {
		NotificationManager manager = (NotificationManager) context
				.getSystemService("notification");
		manager.cancel(NOTIFICATION_TAG, 0);
	}

	public void goSearch(View view) {
		// 隐藏软键盘
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(EmbedHome.this.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		if (ed.getText().toString().trim().equals("")) {
			Toast.makeText(this, "请输入搜索内容", 2000).show();
		} else {
			it1.putExtra("keyword", ed.getText().toString());
			manager.startActivity("soft", it1);
			mviewpager.setCurrentItem(0);
			isInListPage = false;
		}
	}

	public View getView(String k, Intent i) {
		return manager.startActivity(k, i).getDecorView();
	}

	@Override
	protected void onDestroy() {
		MyLog.i(TAG, "embed home on destroy");
		clearNotification();
		session.clearSession();
		LogoDbHelper ldh = LogoDbHelper.getInstance(this);
		ldh.clearTemp();
		ldh.close();
		AppMarketConfig.IS_ACTIVITY_ALIVE = false;
		super.onDestroy();
	}

	public void finishChild() {
		manager.destroyActivity("soft", true);
		manager.destroyActivity("down", true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (isInListPage) {
				if (startClass != null) {
					Intent it = new Intent(this, startClass);
					startActivity(it);
					startClass = null;
					return false;
				} else {
					finishChild();
				}
			} else {
				Intent its = new Intent(this, AppListActivity.class);
				String srt = null;
				its.putExtra("keyword", srt);
				manager.startActivity("soft", its);
				mviewpager.setCurrentItem(0);
				isInListPage = true;
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setTextLight(TextView tv, String str) {

		SpannableStringBuilder style = new SpannableStringBuilder(str);
		style.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 4,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(style);

	}

	private void setTextBack(TextView tv, String str) {
		SpannableStringBuilder style = new SpannableStringBuilder(str);
		style.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 4,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(style);
	}
}
