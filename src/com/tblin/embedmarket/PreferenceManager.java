package com.tblin.embedmarket;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

	private SharedPreferences sp;
	private static final String PREFRENCE_NAME = "embed_market";

	public PreferenceManager(Context context) {
		sp = context
				.getSharedPreferences(PREFRENCE_NAME, Activity.MODE_PRIVATE);
	}

	public boolean isFirstOpenApp() {
		return sp.getBoolean("is_first", true);
	}

	public void setIsFirstOpenApp(boolean value) {
		sp.edit().putBoolean("is_first", value).commit();
	}

}
