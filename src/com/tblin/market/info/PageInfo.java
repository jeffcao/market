package com.tblin.market.info;

/**
 * 页信息，用来控制分页获取数据
 * 
 */

public class PageInfo {

	public int pageSize;
	public int totalPage;
	public int totalRecord;
	public int currentPage;
	public int currentRecord;

	public PageInfo() {
		pageSize = 0;
		totalPage = Integer.MAX_VALUE;
		totalRecord = Integer.MAX_VALUE;
		currentPage = 0;
		currentRecord = 0;
	}

	public boolean newThan(PageInfo pg) {
		if (pg == null) {
			return true;
		}
		return currentPage > pg.currentPage;
	}

	@Override
	public String toString() {
		String str = "page size: " + pageSize + "; total page: " + totalPage
				+ "; total record: " + totalRecord + "current page: "
				+ currentPage + "; + current record: " + currentRecord;
		return str;
	}

}
