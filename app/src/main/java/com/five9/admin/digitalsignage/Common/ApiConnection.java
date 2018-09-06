package com.five9.admin.digitalsignage.Common;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiConnection {

    public static String TAG = "ApiConnection";
    private String token;

    public void sendSchedulesVersionToServer(String version, RequestNetworkListener listener){
        listener.onSuccess("");
    }

    public void getListSchedules(final RequestNetworkListener listener){
        new DoBackgroundTask(new IDoBackgroundTask() {
            @Override
            public void doInBackGround() {
                if (token == null)
                    requestLogin(listener);
                else
                    requestGetSchedule(token, listener);
            }
        }).execute();
    }

    public void requestLogin(RequestNetworkListener listener){
        try {
            URL url = new URL(Constrant.END_POINT + Constrant.LOGIN_METHOD);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            HashMap<String,String> mapParam = new HashMap<>();
            mapParam.put("username", "admin");
            mapParam.put("password", "Five9@123");
            String postContent = getPostContent(mapParam);
            httpConnection.addRequestProperty("Connection", "keep-alive");
            httpConnection.setRequestProperty("Accept-Encoding", "identity");
            httpConnection.setDoOutput(true);
            httpConnection.setReadTimeout(30000);
            httpConnection.setConnectTimeout(30000);
            httpConnection.setFixedLengthStreamingMode(postContent.length());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpConnection.getOutputStream());
            outputStreamWriter.write(postContent);
            outputStreamWriter.close();
            httpConnection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String tmpString;
            while( (tmpString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(tmpString + '\n');
            }
            String res = stringBuilder.toString();
            Log.d(TAG, "requestLogin: " + res);
            JSONObject jsonObject = new JSONObject(res);
            token = jsonObject.getString("token");
            requestGetSchedule(token, listener);
            httpConnection.disconnect();
        } catch (Exception e) {
            Log.d(TAG, "test: " + e.getMessage());
            listener.onFail(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getPostContent(Map source){
        String params = "";
        Set<Map.Entry> set = source.entrySet();
        for (Map.Entry entry : set) {
            if (params.length() > 0) {
                params = params + "&";
            }
            params = params + (String) entry.getKey() + "=" + (String) entry.getValue();
        }
        return params;
    }

    public void requestGetSchedule(String token, RequestNetworkListener listener){
        Log.d(TAG, "requestGetSchedule: JWT  " + token);
        try {
            token = "JWT " + token;
            URL url = new URL(Constrant.END_POINT + Constrant.GETSCHEDULE_METHOD);
            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", token);
            OkHttpClient client = getOkHttpBuilder().build();
            Response response = client.newCall(builder.build()).execute();
            String res = response.body().string();
            Log.d(TAG, "requestGetSchedule: " + res);

//
//            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
//            httpConnection.setRequestMethod("POST");
////            httpConnection.setRequestProperty("Accept-Encoding", "identity");
//            httpConnection.setReadTimeout(30000);
//            httpConnection.setConnectTimeout(30000);
////            httpConnection.addRequestProperty("Connection", "keep-alive");
//            httpConnection.addRequestProperty("Authorization", token);
//            httpConnection.setDoOutput(true);
//            httpConnection.connect();
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(),"utf-8"));
//            StringBuilder stringBuilder = new StringBuilder();
//            String tmpString;
//            while( (tmpString = bufferedReader.readLine()) != null ) {
//                stringBuilder.append(tmpString + '\n');
//            }
//            String res = stringBuilder.toString();
//            Log.d(TAG, "requestGetSchedule: " + res);
            listener.onSuccess(res);

        } catch (Exception e) {
            Log.d(TAG, "requestGetSchedule err: " + e.getMessage());
            listener.onFail(e.getMessage());
            e.printStackTrace();
        }
    }

    @NonNull
    private static OkHttpClient.Builder getOkHttpBuilder() {
        return new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);
    }
}
