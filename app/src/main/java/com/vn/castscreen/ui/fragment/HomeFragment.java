package com.vn.castscreen.ui.fragment;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.databinding.FragmentHomeBinding;

public class HomeFragment extends BaseFragment<FragmentHomeBinding>   {
    private String TAG = "HOME";
    public static final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 2810;

    @Override
    protected FragmentHomeBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }



    @Override
    protected void initEvent() {
        binding.imvCast.setOnClickListener(v -> {

//            CastScreenApplication.sAppInstance.mDevice = mDevice;
//            if (mDevice != null) {
//                LayoutInflater inflater = getLayoutInflater();
//                View alterLayout = inflater.inflate(R.layout.layout_dialog_disconnect, null);
//                TextView cancel = alterLayout.findViewById(R.id.btn_no);
//                AlertDialog.Builder alter = new AlertDialog.Builder(getContext());
//                alter.setView(alterLayout);
//                alter.setCancelable(false);
//                alter.show();
//            } else {
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_findDeviceFragment);
//            }
        });

        binding.layoutPhoto.setOnClickListener(v -> {
            if (notHaveStoragePermission()) {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE);
            } else {
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_photoFragment);
            }
        });

        binding.layoutVideo.setOnClickListener(v -> {
            if (notHaveStoragePermission()) {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE);
            } else {
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_videoFragment);
            }
        });

        }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void requestReadStoragePermissionsSafely(int requestCode) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);

                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }

    }
    private void opernScreenMirroring(){
        try {
            startActivity(new Intent("android.settings.WIFI_DISPLAY_SETTINGS"));
        }catch (ActivityNotFoundException e){
            try {
                startActivity(new Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"));
            }catch (Exception e2){
                startActivity(new Intent("android.settings.CAST_SETTINGS"));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
    }

    public boolean notHaveStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
        } else {
            return (!Environment.isExternalStorageManager());
        }
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }


}
