package com.tblin.market.info;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tblin.embedmarket.AppItem;
import com.tblin.embedmarket.AppMarketConfig;
import com.tblin.embedmarket.MobileInfoGetter;
import com.tblin.embedmarket.MyLog;
import com.tblin.market.info.HttpFetcher.HttpResult;

/**
 * 这个类负责服务器各种接口的调用，消息会分发给HttpFetcher发送， 返回的结果会进行包装然后分发给HolderCenter进行处理。
 * 开始工作的时候要open这个类，退出程序的时候要close这个类。 也是单例模式
 * 
 */

public class Networker {

	private static Networker INSTANCE;
	private HttpFetcher fetcher;
	private HolderCenter holderCenter;
	private Context mContext;
	private SecurityCoder coder;
	private MobileInfoGetter mobileInfoGetter;
	private static final String URL_PREFIX = AppMarketConfig.PHP_SERVER_URL;// "http://192.168.0.208/market/doophp/market/index.php/";
	private static final String TAG = Networker.class.toString();

	private Networker(Context context) {
		fetcher = new HttpFetcher();
		holderCenter = HolderCenter.getInstance();
		mContext = context;
		mobileInfoGetter = new MobileInfoGetter(context);
		coder = new SecurityCoder() {

			@Override
			public String code(String url) {
				// return URLEncoder.encode(url);
				return url;
			}
		};
	}

	public static Networker getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new Networker(context);
		}
		return INSTANCE;
	}

	public void open() {
		fetcher.open();
	}

	public void close() {
		fetcher.close();
	}

	/**
	 * ai must has id and versioncode
	 * 
	 * @param ai
	 * @param type
	 * @throws IOException
	 */
	public void downNotify(AppItem ai, String type) throws IOException {
		if (AppMarketConfig.CONTAINER_APP_ID == null) {
			throw new IllegalArgumentException("appid should be setted");
		} else if (AppMarketConfig.CONTAINER_APP_VERSION == null) {
			throw new IllegalArgumentException("app version should be setted");
		}
		MyLog.i(TAG, "down notify type:" + type + "/id:" + ai.getId());
		Map<String, String> params = mobileInfoGetter.getAllImmediateInfo(
				AppMarketConfig.CONTAINER_APP_ID,
				AppMarketConfig.CONTAINER_APP_VERSION,
				Integer.toString(ai.getId()),
				Integer.toString(ai.getVersionCode()), type);
		List<BasicNameValuePair> pairs = PostExcuter.paramPairsPackage(params);
		PostExcuter.excutePost(URL_PREFIX + "downapk", pairs, mContext);
	}

	public void search(int groupId, String keywords, PageInfo page, int holderId) {
		String kw = URLEncoder.encode(keywords);
		String pg = URLEncoder.encode(Integer.toString(page.currentPage + 1));
		String sz = URLEncoder.encode(Integer.toString(page.pageSize));
		String hid = URLEncoder.encode(Integer.toString(holderId));
		String gd = URLEncoder.encode(Integer.toString(groupId));
		String url = URL_PREFIX + "searchapk/" + kw + "/" + pg + "/" + sz + "/"
				+ gd + "/" + hid;
		String formedUrl = coder.code(url);
		requestUrl(formedUrl);
	}

	public void getAppList(int groupId, PageInfo page, int holderId) {
		String gid = URLEncoder.encode(Integer.toString(groupId));
		String pg = URLEncoder.encode(Integer.toString(page.currentPage + 1));
		String sz = URLEncoder.encode(Integer.toString(page.pageSize));
		String hid = URLEncoder.encode(Integer.toString(holderId));
		String url = URL_PREFIX + "apklist/" + gid + "/" + pg + "/" + sz + "/"
				+ hid;
		String formedUrl = coder.code(url);
		requestUrl(formedUrl);
	}

	public void getAppInfo(int appid, int holderId) {
		String aid = URLEncoder.encode(Integer.toString(appid));
		String hid = URLEncoder.encode(Integer.toString(holderId));
		String url = URL_PREFIX + "apkmsg/" + aid + "/" + hid;
		String formedUrl = coder.code(url);
		requestUrl(formedUrl);
	}

	public void getAppLogo(int appid, int holderId) {
		String aid = URLEncoder.encode(Integer.toString(appid));
		String hid = URLEncoder.encode(Integer.toString(holderId));
		String url = URL_PREFIX + "apklogo/" + aid + "/" + hid;
		String formedUrl = coder.code(url);
		requestUrl(formedUrl);
	}

	public void getAppImage(int appid, int wantNum, int holderId) {
		String aid = URLEncoder.encode(Integer.toString(appid));
		String hid = URLEncoder.encode(Integer.toString(holderId));
		String wnm = URLEncoder.encode(Integer.toString(wantNum));
		String url = URL_PREFIX + "apkjpg/" + aid + "/" + wnm + "/" + hid;
		String formedUrl = coder.code(url);
		requestUrl(formedUrl);
	}

	public void getAppUrl(int appid, int holderId) {
		String aid = URLEncoder.encode(Integer.toString(appid));
		String hid = URLEncoder.encode(Integer.toString(holderId));
		String url = URL_PREFIX + "apkurl/" + aid + "/" + hid;
		String fromedUrl = coder.code(url);
		requestUrl(fromedUrl);
	}

	private void requestUrl(String url) {
		MyLog.i(TAG, "request url by get: " + url);
		HttpGet req = new HttpGet(url);
		HttpResult cb = new HttpResult() {
			@Override
			public void notify(HttpRequest req, HttpResponse resp) {
				int httpCode = resp.getStatusLine().getStatusCode();
				int holder = getHolder(req.getRequestLine().getUri());
				if (holder == -1) {
					// 直接丢弃消息
					MyLog.w(TAG, "discard message");
					return;
				}
				String httpResult = null;
				if (httpCode != 200) {
					netDisableHandle(req);
				} else {
					try {
						httpResult = EntityUtils.toString(resp.getEntity());
					} catch (Exception e) {
						httpResult = getNetDisableJson(req);
					}
					parseJson(httpResult, holder);
				}
			}

			@Override
			public void notify(HttpRequest req, IOException e)
					throws IOException {
				netDisableHandle(req);
			}
		};
		try {
			fetcher.invoke(req, cb);
			MyLog.i(TAG, "fetcher invoked");
		} catch (InterruptedException e1) {
			netDisableHandle(req);
		}
	}

	private void netDisableHandle(HttpRequest req) {
		String msg = getNetDisableJson(req);
		JSONObject root = null;
		if (msg != null) {
			try {
				root = new JSONObject(msg);
				holderCenter.parse(root);
			} catch (JSONException e) {
				// this will not happen
			}
		} else {
			// 消息已经被丢弃
		}
	}

	private void parseJson(String text, int holder) {
		if (text == null) {
			MyLog.w(TAG, "discard message");
			return;
		}
		JSONObject root = null;
		try {
			root = new JSONObject(text);
		} catch (JSONException e) {
			try {
				root = new JSONObject(genJsonFormatErr(holder));
			} catch (JSONException e1) {
				// never happen
			}
		}
		holderCenter.parse(root);
	}

	private String getNetDisableJson(HttpRequest req) {
		int holder = getHolder(req.getRequestLine().getUri());
		if (holder == -1) {
			// 直接丢弃消息
			MyLog.i(TAG, "discard message");
			return null;
		}
		String httpResult = null;
		if (isNetConnected()) {
			// 100
			httpResult = errJsonWrap(holder, 101, "server access error");
		} else {
			// 101
			httpResult = neterrWrap(holder);
		}
		return httpResult;
	}

	/**
	 * 假如url里面找不到holder数据，就返回-1 holder为-1的消息直接丢弃
	 */
	private int getHolder(String url) {
		int holder = -1;
		if (url == null) {
			return holder;
		}
		int pos = url.lastIndexOf("/");
		String sub = url.substring(pos + 1);
		try {
			holder = Integer.parseInt(sub);
		} catch (Exception e) {

		}
		return holder;
	}

	private String neterrWrap(int holder) {
		return errJsonWrap(holder, 100, "network is not availeable");
	}

	private String errJsonWrap(int holder, int code, String info) {
		JSONObject root = null;
		try {
			if (info == null) {
				info = "";
			}
			root = new JSONObject();
			root.put("holder", holder);
			JSONObject data = new JSONObject();
			data.put("type", 2);
			JSONObject result = new JSONObject();
			result.put("type", code);
			result.put("msg", info);
			result.put("sub_type", 0);
			data.put("result", result);
			root.put("data", data);
		} catch (JSONException e) {
			return generateUnknowErr(holder);
		}
		return root.toString();
	}

	private String genJsonFormatErr(int holder) {
		return "{\"holder\":\""
				+ holder
				+ "\"}"
				+ "\"data\":{\"type\":\"2\",\"result\":{\"type\":\"104\",\"msg\":\"json format error\",\"sub_type\":\"0\"}}";
	}

	private String generateUnknowErr(int holder) {
		return "{\"holder\":\""
				+ holder
				+ "\"}"
				+ "\"data\":{\"type\":\"2\",\"result\":{\"type\":\"109\",\"msg\":\"unknow error\",\"sub_type\":\"0\"}}";
	}

	private boolean isNetConnected() {
		boolean result = false;
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			NetworkInfo.State state = info.getState();
			if (state == NetworkInfo.State.CONNECTED) {
				result = true;
			}
		}
		return result;
	}

}
