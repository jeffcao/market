package com.tblin.embedmarket;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import com.tblin.market.info.AppDataProvider;
import com.tblin.market.info.DataListener;
import com.tblin.market.info.DataProvider;
import com.tblin.market.info.OnAppError;

public class DrawableGetter {

	public static Drawable DEFAULT_DRAWABLE;
	private LogoDbHelper logoDb;
	private Handler hdlr;
	private DataProvider provider;
	private static Queue<KeyDrawable> mCache;
	private static final int CACHE_CAPACITY = 10;
	private static final String TAG = DrawableGetter.class.toString();

	private class KeyDrawable {
		String key;
		Drawable d;

		public KeyDrawable(String key, Drawable d) {
			this.d = d;
			this.key = key;
		}
	}

	public interface OnDrawableListener {
		void onDrawable(Drawable d, String pkg);

		void onDrawable(Drawable d, int appid);
	}

	public DrawableGetter(Context context) {
		if (DEFAULT_DRAWABLE == null) {
			DEFAULT_DRAWABLE = context.getResources().getDrawable(
					R.drawable.defalut_logo);
		}
		logoDb = LogoDbHelper.getInstance(context);
		hdlr = new Handler();
		provider = AppDataProvider.getInstance(context);
		if (mCache == null) {
			mCache = new ConcurrentLinkedQueue<KeyDrawable>();
		}
	}

	private Drawable getDrawable(String key) {
		Iterator<KeyDrawable> ite = mCache.iterator();
		while (ite.hasNext()) {
			KeyDrawable kd = ite.next();
			if (kd.key.equals(key)) {
				return kd.d;
			}
		}
		return null;
	}

	public Drawable getLogo(String key) {
		Drawable d = getDrawable(key);
		MyLog.i(TAG, "get logo: " + key + (d == null ? "can't find" : "find"));
		return d == null ? DEFAULT_DRAWABLE : d;
	}

	public Drawable getLogo(int appid) {
		return getLogo(Integer.toString(appid));
	}

	private void insertToCache(int appid, Drawable d) {
		insertToCache(Integer.toString(appid), d);
	}

	private void insertToCache(String key, Drawable d) {
		if (mCache.size() == CACHE_CAPACITY) {
			KeyDrawable kd = mCache.poll();
			MyLog.i(TAG, "cache is full remove: " + kd.key);
		}
		MyLog.i(TAG, "insert:" + key);
		mCache.add(new KeyDrawable(key, d));
	}

	public Drawable getDefaultLogo() {
		return DEFAULT_DRAWABLE;
	}

	public void onLogo(final int appid, final OnDrawableListener lsnr) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				final Drawable d = logoDb.getLogoByAppid(appid);
				if (d == null) {
					DataListener dataLsnr = new DataListener() {

						@Override
						public void onData(Map<String, Object> data) {
							if (data.containsKey("error")) {
								OnAppError err = (OnAppError) data.get("error");
								MyLog.e(TAG, err.toString());
								return;
							}
							final Drawable draw = (Drawable) data.get("logo");
							logoDb.insertLogo(appid, draw);
							insertToCache(appid, draw);
							int k = (Integer) data.get("id");
							if (k == appid) {
								hdlr.post(new Runnable() {

									@Override
									public void run() {
										lsnr.onDrawable(draw, appid);
									}
								});
							}
						}
					};
					provider.onAppLogo(appid, dataLsnr);
				} else {
					insertToCache(appid, d);
					hdlr.post(new Runnable() {

						@Override
						public void run() {
							lsnr.onDrawable(d, appid);
						}
					});
				}
			}

		};
		new Thread(r).start();
	}

	public void onLogo(final String pkg, final OnDrawableListener lsnr) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				final Drawable d = logoDb.getLogoByPkg(pkg);
				insertToCache(pkg, d);
				hdlr.post(new Runnable() {

					@Override
					public void run() {
						lsnr.onDrawable(d, pkg);
					}
				});

			}
		};
		new Thread(r).start();
	}
}
