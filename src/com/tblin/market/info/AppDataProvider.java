package com.tblin.market.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.tblin.embedmarket.AppInfo;
import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.MyLog;
import com.tblin.market.info.Holder.HolderCreator;

/**
 * 要想使用数据，必须先open 在退出程序的时候要close
 * 
 */
public class AppDataProvider implements DataProvider {

	private HolderCenter holderCenter;
	private Networker worker;
	private Context mContext;
	private Base64Utility base64;
	private static AppDataProvider INSTANCE;
	private static final String TAG = AppDataProvider.class.toString();

	private AppDataProvider(Context context) {
		holderCenter = HolderCenter.getInstance();
		worker = Networker.getInstance(context);
		mContext = context;
		base64 = new Base64Utility();
	}

	public static AppDataProvider getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new AppDataProvider(context);
		}
		return INSTANCE;
	}

	public void open() {
		worker.open();
	}

	public void close() {
		worker.close();
	}

	@Override
	public void onAppItem(int groupId, PageInfo page, DataListener lsnr) {
		if (lsnr == null) {
			throw new IllegalArgumentException("lsnr can not be null");
		}
		final IDataListener ilsnr = new IDataListener(lsnr, mContext);
		if (groupId < 0) {
			// 107
			on107Error("group id", groupId, ilsnr);
			return;
		}
		if (page == null) {
			// 106
			OnAppError err = new OnAppError(106, "page should not be null");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("error", err);
			ilsnr.onData(data);
			return;
		}
		if (page.currentPage == page.totalPage) {
			// 108
			OnAppError err = new OnAppError(108, "page had been get to empty");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("error", err);
			ilsnr.onData(data);
			return;
		}
		AbstractJsonParser parser = new AbstractJsonParser() {

			@Override
			public void parse(JSONObject jsonObject, DataListener lsnr) {
				try {
					PageInfo pageInfo = new PageInfo();
					int cPage = jsonObject.getInt("page");
					int totalPage = jsonObject.getInt("total_page");
					int totalRecord = jsonObject.getInt("total_record");
					int pageSize = jsonObject.getInt("page_size");
					int cRecord = jsonObject.getInt("current_record");
					pageInfo.currentPage = cPage;
					pageInfo.currentRecord = cRecord;
					pageInfo.pageSize = pageSize;
					pageInfo.totalPage = totalPage;
					pageInfo.totalRecord = totalRecord;
					MyLog.i(TAG, "pageinfo-----------------");
					MyLog.i(TAG, pageInfo.toString());
					JSONArray apps = jsonObject.getJSONArray("data");
					List<AppItem> appItems = new ArrayList<AppItem>();
					for (int i = 0; i < apps.length(); i++) {
						try {
							JSONObject item = apps.getJSONObject(i);
							AppItem app = new AppItem();
							app.setCompany(item.getString("comp"));
							app.setId(item.getInt("id"));
							app.setName(item.getString("name"));
							app.setPacakgeName(item.getString("package_name"));
							app.setSize(item.getInt("size"));
							app.setVersionCode(item.getInt("version_code"));
							app.setVersionName(item.getString("version_name"));
							appItems.add(app);
						} catch (JSONException e) {
							MyLog.w(TAG, e.getMessage());
							continue;
						}
					}
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("page_info", pageInfo);
					data.put("app_item", appItems);
					lsnr.onData(data);
				} catch (JSONException e) {
					onFormatException(e, lsnr);
				}
			}
		};
		Holder holder = HolderCreator.creatHolder(parser, ilsnr);
		holderCenter.registHolder(holder);
		worker.getAppList(groupId, page, holder.getId());
		MyLog.i(TAG, "start to call networker");
	}

	@Override
	public void onAppInfo(int appid, DataListener lsnr) {
		if (lsnr == null) {
			throw new IllegalArgumentException("lsnr can not be null");
		}
		final IDataListener ilsnr = new IDataListener(lsnr, mContext);
		if (appid < 0) {
			// 107
			on107Error("app id", appid, ilsnr);
			return;
		}
		AbstractJsonParser parser = new AbstractJsonParser() {

			@Override
			public void parse(JSONObject jsonObject, DataListener lsnr) {
				try {
					int id = jsonObject.getInt("id");
					String introduce = jsonObject.getString("intro");
					String type = jsonObject.getString("apktype");
					String zifei = jsonObject.getString("charge");
					int images = jsonObject.getInt("images");
					AppInfo appInfo = new AppInfo();
					appInfo.setType(type);
					appInfo.setZifei(zifei);
					MyLog.i(TAG, "zifei:" + zifei + ", type:" + type);
					appInfo.setIntroduce(introduce);
					appInfo.setTotalImage(images);
					appInfo.setId(id);
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("app_info", appInfo);
					lsnr.onData(data);
				} catch (JSONException e) {
					onFormatException(e, lsnr);
				}

			}
		};
		Holder holder = HolderCreator.creatHolder(parser, ilsnr);
		holderCenter.registHolder(holder);
		worker.getAppInfo(appid, holder.getId());
	}

	@Override
	public void onAppLogo(int appid, DataListener lsnr) {
		if (lsnr == null) {
			throw new IllegalArgumentException("lsnr can not be null");
		}
		final IDataListener ilsnr = new IDataListener(lsnr, mContext);
		if (appid < 0) {
			// 107
			on107Error("app id", appid, ilsnr);
			return;
		}
		AbstractJsonParser parser = new AbstractJsonParser() {

			@Override
			public void parse(JSONObject jsonObject, DataListener lsnr) {
				try {
					int id = jsonObject.getInt("id");
					String logo = jsonObject.getString("data");
					Drawable drawable = base64.base64ToDrawable(logo);
					if (drawable == null) {
						OnAppError err = new OnAppError(105,
								"json format error, base64 can not trans to drawable");
						Map<String, Object> data = new HashMap<String, Object>();
						data.put("error", err);
						lsnr.onData(data);
						return;
					}
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("id", id);
					data.put("logo", drawable);
					lsnr.onData(data);
				} catch (JSONException e) {
					onFormatException(e, lsnr);
				}
			}
		};
		Holder holder = HolderCreator.creatHolder(parser, ilsnr);
		holderCenter.registHolder(holder);
		worker.getAppLogo(appid, holder.getId());
	}

	@Override
	public void onAppImage(int appid, int wantNum, DataListener lsnr) {
		if (lsnr == null) {
			throw new IllegalArgumentException("lsnr can not be null");
		}
		final IDataListener ilsnr = new IDataListener(lsnr, mContext);
		if (appid < 0) {
			// 107
			on107Error("app id", appid, ilsnr);
			return;
		}
		if (wantNum < 0) {
			// 107
			on107Error("want num", wantNum, ilsnr);
			return;
		}

		AbstractJsonParser parser = new AbstractJsonParser() {

			@Override
			public void parse(JSONObject jsonObject, DataListener lsnr) {
				try {
					int id = jsonObject.getInt("id");
					int num = jsonObject.getInt("num");
					String image = jsonObject.getString("data");
					Drawable drawable = base64.base64ToDrawable(image);
					if (drawable == null) {
						OnAppError err = new OnAppError(105,
								"json format error, base64 can not trans to drawable");
						Map<String, Object> data = new HashMap<String, Object>();
						data.put("error", err);
						lsnr.onData(data);
						return;
					}
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("id", id);
					data.put("image", drawable);
					data.put("current_num", num);
					lsnr.onData(data);
				} catch (JSONException e) {
					onFormatException(e, lsnr);
				}
			}
		};
		Holder holder = HolderCreator.creatHolder(parser, ilsnr);
		holderCenter.registHolder(holder);
		worker.getAppImage(appid, wantNum, holder.getId());
	}

	@Override
	public void onAppUrl(int appid, DataListener lsnr) {
		Holder holder = onUrl(appid, lsnr);
		if (holder == null) {
			// holder为Null说明出现了错误，转入了错误处理
			return;
		}
		worker.getAppUrl(appid, holder.getId());
	}

	private Holder onUrl(int appid, DataListener lsnr) {
		if (lsnr == null) {
			throw new IllegalArgumentException("lsnr can not be null");
		}
		final IDataListener ilsnr = new IDataListener(lsnr, mContext);
		if (appid < 0) {
			// 107
			on107Error("app id", appid, ilsnr);
			return null;
		}

		AbstractJsonParser parser = new AbstractJsonParser() {

			@Override
			public void parse(JSONObject jsonObject, DataListener lsnr) {
				try {
					int id = jsonObject.getInt("id");
					String url1 = jsonObject.getString("url1");
					String url2 = jsonObject.getString("url2");
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("id", id);
					data.put("url_normal", url1);
					data.put("url_ownserver", url2);
					lsnr.onData(data);
				} catch (JSONException e) {
					onFormatException(e, lsnr);
				}
			}
		};
		Holder holder = HolderCreator.creatHolder(parser, ilsnr);
		holderCenter.registHolder(holder);
		return holder;
	}

	private void on107Error(String name, int value, DataListener lsnr) {
		OnAppError err = new OnAppError(107, name + " should big than 0: "
				+ value);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("error", err);
		lsnr.onData(data);
	}

	private void onFormatException(JSONException e, DataListener lsnr) {
		OnAppError err = new OnAppError(104, "json format error:"
				+ e.getMessage());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("error", err);
		lsnr.onData(data);
	}

	@Override
	public void onSearch(String keywords, int groupId, PageInfo page,
			DataListener lsnr) {
		if (lsnr == null) {
			throw new IllegalArgumentException("lsnr can not be null");
		}
		final IDataListener ilsnr = new IDataListener(lsnr, mContext);
		if (page == null) {
			// 106
			OnAppError err = new OnAppError(106, "page should not be null");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("error", err);
			ilsnr.onData(data);
			return;
		}
		if (page.currentPage == page.totalPage) {
			// 108
			OnAppError err = new OnAppError(108, "page had been get to empty");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("error", err);
			ilsnr.onData(data);
			return;
		}
		AbstractJsonParser parser = new AbstractJsonParser() {

			@Override
			public void parse(JSONObject jsonObject, DataListener lsnr) {
				try {
					PageInfo pageInfo = new PageInfo();
					int cPage = jsonObject.getInt("page");
					int totalPage = jsonObject.getInt("total_page");
					int totalRecord = jsonObject.getInt("total_record");
					int pageSize = jsonObject.getInt("page_size");
					int cRecord = jsonObject.getInt("current_record");
					pageInfo.currentPage = cPage;
					pageInfo.currentRecord = cRecord;
					pageInfo.pageSize = pageSize;
					pageInfo.totalPage = totalPage;
					pageInfo.totalRecord = totalRecord;
					MyLog.i(TAG, "pageinfo-----------------");
					MyLog.i(TAG, pageInfo.toString());
					JSONArray apps = jsonObject.getJSONArray("data");
					List<AppItem> appItems = new ArrayList<AppItem>();
					for (int i = 0; i < apps.length(); i++) {
						try {
							JSONObject item = apps.getJSONObject(i);
							AppItem app = new AppItem();
							app.setCompany(item.getString("comp"));
							app.setId(item.getInt("id"));
							app.setName(item.getString("name"));
							app.setPacakgeName(item.getString("package_name"));
							app.setSize(item.getInt("size"));
							app.setVersionCode(item.getInt("version_code"));
							app.setVersionName(item.getString("version_name"));
							appItems.add(app);
						} catch (JSONException e) {
							MyLog.w(TAG, e.getMessage());
							continue;
						}
					}
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("page_info", pageInfo);
					data.put("app_item", appItems);
					lsnr.onData(data);
				} catch (JSONException e) {
					onFormatException(e, lsnr);
				}
			}
		};
		Holder holder = HolderCreator.creatHolder(parser, ilsnr);
		holderCenter.registHolder(holder);
		worker.search(groupId, keywords, page, holder.getId());
		MyLog.i(TAG, "start to call networker");
	}

}
