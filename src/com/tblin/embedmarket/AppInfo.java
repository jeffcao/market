package com.tblin.embedmarket;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

public class AppInfo {

	private String introduce;
	private List<Drawable> images;
	private int totalImage;
	private int id;
	private String zifei;
	private String type;

	public AppInfo() {
		images = new ArrayList<Drawable>();
	}

	public String getZifei() {
		return zifei;
	}

	public void setZifei(String zifei) {
		this.zifei = zifei;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	public List<Drawable> getImages() {
		return images;
	}

	public void addImage(Drawable image) {
		images.add(image);
	}

	public int getTotalImage() {
		return totalImage;
	}

	public void setTotalImage(int totalImage) {
		this.totalImage = totalImage;
	}

}
