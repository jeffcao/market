package com.tblin.market.ui;

import android.os.Handler;

import com.tblin.embedmarket.MyLog;
import com.tblin.market.info.PageInfo;

/**
 * 需要有2个ListStatus,一个是推荐列表的，一个是搜索列表的。
 * 推荐列表的ListStatus变量是不变的，而搜索列表的ListStatus变量，每次更换关键字搜索的时候要重新new
 * 取数据之前先看status是不是处于ready状态，出于ready状态才能去取，取到数据后先对比返回的PageInfo
 * 和ListStatus的PageInfo，若是认定返回的PageInfo已经过时，则不把返回的这一批AppItem插入队列
 * 取到数据之后，记得把status状态变更为ready状态，若取数据返回数据已取完，把状态改变为close状态
 */
public class ListStatus {
	private static final long TIME_OUT = 5000;
	private Handler hdlr;
	private int status;
	private PageInfo page;
	public static final int STATUS_CLOSE = 1;
	public static final int STATUS_READY_FOR_MORE = 2;
	public static final int STATUS_WAITING_FOR_RESULT = 3;
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final String TAG = ListStatus.class.toString();

	public ListStatus() {
		hdlr = new Handler();
		status = STATUS_READY_FOR_MORE;
		page = new PageInfo();
		page.pageSize = DEFAULT_PAGE_SIZE;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		if (this.status != status && this.status != STATUS_CLOSE) {
			this.status = status;
			hdlr.removeCallbacksAndMessages(hdlr.obtainMessage().obj);
			if (status == STATUS_WAITING_FOR_RESULT) {
				hdlr.postDelayed(new ChangeStatus2ReadyRunnable(), TIME_OUT);
			}
		}
	}

	public PageInfo getPage() {
		return page;
	}

	public void setPage(PageInfo page) {
		this.page = page;
	}

	private class ChangeStatus2ReadyRunnable implements Runnable {

		@Override
		public void run() {
			setStatus(STATUS_READY_FOR_MORE);
			MyLog.i(TAG, "on app data time out, call again!");
		}
	}
}
