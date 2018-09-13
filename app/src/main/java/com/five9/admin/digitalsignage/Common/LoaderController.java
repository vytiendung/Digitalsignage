package com.five9.admin.digitalsignage.Common;

import android.util.Log;

import com.five9.admin.digitalsignage.Object.Schedule;

import java.util.ArrayList;

public class LoaderController {

    private final String TAG = "LoaderController";
    private final int MAX_TIME_LOAD_FALD = 5;
    private ArrayList<LoadRequest> listLoadRequests;
    private boolean loading = false;

    private static LoaderController instance;
    private int scheduleIdLoading = -1;

    public static LoaderController getInstance(){
        if (instance == null){
            instance = new LoaderController();
            instance.listLoadRequests = new ArrayList<>();
        }
        return instance;
    }

    private LoaderController(){}

    public void addRequest(Schedule schedule){
        if (schedule.id != scheduleIdLoading) {
            if (getRequestByPathOnDevice(schedule.id) == null) {
                LoadRequest newRequest = new LoadRequest(schedule);
                listLoadRequests.add(newRequest);
                loadVideoIfCan();
            }
        }
    }

    private LoadRequest getRequestByPathOnDevice(int scheduleId) {
        for (int i = 0; i < listLoadRequests.size(); i++){
            if (listLoadRequests.get(i).schedule.id == scheduleId)
                return listLoadRequests.get(i);
        }
        return null;
    }

    private void loadVideoIfCan(){
        Log.d(TAG, "loadVideoIfCan: " + loading + " " + listLoadRequests.size());
        if (!loading && listLoadRequests.size() > 0){
            LoadRequest request = listLoadRequests.get(0);
            downloadFile(request);
            loading = true;
            listLoadRequests.remove(request);
        }
    }

    private void downloadFile(LoadRequest request){
        scheduleIdLoading = request.schedule.id;
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.setRequest(request);
        downloadTask.execute();
    }

    public void onLoadRequestSuccess(LoadRequest request) {
        loading = false;
        loadVideoIfCan();
        ListSchedulesManager.getInstance().updateScheduleDownloadedState(request.schedule.id, true);
        Log.d(TAG, "onSuccess: " + request.schedule.id);
        scheduleIdLoading = -1;
    }

    public void onLoadRequestFail(LoadRequest request) {
        Log.d(TAG, "onFail: ");
        loading = false;
        ListSchedulesManager.getInstance().updateScheduleDownloadedState(request.schedule.id, false);
        doIfLoadFail(request);
        loadVideoIfCan();
        scheduleIdLoading = -1;
    }

    public void onLoadRequestCancel(LoadRequest request) {
        Log.d(TAG, "onCancel: ");
        loading = false;
        doIfLoadFail(request);
        loadVideoIfCan();
    }

    private void doIfLoadFail(LoadRequest request) {
        LoadRequest check = getRequestByPathOnDevice(request.schedule.id);
        Log.d(TAG, "doIfLoadFail: " + check);
        if (check == null) {
            request.loadFailTime++;
            if (request.loadFailTime < MAX_TIME_LOAD_FALD) {
                listLoadRequests.add(request);
            }
        }
    }

    public class LoadRequest {
        public Schedule schedule;
        public int loadFailTime;
        public LoadRequest(Schedule schedule){
            this.schedule = schedule;
        }
    }


}


