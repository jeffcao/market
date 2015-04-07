package com.tblin.market.info;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerMessageParser extends AbstractJsonParser {

	private AbstractJsonParser parser;

	public ServerMessageParser(AbstractJsonParser okParser) {
		this.parser = okParser;
	}

	@Override
	public void parse(JSONObject jsonObject, DataListener lsnr) {
		try {
			int type = jsonObject.getInt("type");
			JSONObject result = jsonObject.getJSONObject("result");
			if (type == 1) {
				parser.parse(result.toString(), lsnr);
			} else {
				int code = result.getInt("type");
				String msg = result.getString("msg");
				OnAppError err = new OnAppError(code, msg);
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("error", err);
				lsnr.onData(data);
			}
		} catch (JSONException e) {
			jsonErrHandle(e, lsnr);
		}
	}

	@Override
	protected void jsonErrHandle(JSONException e, DataListener lsnr) {
		OnAppError err = new OnAppError(104,
				"json format error: type or result can not get");
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("error", err);
		lsnr.onData(data);
	}

}
