package com.five9.admin.digitalsignage.Common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.five9.admin.digitalsignage.MyApplication;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import okhttp3.*;
import okio.ByteString;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static com.five9.admin.digitalsignage.Common.Constant.*;

public class SocketController {
	private static SocketController instance;
    private Socket socket;
	private WebSocket ws;
	private String socketURl;
	public String TAG = "SocketController";

	private final class EchoWebSocketListener extends WebSocketListener {
		private static final int NORMAL_CLOSURE_STATUS = 1000;

		@Override
		public void onOpen(WebSocket webSocket, Response response) {
			output("onOpen : " + response.message());

		}

		@Override
		public void onMessage(WebSocket webSocket, String text) {
			output("onMessage : " + text);
			setOnGetMess(text);
		}

		@Override
		public void onMessage(WebSocket webSocket, ByteString bytes) {
//			output("Receiving bytes : " + bytes.hex());
		}

		@Override
		public void onClosing(WebSocket webSocket, int code, String reason) {
			webSocket.close(NORMAL_CLOSURE_STATUS, null);
			output("Closing : " + code + " / " + reason);
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					reConnect();
				}
			}, 5000);
		}

		@Override
		public void onFailure(WebSocket webSocket, Throwable t, Response response) {
			output("Error : " + t.getMessage());
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					reConnect();
				}
			}, 5000);
		}
	}

	private void setOnGetMess(String mess){
		Log.d(TAG, "setOnGetMess: " + mess);
//		new Handler(Looper.getMainLooper()).post(new Runnable() {
//			@Override
//			public void run() {
//				Toast.makeText(MyApplication.getInstance(),"setOnGetMess", Toast.LENGTH_LONG).show();
//			}
//		});
		try {
			JSONObject jsonObject = new JSONObject(mess);
			String event = jsonObject.getString(EVENT);
			if (event.equals(SCK_EVT_REQUEST_NEWS_CHEDULE) || event.equals(SCK_EVT_RESPONSE_UPDATESCHEDULE)){
				if (jsonObject.has(SCHEDULE)){
					ListSchedulesManager.getInstance().updateListSchedule(jsonObject.getString(SCHEDULE));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void output(final String txt) {
		Log.d("abcxxx", "output: " + txt);
	}
    public static SocketController getInstance(){
        if (instance == null){
            instance = new SocketController();
        }
        return instance;
    }

    private SocketController() {
    }

	public void startConnect(final String url){
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "startConnect: " + url);
				socketURl = url;
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder().url(url).build();
				ws = client.newWebSocket(request, new EchoWebSocketListener());
				client.dispatcher().executorService().shutdown();
				ws.send(SCK_EVT_REQUEST_NEWS_CHEDULE);
			}
		});
	}

	public void reConnect(){
		try {
			Log.d(TAG, "reConnect: ");
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(socketURl).build();
			ws = client.newWebSocket(request, new EchoWebSocketListener());
			client.dispatcher().executorService().shutdown();
			ws.send(SCK_EVT_REQUEST_NEWS_CHEDULE);
		} catch (Exception ex){}
	}

    private Emitter.Listener onGetMess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
	        Log.d("abcxxx", "call: " + args[0]);
        }
    };
}
