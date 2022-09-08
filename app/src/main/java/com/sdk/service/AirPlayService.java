package com.sdk.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSDictionary;
import com.sdk.core.ImageInfo;
import com.sdk.core.MediaInfo;
import com.sdk.core.Util;
import com.sdk.discovery.DiscoveryFilter;
import com.sdk.discovery.DiscoveryManager;
import com.sdk.helper.DeviceServiceReachability;
import com.sdk.service.airplay.AirPlayServiceSocketClient;
import com.sdk.service.airplay.PListParser;
import com.sdk.service.capability.CapabilityMethods;
import com.sdk.service.capability.MediaControl;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.capability.listeners.ResponseListener;
import com.sdk.service.command.ServiceCommand;
import com.sdk.service.command.ServiceCommandError;
import com.sdk.service.command.ServiceSubscription;
import com.sdk.service.config.AirPlayServiceConfig;
import com.sdk.service.config.ServiceConfig;
import com.sdk.service.config.ServiceDescription;
import com.sdk.service.sessions.LaunchSession;
import com.sdk.service.sessions.LaunchSession.LaunchSessionType;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class AirPlayService extends DeviceService implements MediaPlayer, MediaControl {
    public static final String X_APPLE_SESSION_ID = "X-Apple-Session-ID";
    public static final String ID = "AirPlay";
    private static final long KEEP_ALIVE_PERIOD = 15000;
    private final static String CHARSET = "UTF-8";
    private String mSessionId;
    private Timer timer;
    private AirPlayServiceSocketClient socketClient;

    @Override
    public CapabilityPriorityLevel getPriorityLevel(Class<? extends CapabilityMethods> clazz) {
        if (clazz.equals(MediaPlayer.class)) {
            return getMediaPlayerCapabilityLevel();
        }
        else if (clazz.equals(MediaControl.class)) {
            return getMediaControlCapabilityLevel();
        }
        return CapabilityPriorityLevel.NOT_SUPPORTED;
    }

    interface PlaybackPositionListener {
        void onGetPlaybackPositionSuccess(long duration, long position);
        void onGetPlaybackPositionFailed(ServiceCommandError error);
    }

    public AirPlayService(ServiceDescription serviceDescription, ServiceConfig serviceConf) {
        super(serviceDescription, serviceConf);
        serviceConfig = new AirPlayServiceConfig(serviceConf.toJSONObject());
        pairingType = PairingType.PIN_CODE;
    }

    public static DiscoveryFilter discoveryFilter() {
        return new DiscoveryFilter(ID, "_airplay._tcp.local.");
    }

    @Override
    public MediaControl getMediaControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getMediaControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void play(ResponseListener<Object> listener) {
        Map <String,String> params = new HashMap<String,String>();
        params.put("value", "1.000000");

        String uri = getRequestURL("rate", params);

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, listener);
        request.send();
    }

    @Override
    public void pause(ResponseListener<Object> listener) {
        Map <String,String> params = new HashMap<String,String>();
        params.put("value", "0.000000");

        String uri = getRequestURL("rate", params);

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, listener);
        request.send();
    }

    @Override
    public void stop(ResponseListener<Object> listener) {
        String uri = getRequestURL("stop");

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, listener);
        // TODO This is temp fix for issue https://github.com/sdk/Connect-SDK-Android/issues/66
        request.send();
        request.send();
        stopTimer();
    }

    @Override
    public void rewind(ResponseListener<Object> listener) {
        Map <String,String> params = new HashMap<String,String>();
        params.put("value", "-2.000000");

        String uri = getRequestURL("rate", params);

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, listener);
        request.send();
    }

    @Override
    public void fastForward(ResponseListener<Object> listener) {
        Map <String,String> params = new HashMap<String,String>();
        params.put("value", "2.000000");

        String uri = getRequestURL("rate", params);

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, listener);
        request.send();
    }


    @Override
    public void previous(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void next(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void seek(long position, ResponseListener<Object> listener) {
        float pos = ((float) position / 1000); 

        Map <String,String> params = new HashMap<String,String>();
        params.put("position", String.valueOf(pos));

        String uri = getRequestURL("scrub", params);

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, listener);
        request.send();
    }

    @Override
    public void getPosition(final PositionListener listener) {
        getPlaybackPosition(new PlaybackPositionListener() {

            @Override
            public void onGetPlaybackPositionSuccess(long duration, long position) {
                Util.postSuccess(listener, position);
            }

            @Override
            public void onGetPlaybackPositionFailed(ServiceCommandError error) {
                Util.postError(listener, new ServiceCommandError(0, "Unable to get position", null));
            }
        });
    }

    /**
     * AirPlay has the same response for Buffering and Finished states that's why this method
     * always returns Finished state for video which is not ready to play.
     * @param listener
     */
    @Override
    public void getPlayState(final PlayStateListener listener) {
        getPlaybackInfo(new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object object) {
                PlayStateStatus playState = PlayStateStatus.Unknown;
                try {
                    JSONObject response = new PListParser().parse(object.toString());
                    if (!response.has("rate")) {
                        playState = PlayStateStatus.Finished;
                    } else {
                        int rate = response.getInt("rate");
                        if (rate == 0) {
                            playState = PlayStateStatus.Paused;
                        } else if (rate == 1) {
                            playState = PlayStateStatus.Playing;
                        }
                    }
                    Util.postSuccess(listener, playState);
                } catch (Exception e) {
                    Util.postError(listener, new ServiceCommandError(500, e.getMessage(), null));
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });
    }

    @Override
    public void getDuration(final DurationListener listener) {
        getPlaybackPosition(new PlaybackPositionListener() {

            @Override
            public void onGetPlaybackPositionSuccess(long duration, long position) {
                Util.postSuccess(listener, duration);
            }

            @Override
            public void onGetPlaybackPositionFailed(ServiceCommandError error) {
                Util.postError(listener, new ServiceCommandError(0, "Unable to get duration", null));
            }
        });
    }

    private void getPlaybackPosition(final PlaybackPositionListener listener) {
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                String strResponse = (String) response;

                long duration = 0;
                long position = 0;

                StringTokenizer st = new StringTokenizer(strResponse);
                while (st.hasMoreTokens()) {
                    String str = st.nextToken();
                    if (str.contains("duration")) {
                        duration = parseTimeValueFromString(st.nextToken());
                    }
                    else if (str.contains("position")) {
                        position = parseTimeValueFromString(st.nextToken());
                    }
                }

                if (listener != null) {
                    listener.onGetPlaybackPositionSuccess(duration, position);
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                if (listener != null)
                    listener.onGetPlaybackPositionFailed(error);
            }
        };

        String uri = getRequestURL("scrub");

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, responseListener);
        request.setHttpMethod(ServiceCommand.TYPE_GET);
        request.send();
    }

    private long parseTimeValueFromString(String value) {
        long duration = 0L;
        try {
            float f = Float.valueOf(value);
            duration = (long) f * 1000;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return duration;
    }

    private void getPlaybackInfo(ResponseListener<Object> listener) {
        String uri = getRequestURL("playback-info");

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, listener);
        request.setHttpMethod(ServiceCommand.TYPE_GET);
        request.send();
    }

    @Override
    public ServiceSubscription<PlayStateListener> subscribePlayState(
            PlayStateListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
        return null;
    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getMediaPlayerCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void getMediaInfo(MediaInfoListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public ServiceSubscription<MediaInfoListener> subscribeMediaInfo(
            MediaInfoListener listener) {
        listener.onError(ServiceCommandError.notSupported());
        return null;
    }

    // This function does not work properly now because photo protocol is changed.
    // 501 Not Implemented error returns with the previous photo protocol.
    @Override
    public void displayImage(final String url, String mimeType, String title,
            String description, String iconSrc, final LaunchListener listener) {
        Util.runInBackground(new Runnable() {

            @Override
            public void run() {
                ResponseListener<Object> responseListener = new ResponseListener<Object>() {

                    @Override
                    public void onSuccess(Object response) {
                        LaunchSession launchSession = new LaunchSession();
                        launchSession.setService(AirPlayService.this);
                        launchSession.setSessionType(LaunchSessionType.Media);

                        Util.postSuccess(listener, new MediaLaunchObject(launchSession, AirPlayService.this));
                    }

                    @Override
                    public void onError(ServiceCommandError error) {
                        Util.postError(listener, error);
                    }
                };

                String uri = getRequestURL("photo");
                byte[] payload = null;

                try {
                    URL imagePath = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) imagePath.openConnection();
                    connection.setInstanceFollowRedirects(true);
                    connection.setDoInput(true);
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    boolean redirect = (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                            || responseCode == HttpURLConnection.HTTP_SEE_OTHER);

                    if(redirect) {
                        String newPath = connection.getHeaderField("Location");
                        URL newImagePath = new URL(newPath);
                        connection = (HttpURLConnection) newImagePath.openConnection();
                        connection.setInstanceFollowRedirects(true);
                        connection.setDoInput(true);
                        connection.connect();
                    }

                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    payload = stream.toByteArray();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(AirPlayService.this, uri, payload, responseListener);
                request.setHttpMethod(ServiceCommand.TYPE_PUT);
                request.send();
            }
        });
    }

    @Override
    public void displayImage(MediaInfo mediaInfo, LaunchListener listener) {
        String mediaUrl = null;
        String mimeType = null;
        String title = null;
        String desc = null;
        String iconSrc = null;

        if (mediaInfo != null) {
            mediaUrl = mediaInfo.getUrl();
            mimeType = mediaInfo.getMimeType();
            title = mediaInfo.getTitle();
            desc = mediaInfo.getDescription();

            if (mediaInfo.getImages() != null && mediaInfo.getImages().size() > 0) {
                ImageInfo imageInfo = mediaInfo.getImages().get(0);
                iconSrc = imageInfo.getUrl();
            }
        }

        displayImage(mediaUrl, mimeType, title, desc, iconSrc, listener);
    }

    public void playVideo(final String url, String mimeType, String title,
            String description, String iconSrc, boolean shouldLoop,
            final LaunchListener listener) {

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                LaunchSession launchSession = new LaunchSession();
                launchSession.setService(AirPlayService.this);
                launchSession.setSessionType(LaunchSessionType.Media);

                Util.postSuccess(listener, new MediaLaunchObject(launchSession, AirPlayService.this));
                startTimer();
            }

            @Override
            public void onError(ServiceCommandError error)
            {
                Util.postError(listener, error);
            }
        };

        String uri = getRequestURL("play");

        NSDictionary plist = new NSDictionary();
        plist.put("Content-Location", url);
        plist.put("Start-Position", 0.0);

        byte[] payload = new byte[0];
        try {
            payload = BinaryPropertyListWriter.writeToArray(plist);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, responseListener);
        request.send();
    }

    @Override
    public void playMedia(String url, String mimeType, String title,
            String description, String iconSrc, boolean shouldLoop,
            LaunchListener listener) {
        if (mimeType.contains("image")) {
            displayImage(url, mimeType, title, description, iconSrc, listener);
        }
        else {
            playVideo(url, mimeType, title, description, iconSrc, shouldLoop, listener);
        }
    }

    @Override
    public void playMedia(MediaInfo mediaInfo, boolean shouldLoop, LaunchListener listener) {
        String mediaUrl = null;
        String mimeType = null;
        String title = null;
        String desc = null;
        String iconSrc = null;

        if (mediaInfo != null) {
            mediaUrl = mediaInfo.getUrl();
            mimeType = mediaInfo.getMimeType();
            title = mediaInfo.getTitle();
            desc = mediaInfo.getDescription();

            if (mediaInfo.getImages() != null && mediaInfo.getImages().size() > 0) {
                ImageInfo imageInfo = mediaInfo.getImages().get(0);
                iconSrc = imageInfo.getUrl();
            }
        }

        playMedia(mediaUrl, mimeType, title, desc, iconSrc, shouldLoop, listener);
    }

    @Override
    public void closeMedia(LaunchSession launchSession,
            ResponseListener<Object> listener) {
        stop(listener);
    }

    @Override
    public void sendCommand(final ServiceCommand<?> serviceCommand) {
        if (socketClient != null)
            socketClient.sendCommand(serviceCommand);
    }

    @Override
    public void sendPairingKey(String pairingKey) {
        socketClient.pair(pairingKey);
    }

    @Override
    protected void updateCapabilities() {
        List<String> capabilities = new ArrayList<String>();
        capabilities.add(Display_Image);
        capabilities.add(Play_Video);
        capabilities.add(Play_Audio);
        capabilities.add(Close);

        capabilities.add(Play);
        capabilities.add(Pause);
        capabilities.add(Stop);
        capabilities.add(Position);
        capabilities.add(Duration);
        capabilities.add(PlayState);
        capabilities.add(Seek);
        capabilities.add(Rewind);
        capabilities.add(FastForward);

        setCapabilities(capabilities);
    }

    private String getRequestURL(String command) {
        return getRequestURL(command, null);
    }

    private String getRequestURL(String command, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(command);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String param = String.format("?%s=%s", entry.getKey(), entry.getValue());
                sb.append(param); 
            }
        }

        return sb.toString();
    }

    @Override
    public boolean isConnectable() {
        return true;
    }

    @Override
    public boolean isConnected() {
        if (DiscoveryManager.getInstance().getPairingLevel().compareTo(DiscoveryManager.PairingLevel.PROTECTED) >= 0) {
            return this.socketClient != null && this.socketClient.isConnected() && this.socketClient.getAuthToken() != "";
        } else {
            return this.socketClient != null && this.socketClient.isConnected();
        }
    }

    private AirPlayServiceSocketClient.AirPlayServiceSocketClientListener mSocketListener
            = new AirPlayServiceSocketClient.AirPlayServiceSocketClientListener() {

        @Override
        public void onRegistrationFailed(final ServiceCommandError error) {
            disconnect();

            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                if (listener != null)
                    listener.onConnectionFailure(AirPlayService.this, error);
                }
            });
        }

        @Override
        public Boolean onReceiveMessage(JSONObject message) { return true; }

        @Override
        public void onFailWithError(final ServiceCommandError error) {
            socketClient.setListener(null);
            socketClient.disconnect();
            socketClient = null;

            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    if (listener != null)
                        listener.onConnectionFailure(AirPlayService.this, error);
                }
            });
        }

        @Override
        public void onConnect() {
            reportConnected(true);
        }

        @Override
        public void onCloseWithError(final ServiceCommandError error) {
            socketClient.setListener(null);
            socketClient.disconnect();
            socketClient = null;

            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    if (listener != null)
                        listener.onDisconnect(AirPlayService.this, error);
                }
            });
        }

        @Override
        public void onBeforeRegister(final PairingType pairingType) {
            if (DiscoveryManager.getInstance().getPairingLevel().compareTo(DiscoveryManager.PairingLevel.ON) >= 0) {
                Util.runOnUI(new Runnable() {

                    @Override
                    public void run() {
                        if (listener != null)
                            listener.onPairingRequired(AirPlayService.this, pairingType, null);
                    }
                });
            }
        }
    };

    public AirPlayServiceConfig getAirPlayServiceConfig() {
        return (AirPlayServiceConfig) serviceConfig;
    }

    @Override
    public void connect() {
        mSessionId = UUID.randomUUID().toString();
        socketClient = new AirPlayServiceSocketClient(this.getAirPlayServiceConfig(), this.getPairingType(),
                this.getServiceDescription().getIpAddress());
        socketClient.setListener(mSocketListener);
        socketClient.connect();
    }

    @Override
    public void disconnect() {
        stopTimer();
        connected=false;

        if (mServiceReachability != null)
            mServiceReachability.stop();

        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onDisconnect(AirPlayService.this, null);
            }
        });

        if (socketClient != null) {
            socketClient.setListener(null);
            socketClient.disconnect();
            socketClient = null;
        }
    }

    @Override
    public void onLoseReachability(DeviceServiceReachability reachability) {
        if (connected) {
            disconnect();
        } else {
            mServiceReachability.stop();
        }
    }

    /**
     * We send periodically a command to keep connection alive and for avoiding 
     * stopping media session
     * 
     * Fix for https://github.com/sdk/Connect-SDK-Cordova-Plugin/issues/5
     */
    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Log.d("Timer", "Timer");
                getPlaybackPosition(new PlaybackPositionListener() {

                    @Override
                    public void onGetPlaybackPositionSuccess(long duration, long position) {
                        if (position >= duration) {
                            stopTimer();
                        }
                    }

                    @Override
                    public void onGetPlaybackPositionFailed(ServiceCommandError error) {
                    }
                });
            }
        }, KEEP_ALIVE_PERIOD, KEEP_ALIVE_PERIOD);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }


}
