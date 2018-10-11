package com.five9.admin.digitalsignage.Common;

import android.content.Context;
import android.content.SharedPreferences;

import com.five9.admin.digitalsignage.MyApplication;

import static com.five9.admin.digitalsignage.Common.Constant.ACCESSTOKEN;

public class DataStorage {
    private final String PREFS_NAME = "my_prefs";
    private final String KEY_LIST_SCHEDULES = "KEY_LIST_SCHEDULES";
    private static DataStorage instance;
    private Context context;
    private DataStorage() {
        this.context = MyApplication.getInstance();
    }

    public static DataStorage getInstance(){
        if (instance == null)
            instance = new DataStorage();
        return instance;
    }

    private SharedPreferences.Editor getEditor(){
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
    }

    private SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveListSchedules(String s){
        SharedPreferences.Editor editor = getEditor();
        editor.putString(KEY_LIST_SCHEDULES, s);
        editor.apply();
    }

    public String getListSchedules(){
        return getSharedPreferences().getString(KEY_LIST_SCHEDULES,"");
    }

    public void saveAccessToken(String s){
        SharedPreferences.Editor editor = getEditor();
        editor.putString(ACCESSTOKEN, s);
        editor.apply();
    }

    public String getAccessToken(){
        return getSharedPreferences().getString(ACCESSTOKEN,"");
    }

	public void saveString(String key, String value) {
		if (value.toLowerCase().equalsIgnoreCase("null")) {
			value = "";
		}
		SharedPreferences.Editor editor = getEditor();
		editor.putString(key, value);
		editor.apply();
	}

	public String getString(String key, String defaulValue) {
		return getSharedPreferences().getString(key, defaulValue);
	}
}
