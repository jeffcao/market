package com.tblin.embedmarket;

public class AppMarketConfig {

	/**
	 * 应用状态更改广播Action,状态更改广播携带data status--int--新状态 appid--int--appid
	 */
	public static final String APP_STS_CHANGE_BROADCAST = "com.tblin.embedmarket.appstschange";

	/**
	 * 暂停任务保留时间
	 */
	public static long DOWNLOAD_SAVE_TIME = 3 * 24 * 60 * 60 * 1000;

	/**
	 * 软件的APPID
	 */
	public static String CONTAINER_APP_ID = "embmarket-1001";

	/**
	 * 软件的version
	 */
	public static String CONTAINER_APP_VERSION = "3.2";

	/**
	 * 界面是否依然存在
	 */
	public static boolean IS_ACTIVITY_ALIVE = false;

	/**
	 * 后台url地址
	 */
	public static String PHP_SERVER_URL = "http://www.tblin.com/market/index.php/";
	
	public static int APP_GROUP_ID = 1;

}
