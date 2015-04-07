package com.tblin.market.info;

import android.content.Context;
import android.telephony.TelephonyManager;

public class OperatorTypeGetter {

	public static final int CMCC = 1;// 中国移动
	public static final int CT = 2;// 中国电信
	public static final int CU = 3;// 中国联通
	public static final int UNKNOW = 4;

	public static int getOperatorType(Context context) {
		TelephonyManager tel = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return getByImsi(tel) == UNKNOW ? getByOperator(tel) : getByImsi(tel);
	}

	private static int getByImsi(TelephonyManager tel) {
		String imsi = tel.getSubscriberId();
		int type = UNKNOW;
		if (imsi != null) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				type = CMCC;
			} else if (imsi.startsWith("46001")) {
				type = CU;
			} else if (imsi.startsWith("46003")) {
				type = CT;
			}
		}
		return type;
	}

	private static int getByOperator(TelephonyManager tel) {
		String operator = tel.getSimOperator();
		int type = UNKNOW;
		if (operator != null) {
			if (operator.equals("46000") || operator.equals("46002")) {
				type = CMCC;
			} else if (operator.equals("46001")) {
				type = CU;
			} else if (operator.equals("46003")) {
				type = CT;
			}
		}
		return type;
	}
}
