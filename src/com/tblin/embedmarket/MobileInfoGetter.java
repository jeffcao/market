package com.tblin.embedmarket;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class MobileInfoGetter {

	private Context mContext;
	private static final String NET_UNKNOW = "UNKNOW";
	public static final String TYPE_OUT_LINK_START = "download_1";
	public static final String TYPE_OUT_LINK_END = "downloaded_1";
	public static final String TYPE_INNER_LINK_START = "download_2";
	public static final String TYPE_INNER_LINK_END = "downloaded_2";
	public static final String TYPE_CANCEL_DOWN = "cancel_down";
	public static final String TYPE_INSTALL_DOWN = "install_down";
	public static final String TYPE_DELETE_DOWN = "delete_down";
	public static final String TYPE_OUT_LINK_BAD = "out_link_bad";
	public static final String TYPE_IN_LINK_BAD = "in_link_bad";

	public MobileInfoGetter(Context context) {
		mContext = context; 
	}

	/**
	 * permissions need: all the permission below
	 * 
	 * @return
	 */
	public Map<String, String> getAllImmediateInfo(String appId,
			String selfVersion, String downAppId, String downAppVersion,
			String type) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("imsi", getImsi());
		params.put("imei", getImei());
		params.put("mac", getMac());
		params.put("ua", getMobileModel());
		params.put("app_id", appId);
		params.put("f_version", selfVersion);
		params.put("d_softid", downAppId);
		params.put("d_version", downAppVersion);
		MyLog.i("MobileInfoGetter", "d_version:" + downAppVersion);
		params.put("type", type);
		return params;
	}

	/**
	 * need permission: android.permission.ACCESS_WIFI_STATE
	 * 
	 * @return
	 */
	public String getMac() {
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return
	 */
	public String getMobileModel() {
		return Build.MODEL;
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return
	 */
	public String getMobileBrand() {
		return Build.BRAND;
	}

	/**
	 * if the context is an activity, the resolution can be getted else can't
	 * the format of resolution is width*height
	 * 
	 * @return
	 */
	public String getMobileResolution() {
		DisplayMetrics dm = new DisplayMetrics();
		if (mContext instanceof Activity) {
			((Activity) mContext).getWindowManager().getDefaultDisplay()
					.getMetrics(dm);
		}
		int height = dm.heightPixels;
		int width = dm.widthPixels;
		String resolution = null;
		if (height != 0 && width != 0) {
			resolution = width + "*" + height;
		}
		return resolution;
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return return null when there is no sim card
	 */
	public String getImsi() {
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return
	 */
	public String getImei() {
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public String getNettype() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info != null) {
			String type = info.getTypeName();
			if (type != null && !"".equals(type)) {
				return type;
			} else {
				return NET_UNKNOW;
			}
		} else {
			return NET_UNKNOW;
		}
	}

}
