package com.tblin.market.info;

import org.json.JSONObject;

public interface JsonParser {

	public void parse(JSONObject jsonObject, DataListener lsnr);

}
