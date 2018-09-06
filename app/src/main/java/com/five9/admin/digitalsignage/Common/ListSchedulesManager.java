package com.five9.admin.digitalsignage.Common;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.five9.admin.digitalsignage.Object.ListSchedules;
import com.five9.admin.digitalsignage.Object.ParseJson;
import com.five9.admin.digitalsignage.Object.Schedule;

import java.io.File;

public class ListSchedulesManager {
    private static final String TAG = "ListSchedulesManager";
    private static final int MAX_TIME_RELOAD = 5;
    private static ListSchedulesManager instance;
    private Context context;
    public ListSchedules currentSchedules;
    private LoaderController loaderController;

    private ParseJson parseJson;
    private ApiConnection apiConnection;
    private DataStore dataStore;
    private int currentIndex = -1;
    private long TIME_TO_CHECK_RELOAD = 1000 * 60;
    private ListSchedules nextSchedules;
    private String currentPath;
    private int timeReload = 0;

    private ListSchedulesManager(Context context) {
        this.context = context;
        parseJson = new ParseJson();
        apiConnection = new ApiConnection();
        dataStore = DataStore.getInstance(context);
        loaderController = LoaderController.getInstance();
    }

    public static ListSchedulesManager getInstance(Context context){
        if (instance == null)
            instance = new ListSchedulesManager(context);
        return instance;
    }

    public void initData(){
        String localData = dataStore.getListSchedules();
        currentSchedules = parseJson.parseListSchedules(localData);
        currentSchedules.schedules = null;
        Log.d(TAG, "initData: " + currentSchedules);
        if (!TextUtils.isEmpty(currentSchedules.version)){
            apiConnection.sendSchedulesVersionToServer(currentSchedules.version, new RequestNetworkListener() {
                @Override
                public void onSuccess(String response) {
                    // todo
                    getListSchedulesFromServer();
                }

                @Override
                public void onFail(String response) {

                }

                @Override
                public void onCancel(String response) {

                }
            });
        } else {
            Log.d(TAG, "initData: 2");
            getListSchedulesFromServer();
        }
        startCheckReload();
    }

    private void startCheckReload() {
        Handler checkReloadVideoIfNeed = new Handler();
        checkReloadVideoIfNeed.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (currentSchedules != null){
                        for (int i = 0; i < currentSchedules.schedules.size(); i++){
                            File f = new File(currentSchedules.schedules.get(i).pathOnDevice);
                            if (!f.exists())
                                loaderController.addRequest(currentSchedules.schedules.get(i).pathOnServer,
                                        currentSchedules.schedules.get(i).pathOnDevice);
                        }
                    }
                } catch (Exception ex){}
                startCheckReload();
            }
        }, TIME_TO_CHECK_RELOAD);
    }

    private void getListSchedulesFromServer(){
        Log.d(TAG, "getListSchedulesFromServer: ");
        apiConnection.getListSchedules(new RequestNetworkListener() {
            @Override
            public void onSuccess(String response) {
                //check validate data
                timeReload = 0;
                nextSchedules = parseJson.parseListSchedules(response);
                if (nextSchedules != null && nextSchedules.schedules != null){
                    dataStore.saveListSchedules(response);
                    loadSchedules(nextSchedules);
                    startThreadCheckUpdate();
                }

            }

            @Override
            public void onFail(String response) {
                timeReload++;
                if(timeReload < MAX_TIME_RELOAD)
                    getListSchedulesFromServer();
            }

            @Override
            public void onCancel(String response) {

            }
        });
    }

    private void startThreadCheckUpdate() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        if (checkUpdateCurrentScheduleByNextSchedule()){
                            break;
                        }
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    private boolean checkUpdateCurrentScheduleByNextSchedule(){
        Log.d(TAG, "checkUpdateCurrentScheduleByNextSchedule: ");
        try {
            for (int i = 0; i < nextSchedules.schedules.size(); i++){
                Schedule schedule = nextSchedules.schedules.get(i);
                Log.d(TAG, "checkUpdateCurrentScheduleByNextSchedule: " + nextSchedules.schedules.get(i).pathOnDevice);
                File f = new File(schedule.pathOnDevice);
                if (f.exists()){
                    if (currentSchedules.version == null || !currentSchedules.version.equals(nextSchedules.version)) {
                        currentIndex = -1;
                    }
                    currentSchedules = nextSchedules;
                    cleanNotUseFile();
                    return true;
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    private void cleanNotUseFile(){
        Log.d(TAG, "cleanNotUseFile: ");
        try {
            File dir = new File(Ulti.getRootFolder());
            for (int i = 0; i < dir.list().length; i ++){
                String path = Ulti.getRootFolder() + dir.list()[i];
                boolean onCurrentSchedule = false;
                for (int j = 0; j < currentSchedules.schedules.size(); j++){
                    Schedule schedule = currentSchedules.schedules.get(j);
                    if (schedule.pathOnDevice.equals(path)){
                        onCurrentSchedule = true;
                        break;
                    }
                }
                if (!onCurrentSchedule && !currentPath.equals(path)){
                    File f = new File(path);
                    f.delete();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadSchedules(ListSchedules listSchedules){
        for (int i = 0; i < listSchedules.schedules.size(); i++){
            Schedule schedule = listSchedules.schedules.get(i);
            Log.d(TAG, "loadSchedules: " + schedule.pathOnServer);
            loaderController.addRequest(schedule.pathOnServer, schedule.pathOnDevice);
        }
    }

    public Schedule getNextSchedule(){
        int lastIndex = currentIndex;
        while (true){
            currentIndex ++;
            if (currentIndex >= currentSchedules.schedules.size())
                currentIndex = 0;
            Schedule schedule = currentSchedules.schedules.get(currentIndex);
            File file = new File(schedule.pathOnDevice);
            if (file.exists()){
                long currentTime = System.currentTimeMillis();
                if (currentTime >= schedule.startTime && currentTime <= schedule.endTime) {
                    currentPath = schedule.pathOnDevice;
                    return schedule;
                }
                else{
                }
            }
            else {
                loaderController.addRequest(schedule.pathOnServer, schedule.pathOnDevice);
            }
            if (currentIndex == lastIndex)
                break;
            if (lastIndex == -1)
                lastIndex = 0;
        }
        return null;
    }

    public void onPlayErrFile(String path) {
        File file = new File(path);
        if (file.exists())
            file.delete();
        Schedule schedule = getScheduleByPathOnDevice(path);
        if (schedule != null)
            LoaderController.getInstance().addRequest(schedule.pathOnServer, schedule.pathOnDevice);

    }

    private Schedule getScheduleByPathOnDevice(String path){
        for (int i = 0; i < currentSchedules.schedules.size(); i++){
            if (currentSchedules.schedules.get(i).pathOnDevice.equals(path))
                return currentSchedules.schedules.get(i);
        }
        return null;
    }

    public boolean canPlay(){
        if (currentSchedules == null || currentSchedules.schedules == null)
            return  false;
        for (int i = 0; i < currentSchedules.schedules.size(); i++){
            Schedule schedule = currentSchedules.schedules.get(i);
            File f = new File(schedule.pathOnDevice);
            if (f.exists() && f.canRead())
            {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= schedule.startTime && currentTime <= schedule.endTime) {
                    Log.d(TAG, "canPlay: " + schedule.pathOnDevice);
                    return true;
                }
            }
        }
        return false;
    }
}
