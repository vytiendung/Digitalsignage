package com.five9.admin.digitalsignage.Object;

import android.util.Log;

import com.five9.admin.digitalsignage.Common.Ulti;

public class Schedule {

    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_IMAGE = "image";
    private static final String TAG = "Schedule";


    public String pathOnServer;
    public String pathOnDevice;
    public String id;
    public String type;
    public long startTime;
    public long endTime;

    public void genNameById(){
        pathOnDevice = Ulti.getRootFolder()  + id;
        try {
            String name = pathOnServer.substring(pathOnServer.lastIndexOf("/") + 1);
            pathOnDevice += "_" + name;
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
