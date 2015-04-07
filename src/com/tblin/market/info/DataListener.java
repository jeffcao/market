package com.tblin.market.info;

import java.util.Map;

/**
 * 获取应用列表：page_info:PageInfo, app_item:List<AppItem> 错误：error:Error
 * 获取应用详细信息：app_info:AppInfo 获取应用logo:id:int, logo:Drawable 获取应用截图:id:int,
 * image:Drawable, current_num:int 获取应用url:id:int, url_normal:String,
 * url_ownserver:String
 */

public interface DataListener {

	/**
	 * 这里要约定好每个对象的关键字，服务端将参数放进去，客户端从里面 取出来用
	 * 
	 * @param data
	 *            存有返回的数据，用关键字去取这些数据
	 */
	public void onData(Map<String, Object> data);

}
