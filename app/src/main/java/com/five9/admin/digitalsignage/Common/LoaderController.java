package com.five9.admin.digitalsignage.Common;

import android.util.Log;

import java.util.ArrayList;

public class LoaderController {

    private final String TAG = "LoaderController";
    private final int MAX_TIME_LOAD_FALD = 5;
    private ArrayList<LoadRequest> listLoadRequests;
    private boolean loading = false;

    private static LoaderController instance;
    public static LoaderController getInstance(){
        if (instance == null){
            instance = new LoaderController();
            instance.listLoadRequests = new ArrayList<>();
        }
        return instance;
    }

    private LoaderController(){}

    public void addRequest(String pathOnServer, String pathOnDevice){
        if(getRequestByPathOnDevice(pathOnDevice) == null){
            LoadRequest newRequest = new LoadRequest(pathOnServer, pathOnDevice);
            listLoadRequests.add(newRequest);
            loadVideoIfCan();
        }
    }

    private LoadRequest getRequestByPathOnDevice(String path) {
        for (int i = 0; i < listLoadRequests.size(); i++){
            if (listLoadRequests.get(i).pathOnDevice.equals(path))
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
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.setRequest(request);
        downloadTask.execute();
    }

    public void onLoadRequestSuccess(LoadRequest request) {
        loading = false;
        loadVideoIfCan();
        Log.d(TAG, "onSuccess: ");
    }

    public void onLoadRequestFail(LoadRequest request) {
        Log.d(TAG, "onFail: ");
        loading = false;
        doIfLoadFail(request);
        loadVideoIfCan();
    }

    public void onLoadRequestCancel(LoadRequest request) {
        Log.d(TAG, "onCancel: ");
        loading = false;
        doIfLoadFail(request);
        loadVideoIfCan();
    }

    private void doIfLoadFail(LoadRequest request) {
        LoadRequest check = getRequestByPathOnDevice(request.pathOnDevice);
        Log.d(TAG, "doIfLoadFail: " + check);
        if (check == null) {
            request.loadFailTime++;
            if (request.loadFailTime < MAX_TIME_LOAD_FALD) {
                listLoadRequests.add(request);
            }
        }
    }

    public class LoadRequest {
        public String pathOnDevice;
        public String pathOnServer;
        public int loadFailTime;
        public LoadRequest(String pathOnServer, String pathOnDevice){
            this.pathOnDevice = pathOnDevice;
            this.pathOnServer = pathOnServer;
        }

    }


}


