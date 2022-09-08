package com.vn.castscreen.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vn.castscreen.CastMediaWeb.ClientWebSocketServer;
import com.vn.castscreen.CastScreenApplication;
import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.databinding.FragmentFindDeviceBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.ConnectableDeviceAdapter;
import com.vn.castscreen.utils.Utils;
import com.sdk.core.MediaInfo;
import com.sdk.device.ConnectableDevice;
import com.sdk.device.ConnectableDeviceListener;
import com.sdk.discovery.DiscoveryManager;
import com.sdk.discovery.DiscoveryManagerListener;
import com.sdk.service.DeviceService;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.command.ServiceCommandError;

import java.util.ArrayList;
import java.util.List;

public class FindDeviceFragment extends BaseFragment<FragmentFindDeviceBinding> implements DiscoveryManagerListener, OnClickItemListener<ConnectableDevice> {

    private String TAG = "SEARCH_DEVICE";
    private ArrayList<ConnectableDevice> listDevice = new ArrayList<>();
    private ArrayList<String> deviceId = new ArrayList<>();
    private ConnectableDeviceAdapter adapter;
    private ConnectableDevice tvConnect;
    String mediaURL;
    String mimeType;

    @Override
    protected FragmentFindDeviceBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentFindDeviceBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ConnectableDeviceAdapter(getContext(), this);
        binding.rvcListDevice.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvcListDevice.setAdapter(adapter);
        CastScreenApplication.sAppInstance.mDiscoveryManager.addListener(this);


    }

    @Override
    protected void initEvent() {
        binding.imvBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

    }

    private void registerSuccess(ConnectableDevice device) {
        CastScreenApplication.sAppInstance.mDevice = device;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ClientWebSocketServer.DebugWebSocket.reloadHtml(Utils.getIpAddress(requireContext()) + ":" + Utils.PORT + Utils.pathMedia);


        playMediaFiles(Utils.getIpAddress(getActivity().getApplicationContext()) + ":" + Utils.PORT + Utils.pathMedia, Utils.image);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CastScreenApplication.sAppInstance.mDevice.removeListener(deviceListener);
                View view = getView();
                Navigation.findNavController(view).popBackStack();

            }
        }, 300L);
    }

    public void playMediaFiles(String mediaURL, String mimeType) {
        if (CastScreenApplication.sAppInstance.mDevice == null) {
            Log.d(TAG, "Device null");
        } else if (CastScreenApplication.sAppInstance.mDevice.isConnected()) {
            this.mimeType = mimeType;
            this.mediaURL = mediaURL;
            MediaInfo build = new MediaInfo.Builder(mediaURL, mimeType).setTitle("").setDescription("").setIcon("").build();
            if (mimeType.equalsIgnoreCase(Utils.image)) {
                CastScreenApplication.sAppInstance.mDevice.getCapability(MediaPlayer.class).displayImage(build, this.mLaunchListener);
            }
            if (mimeType.equalsIgnoreCase(Utils.video)) {
                CastScreenApplication.sAppInstance.mDevice.getCapability(MediaPlayer.class).playMedia(build, false, this.mLaunchListener);
            }
        } else {
            Log.d(TAG, "Device not connected");
        }
    }

    protected MediaPlayer.LaunchListener mLaunchListener = new MediaPlayer.LaunchListener() {
        @Override
        public void onError(ServiceCommandError serviceCommandError) {
            if (serviceCommandError.toString().equalsIgnoreCase("Internal Server Error")) {
                if (CastScreenApplication.sAppInstance.mLaunchSession != null) {
                    CastScreenApplication.sAppInstance.mDevice
                            .getCapability(MediaPlayer.class)
                            .closeMedia(CastScreenApplication.sAppInstance.mLaunchSession, null);
                }
//                playMediaFiles(mediaURL, mimeType);
            }
        }

        public void onSuccess(MediaPlayer.MediaLaunchObject mediaLaunchObject) {
            Log.d("Connect SDK Sample App", "Successfully launched image!");
            CastScreenApplication.sAppInstance.mLaunchSession = mediaLaunchObject.launchSession;
            CastScreenApplication.sAppInstance.mMediaControl = mediaLaunchObject.mediaControl;

        }
    };

    @Override
    public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {

        if (!deviceId.contains(device.getId())) {
            Log.d(TAG, "onDeviceAdded: addddddd");
            deviceId.add(device.getId());
            listDevice.add(device);
            adapter.setData(listDevice);
        }
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {
        Log.d(TAG, "onDeviceUpdated: ");

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {

        if (deviceId.contains(device.getId())) {
            deviceId.remove(device.getId());
            for (int j = 0; j < listDevice.size(); j++) {
                ConnectableDevice connectableDevice = listDevice.get(j);

                if (connectableDevice.getId().equals(device.getId())) {
                    listDevice.remove(connectableDevice);
                    adapter.setData(listDevice);
                    Log.d(TAG, "onDeviceRemoved: removeeeee");
                }
            }
        }
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {
        Log.d(TAG, "onDiscoveryFailed: " + error.getLocalizedMessage());
    }

    @Override
    public void onClickItem(ConnectableDevice item) {
        tvConnect = item;
        tvConnect.addListener(deviceListener);
        tvConnect.connect();

        Log.d(TAG, "onClickItem: " + item.isConnecting);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected ConnectableDeviceListener deviceListener = new ConnectableDeviceListener() {
        @Override
        public void onDeviceReady(ConnectableDevice device) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
            registerSuccess(tvConnect);
            device.getServiceId();
        }

        @Override
        public void onDeviceDisconnected(ConnectableDevice device) {
            Log.d(TAG, "onDeviceDisconnected: ");
        }

        @Override
        public void onPairingRequired(ConnectableDevice device, DeviceService service, DeviceService.PairingType pairingType) {
            Log.d(TAG, "onPairingRequired: ");

        }

        @Override
        public void onCapabilityUpdated(ConnectableDevice device, List<String> added, List<String> removed) {
            Log.d(TAG, "onCapabilityUpdated: ");
        }

        @Override
        public void onConnectionFailed(ConnectableDevice device, ServiceCommandError error) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.connect_fail), Toast.LENGTH_SHORT).show();
        }
    };


}