package com.sdk;

import java.util.HashMap;

public class DefaultPlatform {

    public DefaultPlatform() {
    }

    public static HashMap<String, String> getDeviceServiceMap() {
        HashMap<String, String> devicesList = new HashMap<String, String>();
        devicesList.put("com.sdk.service.WebOSTVService", "com.sdk.discovery.provider.SSDPDiscoveryProvider");
        devicesList.put("com.sdk.service.NetcastTVService", "com.sdk.discovery.provider.SSDPDiscoveryProvider");
        devicesList.put("com.sdk.service.DLNAService", "com.sdk.discovery.provider.SSDPDiscoveryProvider");
        devicesList.put("com.sdk.service.DIALService", "com.sdk.discovery.provider.SSDPDiscoveryProvider");
        devicesList.put("com.sdk.service.RokuService", "com.sdk.discovery.provider.SSDPDiscoveryProvider");
        devicesList.put("com.sdk.service.CastService", "com.sdk.discovery.provider.CastDiscoveryProvider");
        devicesList.put("com.sdk.service.AirPlayService", "com.sdk.discovery.provider.ZeroconfDiscoveryProvider");
        devicesList.put("com.sdk.service.FireTVService", "com.sdk.discovery.provider.FireTVDiscoveryProvider");
        return devicesList;
    }

}