package com.five9.admin.digitalsignage.Object;

import com.five9.admin.digitalsignage.Common.Ulti;
import com.google.gson.Gson;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Schedule {

    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_IMAGE = "image";
    private static final String TAG = "Schedule";


    public String path = "";
    public int id;
    public String type = "";
    public String starttime;
    public String endtime;
    public boolean downloaded = false;

    public static Schedule getDataFromJson(String json) {
        Gson gson = new Gson();
        Schedule obj;
        try {
            obj =  gson.fromJson(json, Schedule.class);
            if (obj.path.indexOf('/') == 0)
                obj.path = obj.path.substring(1);
        } catch (Exception e) {
            obj = new Schedule();
            e.printStackTrace();
        }
        return obj;
    }

    public String getPathOnDevice(){
        String pathOnDevice = Ulti.getRootFolder()  + id;
        try {
            String name = path.substring(path.lastIndexOf("/") + 1);
            pathOnDevice += "_" + name;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return pathOnDevice;
    }

    public long getStartTimeLong(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return dateFormat.parse(starttime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getEndTimeLong(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return dateFormat.parse(endtime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isFetched(){
        File f = new File(getPathOnDevice());
        if (!f.exists())
            downloaded = false;
        return downloaded;
    }

    public boolean canPlay(){
        long currentTime = System.currentTimeMillis();
        return isFetched() && currentTime >= getStartTimeLong() && currentTime <= getEndTimeLong();
    }

}
