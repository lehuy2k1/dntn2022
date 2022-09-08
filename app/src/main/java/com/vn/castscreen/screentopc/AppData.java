package com.vn.castscreen.screentopc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.vn.castscreen.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;


public final class AppData {
    private static final String DEFAULT_CLIENT_TIMEOUT = "3000";
    private static final String DEFAULT_JPEG_QUALITY = "80";
    private volatile boolean isActivityRunning;
    private volatile boolean isStreamRunning;
    private volatile int mClientTimeout;
    private int mClients;
    private final Context mContext;
    private final byte[] mIconBytes;
    private String mIndexHtmlPage;
    private volatile int mJpegQuality;
    private final float mScale;
    private volatile int mServerPort;
    private final SharedPreferences mSharedPreferences;
    private final WifiManager mWifiManager;
    private final WindowManager mWindowManager;
    private final ConcurrentLinkedDeque<byte[]> mImageQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedQueue<Client> mClientQueue = new ConcurrentLinkedQueue<>();
    private String DEFAULT_SERVER_PORT = "8080";
    private int mDensityDpi;

    public AppData(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.mScale = getScale(context);
        this.mIconBytes = getFavicon(context);
        mDensityDpi = getDensityDpi();
        this.mContext = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.mServerPort = Integer.parseInt(this.mSharedPreferences.getString(this.mContext.getString(R.string.pref_key_server_port), this.DEFAULT_SERVER_PORT));
        this.mClientTimeout = Integer.parseInt(this.mSharedPreferences.getString(this.mContext.getString(R.string.pref_key_client_con_timeout), DEFAULT_CLIENT_TIMEOUT));
        this.mJpegQuality = Integer.parseInt(this.mSharedPreferences.getString(this.mContext.getString(R.string.pref_key_jpeg_quality), DEFAULT_JPEG_QUALITY));
    }

    public void setActivityRunning(boolean z) {
        this.isActivityRunning = z;
    }

    public void setStreamRunning(boolean z) {
        this.isStreamRunning = z;
    }

    public ConcurrentLinkedDeque<byte[]> getImageQueue() {
        return this.mImageQueue;
    }

    public ConcurrentLinkedQueue<Client> getClientQueue() {
        return this.mClientQueue;
    }

    public boolean isActivityRunning() {
        return this.isActivityRunning;
    }

    public boolean isStreamRunning() {
        return this.isStreamRunning;
    }

    public WindowManager getWindowsManager() {
        return this.mWindowManager;
    }

    public int getScreenDensity() {
        return this.mDensityDpi;
    }

    public float getDisplayScale() {
        return this.mScale;
    }

    public int getJpegQuality() {
        return this.mJpegQuality;
    }

    public Point getScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        return point;
    }

    public void initIndexHtmlPage(Context context) {
        this.mIndexHtmlPage = getHtml(context, "index.html");
    }

    public String getIndexHtml(String str) {
        return this.mIndexHtmlPage.replaceFirst("SCREEN_STREAM_ADDRESS", str);
    }

    public byte[] getIcon() {
        return this.mIconBytes;
    }

    public void setClients(int i) {
        this.mClients = i;
    }

    @Nullable
    public InetAddress getIpAddress() {
        try {
            int ipAddress = this.mWifiManager.getConnectionInfo().getIpAddress();
            return InetAddress.getByAddress(new byte[]{(byte) (ipAddress & 255), (byte) ((ipAddress >> 8) & 255), (byte) ((ipAddress >> 16) & 255), (byte) ((ipAddress >> 24) & 255)});
        } catch (UnknownHostException unused) {
            return null;
        }
    }

    public int getServerPort() {
        return this.mServerPort;
    }

    public String getServerAddress() {
        return "http:/" + getIpAddress() + ":" + getServerPort();
    }

    public int getClientTimeout() {
        return this.mClientTimeout;
    }

    public boolean isWiFiConnected() {
        return this.mWifiManager.getConnectionInfo().getIpAddress() != 0;
    }

    private int getDensityDpi() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.densityDpi;
    }

    private float getScale(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    private String getHtml(Context context, String str) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open(str), StandardCharsets.UTF_8));
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
        String sb2 = sb.toString();
        sb.setLength(0);
        return sb2;
    }

    private byte[] getFavicon(Context context) {
        try {
            InputStream open = context.getAssets().open("favicon.png");
            byte[] bArr = new byte[open.available()];
            if (open.read(bArr) == 353) {
                if (open != null) {
                    open.close();
                }
                return bArr;
            }
            throw new IOException();
        } catch (IOException unused) {
            return null;
        }
    }
}
