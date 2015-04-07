package com.tblin.market.breakdown;

public interface DownloadListener {

	public void onLoading(String url, int length);

	public void onFinish(String url);

	public void onStart(int fileSize, int compeleteSize, String url);

	public void onPause(String url);

	public void onReset(String url);

	public void onWaiting(String url);

	public void onCancel(String url);
	
	public void onNetworkError(String url);

}
