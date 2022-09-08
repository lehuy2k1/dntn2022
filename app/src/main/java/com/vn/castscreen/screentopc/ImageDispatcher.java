package com.vn.castscreen.screentopc;

import com.vn.castscreen.CastScreenApplication;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

public final class ImageDispatcher {
    private volatile boolean isThreadRunning;
    private JpegStreamerThread mJpegStreamerThread;
    private final Object mLock = new Object();

    public class JpegStreamerThread extends Thread {
        private byte[] mCurrentJpeg;
        private byte[] mLastJpeg;
        private int mSleepCount;

        JpegStreamerThread() {
            super(JpegStreamerThread.class.getSimpleName());
        }

        @Override
        public void run() {
            while (!isInterrupted() && ImageDispatcher.this.isThreadRunning) {
                this.mCurrentJpeg = CastScreenApplication.getAppData().getImageQueue().poll();
                byte[] bArr = this.mCurrentJpeg;
                if (bArr == null) {
                    try {
                        sleep(24L);
                        this.mSleepCount++;
                        if (this.mSleepCount >= 20) {
                            sendLastJPEGToClients();
                        }
                    } catch (InterruptedException unused) {
                        unused.printStackTrace();
                    }
                } else {
                    this.mLastJpeg = bArr;
                    sendLastJPEGToClients();
                }
            }
        }

        private void sendLastJPEGToClients() {
            this.mSleepCount = 0;
            synchronized (ImageDispatcher.this.mLock) {
                if (ImageDispatcher.this.isThreadRunning) {
                    Iterator<Client> it = CastScreenApplication.getAppData().getClientQueue().iterator();
                    while (it.hasNext()) {
                        it.next().sendClientData(2, this.mLastJpeg, false);
                    }
                }
            }
        }
    }

    public void addClient(Socket socket) {
        synchronized (this.mLock) {
            if (this.isThreadRunning) {
                try {
                    Client client = new Client(socket);
                    client.sendClientData(Client.CLIENT_HEADER, null, false);
                    CastScreenApplication.getAppData().getClientQueue().add(client);
                    CastScreenApplication.getAppData().setClients(CastScreenApplication.getAppData().getClientQueue().size());
                } catch (IOException unused) {
                    unused.printStackTrace();
                }
            }
        }
    }

    public void start() {
        synchronized (this.mLock) {
            if (!this.isThreadRunning) {
                this.mJpegStreamerThread = new JpegStreamerThread();
                this.mJpegStreamerThread.start();
                this.isThreadRunning = true;
            }
        }
    }


    public void stop(byte[] bArr) {
        synchronized (this.mLock) {
            if (this.isThreadRunning) {
                this.isThreadRunning = false;
                this.mJpegStreamerThread.interrupt();
                Iterator<Client> it = CastScreenApplication.getAppData().getClientQueue().iterator();
                while (it.hasNext()) {
                    it.next().sendClientData(Client.CLIENT_IMAGE, bArr, true);
                }
                CastScreenApplication.getAppData().getClientQueue().clear();
                CastScreenApplication.getAppData().setClients(0);
            }
        }
    }
}
