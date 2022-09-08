package com.sdk.service.capability;

import com.sdk.core.AppInfo;
import com.sdk.service.capability.listeners.ResponseListener;

import org.json.JSONObject;

public interface ToastControl extends CapabilityMethods {
    public final static String Any = "ToastControl.Any";

    public final static String Show_Toast = "ToastControl.Show";
    public final static String Show_Clickable_Toast_App = "ToastControl.Show.Clickable.App";
    public final static String Show_Clickable_Toast_App_Params = "ToastControl.Show.Clickable.App.Params";
    public final static String Show_Clickable_Toast_URL = "ToastControl.Show.Clickable.URL";

    public final static String[] Capabilities = {
        Show_Toast,
        Show_Clickable_Toast_App,
        Show_Clickable_Toast_App_Params,
        Show_Clickable_Toast_URL
    };

    public ToastControl getToastControl();
    public CapabilityPriorityLevel getToastControlCapabilityLevel();

    public void showToast(String message, ResponseListener<Object> listener);
    public void showToast(String message, String iconData, String iconExtension, ResponseListener<Object> listener);

    public void showClickableToastForApp(String message, AppInfo appInfo, JSONObject params, ResponseListener<Object> listener);
    public void showClickableToastForApp(String message, AppInfo appInfo, JSONObject params, String iconData, String iconExtension, ResponseListener<Object> listener);

    public void showClickableToastForURL(String message, String url, ResponseListener<Object> listener);
    public void showClickableToastForURL(String message, String url, String iconData, String iconExtension, ResponseListener<Object> listener);
}