package com.vn.castscreen.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.net.URLEncoder;
import java.util.Locale;

public class Utils {

    public static final String DISCONNECT = "com.nekosoft.cast_DISCONNECT";
    public  static final String pathMedia = "/sdcard/mediacast/defaultconnected.png";
    public static final int FORWARD = 15;
    public static final int PORT = 8386;
    public static final String ipLocal = "127.0.0.1";
    public static final int REWIND = 15;
    public static final String audio = "audio/*";
    public static final String image = "image/*";
    public static final String video = "video/*";

    public static final int NETWORK_STATUS_MOBILE = 2;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int TYPE_WIFI = 1;

    @SuppressLint("DefaultLocale")
    public static String getIpAddress(Context context) {
        int ipAddress = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress();
        return String.format("http://%d.%d.%d.%d", Integer.valueOf(ipAddress & 255), Integer.valueOf((ipAddress >> 8) & 255), Integer.valueOf((ipAddress >> 16) & 255), Integer.valueOf((ipAddress >> 24) & 255));
    }

    public static String getMimeType(Uri uri, Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap.getSingleton();
        return contentResolver.getType(uri);
    }

    public static String formatTime(long millisec) {
        int seconds = (int) (millisec / 1000);
        int hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        int minutes = seconds / 60;
        seconds %= 60;

        String time;
        if (hours > 0) {
            time = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            time = String.format(Locale.US, "%d:%02d", minutes, seconds);
        }

        return time;
    }

    public static String getExtension(Uri uri, Context context) {
        String str =    MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(Uri.parse(uri.toString().toLowerCase())));
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(Uri.parse(uri.toString().toLowerCase())));
    }

    public static boolean checkNetworkConenction(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(1).isConnected();
    }

    public static void saveDeviceId(Context context, String str) {
        SharedPreferences.Editor edit = context.getSharedPreferences("MyPreferences", 0).edit();
        edit.putString("recentDeviceId", str);
        edit.commit();
    }

    public static int getConnectivityStatus(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return 0;
        }
        if (activeNetworkInfo.getType() == 1) {
            return 1;
        }
        return activeNetworkInfo.getType() == 0 ? 2 : 0;
    }

    public static int getConnectivityStatusString(Context context) {
        int connectivityStatus = getConnectivityStatus(context);
        if (connectivityStatus == 1) {
            return 1;
        }
        return connectivityStatus == 2 ? 2 : 0;
    }
    public static String filterPath(String str) {
        String str2;
        String replace;
        if (str == null) {
            return null;
        }
        String replace2 = str.replace("file:///", "").replace("file://", "");
        try {
            str2 = URLEncoder.encode(replace2, "UTF-8");
        } catch (Exception unused2) {
            str2 = replace2;
            replace = str2.replace(" ", "%20");
            return replace;
        }
        replace = str2.replace(" ", "%20");
        if (replace.startsWith("/")) {
            replace = replace.replaceFirst("/", "");
        }
        return replace;
    }

    public static String getMimeType(String str) {
        String str2 = "text/*";
        if (isVideoFile(str)) {
            return "video/*";
        }
        if (isAudioFile(str)) {
            return "audio/*";
        }
        if (isImageFile(str)) {
            return "image/*";
        }
        return isPdfFile(str) ? "application/pdf" : str2;
    }


    public static boolean isApkFile(String str) {
        return str != null && str.toLowerCase().contains(".apk");
    }

    public static boolean isPdfFile(String str) {
        return str != null && str.toLowerCase().contains(".pdf");
    }

    public static boolean isTextFile(String str) {
        return str != null && (str.toLowerCase().contains(".txt") || str.toLowerCase().contains(".log"));
    }

    public static boolean isImageFile(String str) {
        return str != null && (str.toLowerCase().contains(".jpg") || str.toLowerCase().contains(".jpeg") || str.toLowerCase().contains(".png") || str.toLowerCase().contains(".gif"));
    }

    public static boolean isCompessedFile(String str) {
        return str != null && (str.toLowerCase().contains(".zip") || str.toLowerCase().contains(".gz") || str.toLowerCase().contains(".tar") || str.toLowerCase().contains(".7z") || str.toLowerCase().contains(".bz2"));
    }

    public static boolean isAudioFile(String str) {
        return str != null && (str.toLowerCase().contains(".mp3") || str.toLowerCase().contains(".mpeg3") || str.toLowerCase().contains(".aiff") || str.toLowerCase().contains(".webm") || str.toLowerCase().contains(".3gp") || str.toLowerCase().contains(".m4a") || str.toLowerCase().contains(".m4p") || str.toLowerCase().contains(".wav") || str.toLowerCase().contains(".wma") || str.toLowerCase().contains(".aac"));
    }

    public static boolean isVideoFile(String str) {
        return str != null && (str.toLowerCase().contains(".mkv") || str.toLowerCase().contains(".ogg") || str.toLowerCase().contains(".avi") || str.toLowerCase().contains(".mpeg") || str.toLowerCase().contains(".mpg") || str.toLowerCase().contains(".mov") || str.toLowerCase().contains(".3gp") || str.toLowerCase().contains(".wmv") || str.toLowerCase().contains(".mp4") || str.toLowerCase().contains(".m4p") || str.toLowerCase().contains(".m4v"));
    }

    public static boolean isStartableFile(String str) {
        return !isApkFile(str) && (isImageFile(str) || isVideoFile(str) || isAudioFile(str) || isPdfFile(str) || isTextFile(str));
    }

    public static int[] getImageDimension(Uri uri){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getLastPathSegment()).getAbsolutePath(), options);
        return new int[]{options.outWidth, options.outHeight};
    }
}
