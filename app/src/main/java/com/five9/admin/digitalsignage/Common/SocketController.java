package com.five9.admin.digitalsignage.Common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
	private final class EchoWebSocketListener extends WebSocketListener {
		private static final int NORMAL_CLOSURE_STATUS = 1000;

		@Override
		public void onOpen(WebSocket webSocket, Response response) {
		}

		@Override
		public void onMessage(WebSocket webSocket, String text) {
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
		}

		@Override
		public void onFailure(WebSocket webSocket, Throwable t, Response response) {
			output("Error : " + t.getMessage());
		}
	}

	private void setOnGetMess(String mess){
		Log.d("abcxxx", "setOnGetMess: " + mess);
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

	public void startConnect(String url){
		Log.d("abcxxx", "startConnect: " + url);
		OkHttpClient client = new OkHttpClient();
		//ws://192.168.8.22:8080/socketads?devid=0001000110110111
		Request request = new Request.Builder().url(url).build();
		WebSocket ws = client.newWebSocket(request, new EchoWebSocketListener());
		client.dispatcher().executorService().shutdown();
		ws.send(SCK_EVT_REQUEST_NEWS_CHEDULE);
	}

    private Emitter.Listener onGetMess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
	        Log.d("abcxxx", "call: " + args[0]);
        }
    };
}
