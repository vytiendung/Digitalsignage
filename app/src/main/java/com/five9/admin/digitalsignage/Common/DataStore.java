package com.five9.admin.digitalsignage.Common;

import android.content.Context;
import android.content.SharedPreferences;

public class DataStore {
    private final String PREFS_NAME = "my_prefs";
    private final String KEY_LIST_SCHEDULES = "KEY_LIST_SCHEDULES";
    private static DataStore instance;
    private Context context;
    private DataStore(Context context) {
        this.context = context;
    }

    public static DataStore getInstance(Context context){
        if (instance == null)
            instance = new DataStore(context);
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
}
