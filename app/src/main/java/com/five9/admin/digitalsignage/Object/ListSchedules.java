package com.five9.admin.digitalsignage.Object;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListSchedules {
    public static final String VERSION = "version";
    public static final String DEVID = "devid";
    public static final String SCHEDULES = "schedules";
    public String version = "";
    public String devid = "";
    public ArrayList <Schedule> schedules = new ArrayList<>();

    public static ListSchedules getDataFromJson(String json) {
        ListSchedules obj = new ListSchedules();
        try {
            JSONObject jsonObject = new JSONObject(json);
            obj.version = jsonObject.getString(VERSION);
            obj.devid = jsonObject.getString(DEVID);
            JSONArray jsonArray = jsonObject.getJSONArray(SCHEDULES);
            for (int i = 0; i < jsonArray.length(); i++){
                obj.schedules.add(Schedule.getDataFromJson(jsonArray.getString(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public String toJsonString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
