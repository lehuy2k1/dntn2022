package com.vn.castscreen.screentopc;

import com.vn.castscreen.CastScreenApplication;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Client {
    static final int CLIENT_HEADER = 1;
    static final int CLIENT_IMAGE = 2;
    private volatile boolean isClosing;
    private volatile boolean isSending;
    private final Socket mClientSocket;
    private final ExecutorService mClientThreadPool = Executors.newFixedThreadPool(2);
    private final OutputStreamWriter mOtputStreamWriter;


    public Client(Socket socket) throws IOException {
        this.mClientSocket = socket;
        this.mOtputStreamWriter = new OutputStreamWriter(this.mClientSocket.getOutputStream(), "UTF8");
    }


    public void closeSocket() {
        this.isClosing = true;
        CastScreenApplication.getAppData().getClientQueue().remove(this);
        CastScreenApplication.getAppData().setClients(CastScreenApplication.getAppData().getClientQueue().size());
        try {
            this.mClientThreadPool.shutdownNow();
            this.mOtputStreamWriter.close();
            this.mClientSocket.close();
        } catch (IOException unused) {
            unused.printStackTrace();
        }
    }


    public void sendClientData(final int i, final byte[] bArr, final boolean z) {
        if (!this.isClosing) {
            if (z && bArr == null) {
                closeSocket();
            }
            if (!this.isSending) {
                this.isSending = true;
                this.mClientThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Client.this.mClientThreadPool.submit(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    if (i == Client.CLIENT_HEADER) {
                                        Client.this.sendHeader();
                                    }
                                    if (i != Client.CLIENT_IMAGE) {
                                        return null;
                                    }
                                    Client.this.sendImage(bArr);
                                    return null;
                                }
                            }).get(CastScreenApplication.getAppData().getClientTimeout(), TimeUnit.MILLISECONDS);
                            if (z) {
                                Client.this.closeSocket();
                            }
                            Client.this.isSending = false;
                        } catch (Exception unused) {
                            Client.this.closeSocket();
                        }
                    }
                });
            }
        }
    }


    public void sendHeader() throws IOException {
        this.mOtputStreamWriter.write("HTTP/1.1 200 OK\r\n");
        this.mOtputStreamWriter.write("Content-Type: multipart/x-mixed-replace; boundary=y5exa7CYPPqoASFONZJMz4Ky\r\n");
        this.mOtputStreamWriter.write("Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n");
        this.mOtputStreamWriter.write("Pragma: no-cache\r\n");
        this.mOtputStreamWriter.write("Connection: keep-alive\r\n");
        this.mOtputStreamWriter.write("\r\n");
        this.mOtputStreamWriter.flush();
    }


    public void sendImage(byte[] bArr) throws IOException {
        this.mOtputStreamWriter.write("--y5exa7CYPPqoASFONZJMz4Ky\r\n");
        this.mOtputStreamWriter.write("Content-Type: image/jpeg\r\n");
        OutputStreamWriter outputStreamWriter = this.mOtputStreamWriter;
        outputStreamWriter.write("Content-Length: " + bArr.length + "\r\n");
        this.mOtputStreamWriter.write("\r\n");
        this.mOtputStreamWriter.flush();
        this.mClientSocket.getOutputStream().write(bArr);
        this.mClientSocket.getOutputStream().flush();
        this.mOtputStreamWriter.write("\r\n");
        this.mOtputStreamWriter.flush();
    }
}
