package com.vn.castscreen.screentopc;

import com.vn.castscreen.CastScreenApplication;
import com.sdk.service.command.ServiceCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public final class HttpServer {
    private static final String DEFAULT_ADDRESS = "/";
    private static final String DEFAULT_ICO_ADDRESS = "/favicon.ico";
    private static final String DEFAULT_PIN_ADDRESS = "/?pin=";
    private static final String DEFAULT_STREAM_ADDRESS = "/screen_stream.mjpeg";
    private static final int SEVER_SOCKET_TIMEOUT = 50;
    private ImageDispatcher mImageDispatcher;
    private ServerSocket mServerSocket;
    private final Object mLock = new Object();
    private String mCurrentStreamAddress = DEFAULT_STREAM_ADDRESS;
    private HttpServerThread mHttpServerThread = new HttpServerThread();

    public class HttpServerThread extends Thread {
        HttpServerThread() {
            super(HttpServerThread.class.getSimpleName());
        }

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (mLock) {
                    try {
                        Socket accept = mServerSocket.accept();
                        String readLine = new BufferedReader(new InputStreamReader(accept.getInputStream(), "UTF8")).readLine();
                        if (readLine != null && readLine.startsWith(ServiceCommand.TYPE_GET)) {
                            String[] split = readLine.split(" ");
                            if (split.length >= 2) {
                                String str = split[1];
                                if (HttpServer.DEFAULT_ADDRESS.equals(str)) {
                                    try {
                                        sendMainPage(accept, HttpServer.this.mCurrentStreamAddress);
                                    } catch (IOException e) {
//                                        e.printStackTrace();
                                    }
                                } else if (HttpServer.this.mCurrentStreamAddress.equals(str)) {
                                    HttpServer.this.mImageDispatcher.addClient(accept);
                                } else if (HttpServer.DEFAULT_ICO_ADDRESS.equals(str)) {
                                    try {
                                        sendFavicon(accept);
                                    } catch (IOException e) {
//                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        sendNotFound(accept);
                                    } catch (IOException e) {
//                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                try {
                                    sendNotFound(accept);
                                } catch (IOException e) {
//                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (IOException e) {
//                        e.printStackTrace();
                    }
                }
            }
        }

        private void sendMainPage(Socket socket, String str) throws IOException {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), "UTF8");
            try {
                outputStreamWriter.write("HTTP/1.1 200 OK\r\n");
                outputStreamWriter.write("Content-Type: text/html\r\n");
                outputStreamWriter.write("Connection: close\r\n");
                outputStreamWriter.write("\r\n");
                outputStreamWriter.write(CastScreenApplication.getAppData().getIndexHtml(str));
                outputStreamWriter.write("\r\n");
                outputStreamWriter.flush();
                outputStreamWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendFavicon(Socket socket) throws IOException {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), "UTF8");
            try {
                outputStreamWriter.write("HTTP/1.1 200 OK\r\n");
                outputStreamWriter.write("Content-Type: image/png\r\n");
                outputStreamWriter.write("Connection: close\r\n");
                outputStreamWriter.write("\r\n");
                outputStreamWriter.flush();
                socket.getOutputStream().write(CastScreenApplication.getAppData().getIcon());
                socket.getOutputStream().flush();
                outputStreamWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendNotFound(Socket socket) throws IOException {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), "UTF8");
            try {
                outputStreamWriter.write("HTTP/1.1 301 Moved Permanently\r\n");
                outputStreamWriter.write("Location: " + CastScreenApplication.getAppData().getServerAddress() + "\r\n");
                outputStreamWriter.write("Connection: close\r\n");
                outputStreamWriter.write("\r\n");
                outputStreamWriter.flush();
                outputStreamWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (!this.mHttpServerThread.isAlive()) {
            this.mCurrentStreamAddress = DEFAULT_STREAM_ADDRESS;
            try {
                this.mServerSocket = new ServerSocket(CastScreenApplication.getAppData().getServerPort(), 4, CastScreenApplication.getAppData().getIpAddress());
                this.mServerSocket.setSoTimeout(50);
                this.mImageDispatcher = new ImageDispatcher();
                this.mImageDispatcher.start();
                this.mHttpServerThread.start();
            } catch (IOException unused) {
                unused.printStackTrace();
            }
        }
    }

    public void stop(byte[] bArr) {
        if (this.mHttpServerThread.isAlive()) {
            this.mHttpServerThread.interrupt();
            synchronized (this.mLock) {
                this.mImageDispatcher.stop(bArr);
                this.mImageDispatcher = null;
                try {
                    this.mServerSocket.close();
                } catch (IOException unused) {
                }
                this.mServerSocket = null;
                this.mHttpServerThread = new HttpServerThread();
            }
        }
    }

    private String getRandomStreamAddress(String str) {
        Random random = new Random(Long.parseLong(str));
        char[] cArr = new char[10];
        for (int i = 0; i < 10; i++) {
            cArr[i] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(random.nextInt(62));
        }
        return "/screen_stream_" + String.valueOf(cArr) + ".mjpeg";
    }
}
