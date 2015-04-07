package com.tblin.embedmarket;


public class AppItem {

	private int id;
	private String name;
	private String company;
	private int size;
	private String versionName;
	private int versionCode;
	private String pacakgeName;
	private String urlNomal;
	private String urlServer;
	private int complete;
	private int status;
	private boolean isOutUrlWork = true;
	public static final int STATUS_INITIAL = 0;// 初始状态
	public static final int STATUS_LOADING = 1;// 正在下载
	public static final int STATUS_PAUSED = 2;// 下载已暂停
	public static final int STATUS_LOADED = 3;// 下载完成
	public static final int STATUS_INSTALLED = 5;// 已安装
	public static final int STATUS_WAITING = 6;// 等待下载

	public int getComplete() {
		return complete;
	}

	public void setComplete(int complete) {
		this.complete = complete;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getWorkUrl() {
		if (isOutUrlWork) {
			return urlNomal;
		}
		return urlServer;
	}

	public boolean isUrlEquals(String url) {
		if (url == null) {
			return false;
		}
		boolean result = false;
		if (urlNomal != null) {
			result = urlNomal.equals(url);
		}
		if (urlServer != null) {
			result = result || urlServer.equals(url);
		}
		return result;
	}

	public boolean hasUrl() {
		return urlNomal != null || urlServer != null;
	}

	public void setIsOutUrlWork(boolean value) {
		isOutUrlWork = value;
	}

	public String getUrl1() {
		return urlNomal;
	}

	public String getUrl2() {
		return urlServer;
	}

	public boolean isOutUrlWork() {
		return isOutUrlWork;
	}

	public void setUrl1(String url) {
		urlNomal = url;
	}

	public void setUrl2(String url) {
		urlServer = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String desp) {
		this.company = desp;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getPacakgeName() {
		return pacakgeName;
	}

	public void setPacakgeName(String pacakgeName) {
		this.pacakgeName = pacakgeName;
	}

	@Override
	public String toString() {
		String str = "app name: " + name + "; + app id: " + id
				+ "; app company: " + company + "app package: " + pacakgeName
				+ "app version name: " + versionName + "app version code: "
				+ versionCode;
		return str;
	}

}
