package com.tblin.market.info;

import java.util.Map;

import android.content.Context;
import android.os.Handler;

public class IDataListener implements DataListener {

	private Handler hdlr;
	private DataListener lsnr;

	public IDataListener(DataListener lsnr, Context context) {
		hdlr = new Handler(context.getMainLooper());
		this.lsnr = lsnr;
	}

	@Override
	public void onData(Map<String, Object> data) {
		final Map<String, Object> finalData = data;
		hdlr.post(new Runnable() {

			@Override
			public void run() {
				lsnr.onData(finalData);
			}
		});
	}

}
