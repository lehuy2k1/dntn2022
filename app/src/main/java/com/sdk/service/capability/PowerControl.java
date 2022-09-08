package com.sdk.service.capability;

import com.sdk.service.capability.listeners.ResponseListener;

public interface PowerControl extends CapabilityMethods {

    public final static String Any = "PowerControl.Any";

    public final static String Off = "PowerControl.Off";
    public final static String On = "PowerControl.On";

    public final static String[] Capabilities = {
        Off,
        On
    };

    public PowerControl getPowerControl();
    public CapabilityPriorityLevel getPowerControlCapabilityLevel();

    public void powerOff(ResponseListener<Object> listener);
    public void powerOn(ResponseListener<Object> listener);
}
