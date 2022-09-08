package com.sdk.service.capability;

import java.util.regex.Pattern;

public interface CapabilityMethods {
    // @cond INTERNAL
    public final static Pattern ANY_PATTERN = Pattern.compile(".+\\.(?=Any)");

    // @endcond
    
    
    /**
     * CapabilityPriorityLevel values are used by ConnectableDevice to find the most suitable DeviceService capability to be presented to the user. Values of VeryLow and VeryHigh are not in use internally the SDK. Connect SDK uses Low, Normal, and High internally.
     *
     * Default behavior:
     * If you are unsatisfied with the default priority levels & behavior of Connect SDK, it is possible to subclass a particular DeviceService and provide your own value for each capability. That DeviceService subclass would need to be registered with DiscoveryManager.
     */
    public enum CapabilityPriorityLevel {
        NOT_SUPPORTED   (0),
        VERY_LOW        (1),
        LOW             (25),
        NORMAL          (50),
        HIGH            (75),
        VERY_HIGH       (100);

        private final int value;

        CapabilityPriorityLevel(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }
}
