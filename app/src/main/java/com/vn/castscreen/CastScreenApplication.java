package com.vn.castscreen;

import static com.vn.castscreen.utils.Utils.PORT;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.vn.castscreen.screentopc.AppData;
import com.vn.castscreen.screentopc.HttpServer;
import com.vn.castscreen.screentopc.ImageGenerator;
import com.pixplicity.easyprefs.library.Prefs;
import com.sdk.device.ConnectableDevice;
import com.sdk.discovery.CapabilityFilter;
import com.sdk.discovery.DiscoveryManager;
import com.sdk.discovery.provider.CastDiscoveryProvider;
import com.sdk.discovery.provider.SSDPDiscoveryProvider;
import com.sdk.discovery.provider.ZeroconfDiscoveryProvider;
import com.sdk.service.AirPlayService;
import com.sdk.service.CastService;
import com.sdk.service.DIALService;
import com.sdk.service.DLNAService;

import com.sdk.service.RokuService;
import com.sdk.service.WebOSTVService;
import com.sdk.service.capability.KeyControl;
import com.sdk.service.capability.MediaControl;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.capability.TVControl;
import com.sdk.service.capability.VolumeControl;
import com.sdk.service.sessions.LaunchSession;
import com.sdk.service.sessions.WebAppSession;
import com.universalimageloader.core.DisplayImageOptions;
import com.universalimageloader.core.ImageLoader;
import com.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CastScreenApplication extends MultiDexApplication {

    public static Context applicationContext;
    public static volatile Handler applicationHandler;
    public static CastScreenApplication sAppInstance;
    public String LOG = "APPLICATION";
    public PendingIntent contentIntent;
    public String lastConnectedId = "";
    public AndroidHTTPServer mAndroidHTTPServer;
    private AppData mAppData;
    public ConnectableDevice mDevice;
    public DiscoveryManager mDiscoveryManager;
    private HttpServer mHttpServer;
    private ImageGenerator mImageGenerator;
    public LaunchSession mLaunchSession;
    public MediaControl mMediaControl;


    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection.Callback mProjectionCallback;
    public Intent notificationIntent;
    public RemoteViews notificationLayout;
    public static Point displaySize = new Point();
    public static float density = 1.0f;


    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
        checkDisplaySize();
        density = applicationContext.getResources().getDisplayMetrics().density;
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(new DisplayImageOptions.Builder().considerExifParams(true).resetViewBeforeLoading(true).showImageOnLoading(R.drawable.nophotos).showImageOnFail(R.drawable.nophotos).delayBeforeLoading(0).build()).memoryCacheExtraOptions(480, 800).threadPoolSize(5).build());
        DiscoveryManager.init(getApplicationContext());
        startDiscoveryManger();
        new Prefs.Builder().setContext(this).setMode(0).setPrefsName(getPackageName()).setUseDefaultSharedPreference(true).build();
//        createDirectory();
        sAppInstance = this;
        this.mAppData = new AppData(this);
        getAppData().initIndexHtmlPage(this);
        this.mHttpServer = new HttpServer();
        this.mImageGenerator = new ImageGenerator();
        this.mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        this.mHttpServer.start();
    }

    public void startDiscoveryManger() {
        Log.d(this.LOG, "Starting Discovery Manager");
        CapabilityFilter capabilityFilter = new CapabilityFilter(MediaPlayer.Play_Video,
                MediaControl.Any,
                VolumeControl.Volume_Up_Down);
        CapabilityFilter capabilityFilter2 = new CapabilityFilter(MediaPlayer.Display_Image);
        this.mDiscoveryManager = DiscoveryManager.getInstance();
        this.mDiscoveryManager.registerDefaultDeviceTypes();
//        DIALService.registerApp("Levak");
        this.mDiscoveryManager.setCapabilityFilters(capabilityFilter, capabilityFilter2);
        this.mDiscoveryManager.registerDeviceService(AirPlayService.class, ZeroconfDiscoveryProvider.class);
        this.mDiscoveryManager.registerDeviceService(CastService.class, CastDiscoveryProvider.class);
        this.mDiscoveryManager.registerDeviceService(DIALService.class, SSDPDiscoveryProvider.class);
        this.mDiscoveryManager.registerDeviceService(RokuService.class, SSDPDiscoveryProvider.class);
        this.mDiscoveryManager.registerDeviceService(DLNAService.class, SSDPDiscoveryProvider.class);
        this.mDiscoveryManager.registerDeviceService(WebOSTVService.class, SSDPDiscoveryProvider.class);
        this.mDiscoveryManager.setPairingLevel(DiscoveryManager.PairingLevel.ON);
        this.mDiscoveryManager.start();
        startServer();
    }

    public void createDirectory() {
        File file = new File("/mnt/sdcard/mediacast/");
        if (!file.exists()) {
            file.mkdir();
            Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.defaultconnected);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(new File("/mnt/sdcard/mediacast/"), "defaultconnected.png"));
                decodeResource.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }


    public void startServer() {
        if (this.mAndroidHTTPServer == null) {
            this.mAndroidHTTPServer = new AndroidHTTPServer(PORT);
            try {
                this.mAndroidHTTPServer.start();
                Log.d(this.LOG, "HTTP Server started");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public static int dp(float f) {
        return (int) Math.ceil(density * f);
    }

    public static void checkDisplaySize() {
        Display defaultDisplay;
        try {
            WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (!(windowManager == null || (defaultDisplay = windowManager.getDefaultDisplay()) == null)) {
                defaultDisplay.getSize(displaySize);
            }
        } catch (Exception unused) {
            unused.printStackTrace();
        }
    }

    public void onDestroy() {
        this.mHttpServer.stop(null);
    }

    public static AppData getAppData() {
        return sAppInstance.mAppData;
    }

    public static void setMediaProjection(MediaProjection mediaProjection) {
        sAppInstance.mMediaProjection = mediaProjection;
    }

    @Nullable
    public static MediaProjection getMediaProjection() {
        CastScreenApplication castMediaApplication = sAppInstance;
        if (castMediaApplication == null) {
            return null;
        }
        return castMediaApplication.mMediaProjection;
    }

    @Nullable
    public static ImageGenerator getImageGenerator() {
        CastScreenApplication castMediaApplication = sAppInstance;
        if (castMediaApplication == null) {
            return null;
        }
        return castMediaApplication.mImageGenerator;
    }

    @Nullable
    public static MediaProjectionManager getProjectionManager() {
        CastScreenApplication castMediaApplication = sAppInstance;
        if (castMediaApplication == null) {
            return null;
        }
        return castMediaApplication.mMediaProjectionManager;
    }
}
