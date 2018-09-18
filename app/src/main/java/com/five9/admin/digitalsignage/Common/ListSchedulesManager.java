package com.five9.admin.digitalsignage.Common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.five9.admin.digitalsignage.MyApplication;
import com.five9.admin.digitalsignage.Object.ListSchedules;
import com.five9.admin.digitalsignage.Object.ParseJson;
import com.five9.admin.digitalsignage.Object.Schedule;

import java.io.File;
import java.util.ArrayList;

public class ListSchedulesManager {
    private static final String TAG = "ListSchedulesManager";
    private static final int MAX_TIME_RELOAD = 5;
    private static ListSchedulesManager instance;
    private Context context;
    public ListSchedules currentSchedules;
    private ListSchedules nextSchedules;
    private LoaderController loaderController;

    private ParseJson parseJson;
    private ApiConnection apiConnection;
    private DataStorage dataStorage;
    private int currentIndex = -1;
    private long TIME_TO_CHECK_RELOAD = 1000 * 60;
    private String currentPath = "";
    private int timeReload = 0;

    private ListSchedulesManager() {
        this.context = MyApplication.getInstance();
        parseJson = new ParseJson();
        apiConnection = new ApiConnection();
        dataStorage = DataStorage.getInstance();
        loaderController = LoaderController.getInstance();
    }

    public static ListSchedulesManager getInstance(){
        if (instance == null)
            instance = new ListSchedulesManager();
        return instance;
    }

    public void initData(){
//        String localData = dataStorage.getListSchedules();
//        currentSchedules = parseJson.parseListSchedules(localData);
//        currentSchedules.schedules = null;
//        Log.d(TAG, "initData: " + currentSchedules);
//        if (!TextUtils.isEmpty(currentSchedules.version)){
//            apiConnection.sendSchedulesVersionToServer(currentSchedules.version, new RequestNetworkListener() {
//                @Override
//                public void onSuccess(String response) {
//                    // todo
//                    getListSchedulesFromServer();
//                }
//
//                @Override
//                public void onFail(String response) {
//
//                }
//
//                @Override
//                public void onCancel(String response) {
//
//                }
//            });
//        } else {
//            Log.d(TAG, "initData: 2");
//            getListSchedulesFromServer();
//        }
        currentSchedules = ListSchedules.getDataFromJson(DataStorage.getInstance().getListSchedules());
        loadSchedulesIfNeed(currentSchedules);
        getListSchedulesFromServer();
    }

    private void startCheckReload() {
        Handler checkReloadVideoIfNeed = new Handler(Looper.getMainLooper());
        checkReloadVideoIfNeed.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (currentSchedules != null){
                       loadSchedulesIfNeed(currentSchedules);
                    }
                    if (nextSchedules != null)
                        loadSchedulesIfNeed(nextSchedules);
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
                nextSchedules = ListSchedules.getDataFromJson(response);
                if (currentSchedules == null) {
                    Log.d(TAG, "getListSchedulesFromServer1: ");
                    currentSchedules = nextSchedules;
                    nextSchedules = null;
                    if (!TextUtils.isEmpty(currentSchedules.devid)){
                        dataStorage.saveListSchedules(currentSchedules.toJsonString());
                        loadSchedulesIfNeed(currentSchedules);
                    }
                } else {
                    if (!currentSchedules.version.equals(nextSchedules.version)) {
                        Log.d(TAG, "getListSchedulesFromServer2: ");
                        if (!TextUtils.isEmpty(nextSchedules.devid)){
                            Log.d(TAG, "getListSchedulesFromServer3: ");
                            for (int i = 0; i < currentSchedules.schedules.size(); i++){
                                Schedule current = currentSchedules.schedules.get(i);
                                for (int j = 0; j < nextSchedules.schedules.size(); j++){
                                    Schedule next = nextSchedules.schedules.get(j);
                                    if (current.id == next.id){
                                        current.endtime = next.endtime;
                                        current.starttime = next.starttime;
                                        if (current.isFetched())
                                            next.downloaded = true;
                                    }
                                }
                            }
                            dataStorage.saveListSchedules(nextSchedules.toJsonString());
                            loadSchedulesIfNeed(nextSchedules);
                            startThreadCheckUpdate();
                        }
                    }
                }

                startCheckReload();
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
                if (schedule.isFetched()){
                    currentSchedules = nextSchedules;
                    DataStorage.getInstance().saveListSchedules(currentSchedules.toJsonString());
                    nextSchedules = null;
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
            ArrayList<String> removeFiles = new ArrayList<>();
            File dir = new File(Ulti.getRootFolder());
            Log.d(TAG, "cleanNotUseFile: " + dir.list().length);
            for (int i = 0; i < dir.list().length; i ++){
                String path = Ulti.getRootFolder() + dir.list()[i];
                Log.d(TAG, "cleanNotUseFile: " + path);
                boolean onCurrentSchedule = false;
                for (int j = 0; j < currentSchedules.schedules.size(); j++){
                    Schedule schedule = currentSchedules.schedules.get(j);
                    if (schedule.getPathOnDevice().equals(path)){
                        onCurrentSchedule = true;
                        break;
                    }
                }
                if (!onCurrentSchedule && !currentPath.equals(path)){
                    removeFiles.add(path);
                }
            }
            Log.d(TAG, "cleanNotUseFile: ccc" + removeFiles.size());
            for (int i = 0; i < removeFiles.size(); i++){
                File f = new File(removeFiles.get(i));
                f.delete();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadSchedulesIfNeed(ListSchedules listSchedules){
        for (int i = 0; i < listSchedules.schedules.size(); i++){
            if (!listSchedules.schedules.get(i).isFetched()) {
                loaderController.addRequest(listSchedules.schedules.get(i));
            }
        }
    }

    public Schedule getNextSchedule(){
        try {
	        int lastIndex = currentIndex;
	        while (true){
		        currentIndex ++;
		        if (currentIndex >= currentSchedules.schedules.size())
			        currentIndex = 0;
		        Schedule schedule = currentSchedules.schedules.get(currentIndex);
		        if (schedule.canPlay()){
			        currentPath = schedule.getPathOnDevice();
			        return schedule;
		        }
		        else {
			        loaderController.addRequest(schedule);
		        }
		        if (currentIndex == lastIndex)
			        break;
		        if (lastIndex == -1)
			        lastIndex = 0;
	        }
        }catch (Exception e){
        	e.printStackTrace();
        }
        return null;
    }

    public void onPlayErrFile(String path) {
        File file = new File(path);
        if (file.exists())
            file.delete();
        Schedule schedule = getScheduleByPathOnDevice(path);
        schedule.downloaded = false;
        if (schedule != null)
            LoaderController.getInstance().addRequest(schedule);

    }

    private Schedule getScheduleByPathOnDevice(String path){
        for (int i = 0; i < currentSchedules.schedules.size(); i++){
            if (currentSchedules.schedules.get(i).getPathOnDevice().equals(path))
                return currentSchedules.schedules.get(i);
        }
        return null;
    }

    public boolean canPlay(){
        if (currentSchedules == null || currentSchedules.schedules == null)
            return  false;
        for (int i = 0; i < currentSchedules.schedules.size(); i++){
            Schedule schedule = currentSchedules.schedules.get(i);
            if (schedule.canPlay())
            {
                return true;
            }
        }
        return false;
    }

    public void updateScheduleDownloadedState(int id, boolean downloaded){
        try {
            if (currentSchedules != null) {
                for (int i = 0; i < currentSchedules.schedules.size(); i++) {
                    if (currentSchedules.schedules.get(i).id == id)
                        currentSchedules.schedules.get(i).downloaded = downloaded;
                }
                DataStorage.getInstance().saveListSchedules(currentSchedules.toJsonString());
            }

            if (nextSchedules != null) {
                for (int i = 0; i < nextSchedules.schedules.size(); i++) {
                    if (nextSchedules.schedules.get(i).id == id)
                        nextSchedules.schedules.get(i).downloaded = downloaded;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
