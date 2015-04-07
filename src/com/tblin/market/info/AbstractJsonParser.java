package com.tblin.market.info;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonParser implements JsonParser {

	public final void parse(String jsonStr, DataListener lsnr) {
		try {
			JSONObject obj = new JSONObject(jsonStr);
			parse(obj, lsnr);
		} catch (JSONException e) {
			jsonErrHandle(e, lsnr);
		}
	}

	/**
	 * 在解析的时候出现json异常可以调用这个函数，默认这个函数什么也不做 如果要处理异常，重写这个方法
	 */
	protected void jsonErrHandle(JSONException e, DataListener lsnr) {

	}

}
