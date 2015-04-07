package com.tblin.market.breakdown;

import android.os.Handler;

import com.tblin.embedmarket.AppItem;

/**
 * 调用down或者continuedown之后，item处于排队状态，等到 start广播，item才处于下载状态
 * 处于排序状态的item调用cancel之后，会从队列中移除，处于未下载状态
 * 
 */
public interface LoadSequenceManager {

	void down(AppItem ai, DownloadListener lsnr, Handler hdlr);

	void cancel(int appid);

	void pause(int appid);

	void continueDown(int appid);

	boolean isInQueue(int appid);

	void pauseAll();

	boolean hasTaskGoing();

	void onNetWorkagain();

	void tempPauseAll();

}
