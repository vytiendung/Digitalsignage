package com.five9.admin.digitalsignage.Common;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.five9.admin.digitalsignage.BuildConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.five9.admin.digitalsignage.Common.Constant.ACCESSTOKEN;
import static com.five9.admin.digitalsignage.Common.Constant.DEV_ID;
import static com.five9.admin.digitalsignage.Common.Constant.GET_METHOD;
import static com.five9.admin.digitalsignage.Common.Constant.IP_ADDRESS;
import static com.five9.admin.digitalsignage.Common.Constant.IS_DEVICE;
import static com.five9.admin.digitalsignage.Common.Constant.KAFKA;
import static com.five9.admin.digitalsignage.Common.Constant.MAC_ADDRESS;
import static com.five9.admin.digitalsignage.Common.Constant.OPTION;
import static com.five9.admin.digitalsignage.Common.Constant.PASSWORD;
import static com.five9.admin.digitalsignage.Common.Constant.POST_METHOD;
import static com.five9.admin.digitalsignage.Common.Constant.USERNAME;

public class ApiConnection {
    public static String TAG = "ApiConnection";

    public void sendSchedulesVersionToServer(String version, RequestNetworkListener listener){
        listener.onSuccess("");
    }

    public void getListSchedules(final RequestNetworkListener listener){
        new DoBackgroundTask(new IDoBackgroundTask() {
            @Override
            public void doInBackGround() {
                if (TextUtils.isEmpty(DataTranform.jwtToken))
                    requestLogin(listener);
                else
                    requestGetSchedule(listener);
            }
        }).execute();
    }

    public void requestLogin(RequestNetworkListener listener){
        try {
            URL url = new URL(Config.getServerEndpoint() + Constant.API_V1 + Constant.LOGIN_METHOD);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            HashMap<String,String> mapParam = new HashMap<>();
            mapParam.put(USERNAME, "admin");
            mapParam.put(PASSWORD, "Five9@123");
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
            DataTranform.jwtToken = "JWT " + jsonObject.getString(Constant.TOKEN);
            requestGetSchedule(listener);
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

    public static void getDataOnStart(){

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                login(new RequestNetworkListener() {
                    @Override
                    public void onSuccess(String response) {
                        onLoginSuccess();
                    }

                    @Override
                    public void onFail(String response) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getDataOnStart();
                            }
                        }, 1000);
                    }

                    @Override
                    public void onCancel(String response) {
                    }
                });
                return null;
            }
        }.execute();
    }

    private static void onLoginSuccess() {
        deviceRegister(new RequestNetworkListener() {
            @Override
            public void onSuccess(String response) {
                onDevRegisterSuccess();
            }

            @Override
            public void onFail(String response) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onLoginSuccess();
                    }
                }, 1000);
            }

            @Override
            public void onCancel(String response) {

            }
        });
    }

    private static void onDevRegisterSuccess() {
        getId(new RequestNetworkListener() {
            @Override
            public void onSuccess(String response) {
                onGetIdSuccess();
            }

            @Override
            public void onFail(String response) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onDevRegisterSuccess();
                    }
                }, 1000);
            }

            @Override
            public void onCancel(String response) {

            }
        });
    }

    private static void onGetIdSuccess() {
        if (TextUtils.isEmpty(DataTranform.devid) || DataTranform.devid.equalsIgnoreCase("null")) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    onDevRegisterSuccess();
                }
            },1000);
        }else {
            getInfo(new RequestNetworkListener() {
                @Override
                public void onSuccess(String response) {
                    onGetInfoSuccess();
                }

                @Override
                public void onFail(String response) {
                    new Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                        @Override
                        public void run() {
                            onGetIdSuccess();
                        }
                    }, 1000);
                }

                @Override
                public void onCancel(String response) {

                }
            });
        }
    }

    private static void onGetInfoSuccess() {
        SocketController.getInstance().startConnect("");
        ListSchedulesManager.getInstance().initData();
    }

    private static void login(final RequestNetworkListener listener){
        try {
            String url = Config.getServerEndpoint() + Constant.API_V1 + Constant.LOGIN_METHOD;
            OkHttpClient client = getOkHttpBuilder().build();
            RequestBody formBody = new FormBody.Builder()
                    .add(USERNAME, Config.getUseName())
                    .add(PASSWORD, Config.getPw())
                    .build();
            Response response = client.newCall(getRequest(url, formBody, POST_METHOD)).execute();
            String res = response.body().string();
            JSONObject jsonObject = new JSONObject(res);
            DataTranform.jwtToken = "JWT " + jsonObject.getString(Constant.TOKEN);
            response.close();
            listener.onSuccess(res);
        } catch (Exception e) {
            Log.d(TAG, "login err: " + e.getMessage());
            listener.onFail(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deviceRegister(final RequestNetworkListener listener){
        try {
            String url = Config.getServerEndpoint() + Constant.API_V1 + Constant.DEVICE + Constant.REGISTER_METHOD;
            OkHttpClient client = getOkHttpBuilder().build();
            RequestBody formBody = new FormBody.Builder()
                    .add(ACCESSTOKEN, DataStorage.getInstance().getAccessToken())
                    .add(IP_ADDRESS, DeviceInfo.getIpAddress())
                    .add(MAC_ADDRESS, DeviceInfo.getMacAddress())
                    .add(IS_DEVICE, "true")
                    .build();
            Response response = client.newCall(getRequest(url, formBody, POST_METHOD)).execute();
            String res = response.body().string();
            JSONObject jsonObject = new JSONObject(res);
            DataStorage.getInstance().saveAccessToken(jsonObject.getString(Constant.ACCESSTOKEN));
            response.close();
            listener.onSuccess(res);
        } catch (Exception e) {
            Log.d(TAG, "deviceRegister err: " + e.getMessage());
            listener.onFail(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void getId(final RequestNetworkListener listener){
        try {
            String url = Config.getServerEndpoint() + Constant.API_V1 + Constant.DEVICE + Constant.GET_ID_METHOD;
            OkHttpClient client = getOkHttpBuilder().build();
            RequestBody formBody = new FormBody.Builder()
                    .add(ACCESSTOKEN, DataStorage.getInstance().getAccessToken())
                    .build();
            Response response = client.newCall(getRequest(url, formBody, POST_METHOD)).execute();
            String res = response.body().string();
            JSONObject jsonObject = new JSONObject(res);
            DataTranform.devid = jsonObject.getString(Constant.DEV_ID);
            response.close();
            listener.onSuccess(res);
        } catch (Exception e) {
            Log.d(TAG, "deviceRegister err: " + e.getMessage());
            listener.onFail(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void getInfo(final RequestNetworkListener listener){
        try {
            String url = Config.getServerEndpoint() + Constant.API_V1 + Constant.DEVICE + Constant.GET_INFO_METHOD;
            OkHttpClient client = getOkHttpBuilder().build();
            RequestBody formBody = new FormBody.Builder()
                    .add(ACCESSTOKEN, DataStorage.getInstance().getAccessToken())
                    .add(DEV_ID, DataTranform.devid)
                    .add(OPTION, KAFKA)
                    .build();
            Response response = client.newCall(getRequest(url, formBody, POST_METHOD)).execute();
            String res = response.body().string();
            listener.onSuccess(res);
        } catch (Exception e) {
            Log.d(TAG, "deviceRegister err: " + e.getMessage());
            listener.onFail(e.getMessage());
            e.printStackTrace();
        }
    }


    public void requestGetSchedule(RequestNetworkListener listener){
        try {
            String url = Config.getServerEndpoint() + Constant.API_V1 + Constant.GET_SCHEDULE_METHOD;
            OkHttpClient client = getOkHttpBuilder().build();
            Response response = client.newCall(getRequest(url, null, GET_METHOD)).execute();
            String res = response.body().string();
            Log.d(TAG, "requestGetSchedule: " + res);
            listener.onSuccess(res);

        } catch (Exception e) {
            Log.d(TAG, "requestGetSchedule err: " + e.getMessage());
            listener.onFail(e.getMessage());
            e.printStackTrace();
        }
    }

    @NonNull
    private static OkHttpClient.Builder getOkHttpBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        return builder;
    }

    private static Request getRequest(String url, RequestBody body, int method) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader(Constant.AUTHORIZATION_TOKEN, DataTranform.jwtToken);
        if (method == Constant.POST_METHOD){
            builder.post(body);
        } else if (method == Constant.PUT_METHOD){
            builder.put(body);
        }
        return builder.build();
    }
}
