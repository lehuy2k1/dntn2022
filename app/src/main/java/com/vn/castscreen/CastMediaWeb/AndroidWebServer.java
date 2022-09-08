package com.vn.castscreen.CastMediaWeb;

import com.vn.castscreen.CastScreenApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import fi.iki.elonen.NanoHTTPD;

public class AndroidWebServer extends NanoHTTPD {

    private static final String TAG = AndroidWebServer.class.getSimpleName();

    public AndroidWebServer(int port) {
        super(port);
        clientWebSocketServer = new ClientWebSocketServer(9999, true);
    }

    private ClientWebSocketServer clientWebSocketServer;

    public AndroidWebServer(String hostname, int port) {
        super(hostname, port);
        clientWebSocketServer = new ClientWebSocketServer(9999, true);
    }

    @Override
    public void start() throws IOException {
        super.start();
        if (clientWebSocketServer != null) clientWebSocketServer.start();
    }

    @Override
    public Response serve(IHTTPSession session) {

        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(CastScreenApplication.applicationContext.getAssets().open("index.html"), StandardCharsets.UTF_8));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                sb.append(readLine.toCharArray());
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String html = sb.toString();
        return newFixedLengthResponse(html);
    }

}
