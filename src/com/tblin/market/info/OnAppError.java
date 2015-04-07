package com.tblin.market.info;

import java.util.Map;

public class OnAppError {

	private int code;
	private String information;
	private Map<String, Object> data;

	public OnAppError(int code, String information) {
		this.code = code;
		this.information = information;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return code + ", " + information;
	}

}
