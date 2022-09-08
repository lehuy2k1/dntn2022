package com.vn.castscreen.CastMediaWeb;

import android.util.Log;

import com.vn.castscreen.CastScreenApplication;
import com.vn.castscreen.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoWSD;

public class ClientWebSocketServer extends NanoWSD {

    /**
     * logger to log to.
     */
    private static final String LOG = ClientWebSocketServer.class.getSimpleName();

    private final boolean debug;

    public ClientWebSocketServer(int port, boolean debug) {
        super(port);
        this.debug = debug;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new DebugWebSocket(this, handshake);
    }


    public static class DebugWebSocket extends WebSocket {

        private final ClientWebSocketServer server;

        private static List<DebugWebSocket> list = new ArrayList<>();

        public static void reloadHtml(String url) {
            for (DebugWebSocket webSocket : list) {
                try {
                    WebSocketFrame webSocketFrame = new WebSocketFrame(WebSocketFrame.OpCode.Text, true, url);
                    webSocket.sendFrame(webSocketFrame);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public DebugWebSocket(ClientWebSocketServer server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
            Log.d(LOG, "onOpen ");
            list.add(this);
            reloadHtml(Utils.getIpAddress(CastScreenApplication.applicationContext) + ":" + Utils.PORT + Utils.pathMedia);
        }

        @Override
        protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
            list.remove(this);
            if (server.debug) {
                Log.d(LOG, "C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                        + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            }
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            try {
                Log.d(LOG, "onMessage " + message.getTextPayload());
                if ("onPing".equalsIgnoreCase(message.getTextPayload())) {
                    message.setUnmasked();
                    message.setTextPayload("onPong");
                } else {
                    message.setUnmasked();
                    message.setTextPayload(Utils.getIpAddress(CastScreenApplication.applicationContext) + ":" + Utils.PORT + Utils.pathMedia);
                }
                sendFrame(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                Log.d(LOG, "P " + pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            exception.printStackTrace();
        }
    }
}
