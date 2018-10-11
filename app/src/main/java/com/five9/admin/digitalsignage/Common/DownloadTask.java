package com.five9.admin.digitalsignage.Common;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.five9.admin.digitalsignage.Object.Schedule;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask  extends AsyncTask<Void, Void, Boolean> {

    private final String TAG = "DownloadTask";
    private final int TIMEOUT_CONNECTION =5000;//5sec
    private final int TIMEOUT_SOCKET = 5000;//30sec
    private LoaderController.LoadRequest request;

    public void setRequest(LoaderController.LoadRequest request){
        this.request = request;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return download(request.schedule);
    }

    public boolean download(Schedule schedule){
        File file = null;
        try {
            file = new File(schedule.getPathOnDevice());
            if (file.exists()) {
                Log.d(TAG, "download: file exists");
                file.delete();
//                return true;
            } else {
                file.getParentFile().mkdirs();
            }
            URL url;

            url = new URL(schedule.path);
            Log.d(TAG, "download: " + url);
            Request.Builder builder = new Request.Builder()
                    .url(url);
            OkHttpClient client = new  OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS).build();
            Response response = client.newCall(builder.build()).execute();
            BufferedInputStream inStream = new BufferedInputStream( response.body().byteStream(), 1024);
            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[1024];
            int len;
            while ((len = inStream.read(buff)) != -1)
            {
                outStream.write(buff,0,len);
            }
            outStream.flush();
            outStream.close();
            inStream.close();
            response.body().close();
            if (TextUtils.isEmpty(request.schedule.md5cksum)
		            || request.schedule.md5cksum.equalsIgnoreCase(checksum(file.getPath())))
	        	return true;
        } catch (Exception e) {
            Log.d(TAG, "download: " + e.getMessage());
            try {
                file.delete();
            } catch (Exception ex){}
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean)
            LoaderController.getInstance().onLoadRequestSuccess(request);
        else
            LoaderController.getInstance().onLoadRequestFail(request);
        super.onPostExecute(aBoolean);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        LoaderController.getInstance().onLoadRequestCancel(request);

    }

	private static String checksum(String filepath){
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {

				byte[] buff = new byte[1024];
				while ((dis.read(buff,0, 1024)) != -1)
				{
					md = dis.getMessageDigest();
				}
			}
			// bytes to hex
			StringBuilder result = new StringBuilder();
			for (byte b : md.digest()) {
				result.append(String.format("%02x", b));
			}
			return result.toString();
		} catch (Exception e){}
		return "";
	}
}
