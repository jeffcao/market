package com.tblin.market.info;

/**
 * 要想使用数据，必须先open 在退出程序的时候要close
 * 
 */
public interface DataProvider {

	/**
	 * 获取一组应用
	 * 
	 * @param groupId
	 *            要取的应用是属于哪一组
	 * @param page
	 *            页信息
	 * @param lsnr
	 *            回调接口
	 */
	public void onAppItem(int groupId, PageInfo page, DataListener lsnr);

	/**
	 * 获取应用的详细信息
	 * 
	 * @param appid
	 *            应用的appid
	 * @param lsnr
	 *            回调接口
	 */
	public void onAppInfo(int appid, DataListener lsnr);

	/**
	 * 获取应用的Logo
	 * 
	 * @param appid
	 *            应用的appid
	 * @param lsnr
	 *            回调接口
	 */
	public void onAppLogo(int appid, DataListener lsnr);

	/**
	 * 获取应用的截图
	 * 
	 * @param appid
	 *            应用的appid
	 * @param wantNum
	 *            要哪一页
	 * @param lsnr
	 *            回调接口
	 */
	public void onAppImage(int appid, int wantNum, DataListener lsnr);

	/**
	 * 获取应用的下载地址
	 * 
	 * @param appid
	 *            应用的appid
	 * @param lsnr
	 *            回调接口
	 */
	public void onAppUrl(int appid, DataListener lsnr);

	/**
	 * 获取搜索结果
	 * 
	 * @param keywords
	 *            关键字
	 * @param pginfo
	 *            页信息
	 * @param lsnr
	 *            回调接口
	 */
	public void onSearch(String keywords, int groupId, PageInfo pginfo, DataListener lsnr);

	public void open();

	public void close();

}
