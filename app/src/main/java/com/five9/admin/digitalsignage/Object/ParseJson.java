package com.five9.admin.digitalsignage.Object;

import com.five9.admin.digitalsignage.Common.Config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ParseJson {

    private static final String TAG = "ParseJson";
    private final String KEY_VERSION = "version";
    private final String KEY_DEV_ID = "devid";
    private final String KEY_SCHEDULES = "schedules";

    private final String KEY_PATH = "path";
    private final String KEY_TYPE = "type";
    private final String KEY_START_TIME = "starttime";
    private final String KEY_END_TIME = "endtime";
    private final String KEY_ID = "id";
    public Schedule parseSchedule(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            return parseSchedule(jsonObject);

        } catch (Exception e){
            e.printStackTrace();
        }
        return  new Schedule();
    }

    public Schedule parseSchedule(JSONObject jsonObject){
        Schedule schedule = new Schedule();
        try {
            schedule.path = Config.getServerEndpoint() + getString(jsonObject, KEY_PATH);
            schedule.type = getString(jsonObject, KEY_TYPE);
            String startTimeString = getString(jsonObject, KEY_START_TIME);
            String endTimeString = getString(jsonObject, KEY_END_TIME);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//            schedule.starttime = dateFormat.parse(startTimeString).getTime();
//            schedule.endtime = dateFormat.parse(endTimeString).getTime();
//            Log.d(TAG, "parseSchedule: " + dateFormat.parse(endTimeString).toString());
//            Log.d(TAG, "parseSchedule: " + startTimeString + "===========" + endTimeString);
//            schedule.id = getString(jsonObject, KEY_ID);
//            schedule.genPathOnDevice();
        } catch (Exception e){
            e.printStackTrace();
        }
        return  schedule;
    }

    public ListSchedules parseListSchedules(String json){
        ListSchedules listSchedules = new ListSchedules();
        try {
            JSONObject jsonObject = new JSONObject(json);
            listSchedules.version = getString(jsonObject, KEY_VERSION);
            listSchedules.devid = getString(jsonObject, KEY_DEV_ID);
            listSchedules.schedules = new ArrayList();
            JSONArray jsonArray = jsonObject.getJSONArray(KEY_SCHEDULES);
            for(int i = 0; i < jsonArray.length(); i++){
                jsonObject = jsonArray.getJSONObject(i);
                Schedule schedule = parseSchedule(jsonObject);
                listSchedules.schedules.add(schedule);
            }
        } catch (Exception ex){
            listSchedules = new ListSchedules();
            ex.printStackTrace();
        }
        return listSchedules;
    }

    private String getString(JSONObject json, String key){
        try {
            return json.getString(key);
        } catch (Exception ex){
            return "";
        }
    }
}
