package com.tblin.market.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.URLUtil;

import com.tblin.embedmarket.MyLog;

public class PostExcuter {

	private static HttpHost proxy;
	private static boolean isProxySetted = false;
	private static final String TAG = PostExcuter.class.toString();
	private static HttpHost CTPROXY = new HttpHost("10.0.0.200", 80, "http");// 电信wap代理
	private static HttpHost CMPROXY = new HttpHost("10.0.0.172", 80, "http");// 移动联通代理

	public static String excutePost(String url,
			List<BasicNameValuePair> paramPairs, Context context)
			throws ClientProtocolException, IOException {
		if (context == null) {
			return null;
		}
		MyLog.d(TAG, "post excute this url:" + url);
		String smsContent = null;
		if (URLUtil.isHttpUrl(url)) {
			DefaultHttpClient client = new DefaultHttpClient();
			String apnType = APNGetter.getApnType(context);
			MyLog.i(TAG, "apn type is :" + apnType);
			if (APNGetter.WAP.equals(apnType) && !isWifi(context)) {
				setProxy(context);
				if (proxy != null) {
					MyLog.i(TAG, "set proxy");
					client.getParams().setParameter(
							ConnRoutePNames.DEFAULT_PROXY, proxy);
				}
			}
			HttpPost httpPost = new HttpPost(url);
			UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(
					paramPairs, HTTP.UTF_8);
			httpPost.setEntity(p_entity);
			HttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200) {
				smsContent = EntityUtils.toString(entity);
			} else {
				MyLog.w(TAG, "HTTP请求错误："
						+ response.getStatusLine().getStatusCode());
				MyLog.w(TAG, "无法获取到相关资源");
			}
		}
		MyLog.d(TAG, "the result return from post is:" + smsContent);
		return smsContent;
	}

	private static boolean isWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		String netType = info.getTypeName().toUpperCase();
		return "WIFI".equals(netType);
	}

	private static void setProxy(Context context) {
		if (isProxySetted) {
			return;
		} else {
			int type = OperatorTypeGetter.getOperatorType(context);
			if (type == OperatorTypeGetter.UNKNOW) {
			} else if (type == OperatorTypeGetter.CT) {
				proxy = CTPROXY;
			} else {
				proxy = CMPROXY;
			}
			isProxySetted = true;
		}
	}

	public static List<BasicNameValuePair> paramPairsPackage(
			Map<String, String> params) {
		List<BasicNameValuePair> paramPairs = new ArrayList<BasicNameValuePair>();
		if (params != null) {
			Set<String> keys = params.keySet();
			for (Iterator<String> i = keys.iterator(); i.hasNext();) {
				String key = (String) i.next();
				paramPairs.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		return paramPairs;
	}
}
