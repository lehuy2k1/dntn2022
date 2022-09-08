package com.sdk.device;

import android.content.Context;
import android.widget.ListView;

import com.sdk.core.Util;
import com.sdk.discovery.DiscoveryManager;
import com.sdk.discovery.DiscoveryManagerListener;
import com.sdk.service.command.ServiceCommandError;

public class DevicePickerListView extends ListView implements DiscoveryManagerListener {
    DevicePickerAdapter pickerAdapter;

    public DevicePickerListView(Context context) {
        super(context);

        pickerAdapter = new DevicePickerAdapter(context);

        setAdapter(pickerAdapter);

        DiscoveryManager.getInstance().addListener(this);
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {
        Util.runOnUI(new Runnable () {
            @Override
            public void run() {
                pickerAdapter.clear();
            }
        });
    }

    @Override
    public void onDeviceAdded(final DiscoveryManager manager, final ConnectableDevice device) {
        Util.runOnUI(new Runnable () {
            @Override
            public void run() {
                int index = -1;
                for (int i = 0; i < pickerAdapter.getCount(); i++) {
                    ConnectableDevice d = pickerAdapter.getItem(i);

                    String newDeviceName = device.getFriendlyName();
                    String dName = d.getFriendlyName();

                    if (newDeviceName == null) {
                        newDeviceName = device.getModelName();
                    }

                    if (dName == null) {
                        dName = d.getModelName();
                    }

                    if (d.getIpAddress().equals(device.getIpAddress())) {
                        if (d.getFriendlyName().equals(device.getFriendlyName())) {
                            if (!manager.isServiceIntegrationEnabled() &&
                                    d.getServiceId().equals(device.getServiceId())) {
                                pickerAdapter.remove(d);
                                pickerAdapter.insert(device, i);
                                return;
                            }
                        }
                    }

                    if (newDeviceName.compareToIgnoreCase(dName) < 0) {
                        index = i;
                        pickerAdapter.insert(device, index);
                        break;
                    }
                }

                if (index == -1)
                    pickerAdapter.add(device);
            }
        });
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager, final ConnectableDevice device) {
        Util.runOnUI(new Runnable () {
            @Override
            public void run() {
                pickerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDeviceRemoved(DiscoveryManager manager, final ConnectableDevice device) {
        Util.runOnUI(new Runnable () {
            @Override
            public void run() {
                pickerAdapter.remove(device);
            }
        });
    }
}
