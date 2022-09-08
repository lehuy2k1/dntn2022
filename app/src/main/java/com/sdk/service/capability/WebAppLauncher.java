package com.sdk.service.capability;

import com.sdk.service.capability.listeners.ResponseListener;
import com.sdk.service.command.ServiceSubscription;
import com.sdk.service.sessions.LaunchSession;
import com.sdk.service.sessions.WebAppSession.LaunchListener;
import com.sdk.service.sessions.WebAppSession.WebAppPinStatusListener;

import org.json.JSONObject;


public interface WebAppLauncher extends CapabilityMethods {
    public final static String Any = "WebAppLauncher.Any";

    public final static String Launch = "WebAppLauncher.Launch";
    public final static String Launch_Params = "WebAppLauncher.Launch.Params";
    public final static String Message_Send = "WebAppLauncher.Message.Send";
    public final static String Message_Receive = "WebAppLauncher.Message.Receive";
    public final static String Message_Send_JSON = "WebAppLauncher.Message.Send.JSON";
    public final static String Message_Receive_JSON = "WebAppLauncher.Message.Receive.JSON";
    public final static String Connect = "WebAppLauncher.Connect";
    public final static String Disconnect = "WebAppLauncher.Disconnect";
    public final static String Join = "WebAppLauncher.Join";
    public final static String Close = "WebAppLauncher.Close";
    public final static String Pin = "WebAppLauncher.Pin";

    public final static String[] Capabilities = {
        Launch,
        Launch_Params,
        Message_Send,
        Message_Receive,
        Message_Send_JSON,
        Message_Receive_JSON,
        Connect,
        Disconnect,
        Join,
        Close,
        Pin
    };

    public WebAppLauncher getWebAppLauncher();
    public CapabilityPriorityLevel getWebAppLauncherCapabilityLevel();

    public void launchWebApp(String webAppId, LaunchListener listener);
    public void launchWebApp(String webAppId, boolean relaunchIfRunning, LaunchListener listener);
    public void launchWebApp(String webAppId, JSONObject params, LaunchListener listener);
    public void launchWebApp(String webAppId, JSONObject params, boolean relaunchIfRunning, LaunchListener listener);

    public void joinWebApp(LaunchSession webAppLaunchSession, LaunchListener listener);
    public void joinWebApp(String webAppId, LaunchListener listener);

    public void closeWebApp(LaunchSession launchSession, ResponseListener<Object> listener);

    public void pinWebApp(String webAppId, ResponseListener<Object> listener);
    public void unPinWebApp(String webAppId, ResponseListener<Object> listener);
    public void isWebAppPinned(String webAppId, WebAppPinStatusListener listener);
    public ServiceSubscription<WebAppPinStatusListener> subscribeIsWebAppPinned(String webAppId, WebAppPinStatusListener listener);
}
