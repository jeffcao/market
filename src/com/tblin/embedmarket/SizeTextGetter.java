package com.tblin.embedmarket;

import java.text.DecimalFormat;

public class SizeTextGetter {
	
	public static String getSize(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("size must big than 0");
		}
		String result = "";
		if (size < 1024 * 1024) {
			float f = (float) size / (float)(1024);
			result = round(f) + "KB";
		} else {
			float f = (float) size / (float)(1024 * 1024);
			result = round(f) + "MB";
		}
		return result;
	}
	
	private static float round(float f) {
		float value = f;
		return Float.valueOf(new DecimalFormat("#.0").format(value));
	}
	
}
