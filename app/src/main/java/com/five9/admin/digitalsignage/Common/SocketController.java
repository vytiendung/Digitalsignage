package com.five9.admin.digitalsignage.Common;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class SocketController {
    private static SocketController instance;
    private Socket socket;
    public static SocketController getInstance(){
        if (instance == null){
            instance = new SocketController();
        }
        return instance;
    }

    private SocketController() {
    }

    public void startConnect(String url){
        try {
            this.socket = IO.socket(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.close().connect();
        socket.on("", onGetMess);
    }

    private Emitter.Listener onGetMess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };
}
