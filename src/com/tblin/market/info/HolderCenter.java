package com.tblin.market.info;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 这个类用来处理服务器回传过来的消息，是一个单例模式的类 服务器回传过来的json会有一个holder字段指明应该处理此消息
 * 的holder，所有的holder都会在这里注册，当有消息回来时，根据 holderid找到相应的holder处理消息。
 * 
 */
public class HolderCenter {

	private HashMap<Integer, Holder> holders;
	private static HolderCenter INSTANCE;

	private HolderCenter() {
		holders = new HashMap<Integer, Holder>();
	}

	public static HolderCenter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new HolderCenter();
		}
		return INSTANCE;
	}

	public void registHolder(Holder holder) {
		holders.put(holder.getId(), holder);
	}

	public void parse(JSONObject jsonObject) {
		int holderId = -1;
		Holder holder = null;
		try {
			holderId = jsonObject.getInt("holder");
			holder = holders.get(new Integer(holderId));
			if (holder != null) {
				JSONObject data = jsonObject.getJSONObject("data");
				holder.parse(data);
			} else {
				// 若是找不到holder，直接丢弃消息
			}
		} catch (JSONException e) {
			if (holder != null) {
				OnAppError err = new OnAppError(104,
						"json format error: holder or data can not get");
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("error", err);
				DataListener lsnr = holder.getDataListener();
				if (lsnr != null) {
					lsnr.onData(data);
				} else {
					// 找不到数据监听器，直接丢弃消息
				}
			} else {
				// 若是找不到holder，直接丢弃消息
			}
		}
	}

}
