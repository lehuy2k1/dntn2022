package com.vn.castscreen.ui.fragment.photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vn.castscreen.CastScreenApplication;
import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.databinding.LayoutFragmentCastPhotoBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.CastAlbumPhotoAdapter;
import com.vn.castscreen.utils.Utils;
import com.sdk.core.MediaInfo;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.command.ServiceCommandError;

import java.util.ArrayList;
import java.util.Objects;

public class CastImagesFragment extends BaseFragment<LayoutFragmentCastPhotoBinding> implements OnClickItemListener<String> {
    private String item;
    private CastAlbumPhotoAdapter adapter;
    private String mediaURL;
    private String mimeType;
    private String TAG = "DETAIL_ALBUMS_PHOTO";
    private int count = 0;
    private int count1 = 0;
    private ArrayList<String> listOfAllImages = new ArrayList<String>();


    @Override
    protected LayoutFragmentCastPhotoBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return LayoutFragmentCastPhotoBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initEvent() {
        binding.icBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        binding.btnShowimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (count < listOfAllImages.size()){
                    new CountDownTimer(8000*listOfAllImages.size(), 8000) {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onTick(long l) {
                            binding.coutdowtime.setText("seconds" + l / 1000);
                            playMediaFiles(listOfAllImages.get(count1), Utils.image);
                            count1= count1 + 1;
                            Log.d(TAG, "list: " + count1);
                        }
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onFinish() {
                            binding.coutdowtime.setText("done!");
                            count1 = 0;
                        }
                    }.start();
//                }
            }
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        item = (String) getArguments().getSerializable("item");
        binding.image.setImageURI(Uri.parse(item));
        adapter = new CastAlbumPhotoAdapter(getContext(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setReverseLayout(true);
        binding.rclQueueImage.setLayoutManager(layoutManager);
        binding.rclQueueImage.setAdapter(adapter);
        adapter.setData(getImages(requireContext()));
    }

    @Override
    public void onClickItem(String item) {
        binding.image.setImageURI(Uri.parse(item));
        count = 0;
        playMediaFiles(item, Utils.image);
        Log.d(TAG, "onClickItem: " + item.toString());
    }

    private ArrayList<String> getImages(Context context) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    private MediaPlayer.LaunchListener mLaunchListener = new MediaPlayer.LaunchListener() {
        @Override
        public void onError(ServiceCommandError serviceCommandError) {

            Log.d(TAG, "onError: " + serviceCommandError.getLocalizedMessage());
            if (Objects.requireNonNull(serviceCommandError.getLocalizedMessage()).equalsIgnoreCase("Internal Server Error")) {

                if (CastScreenApplication.sAppInstance.mLaunchSession != null) {
                    CastScreenApplication.sAppInstance.mDevice.getMediaPlayer().closeMedia(CastScreenApplication.sAppInstance.mLaunchSession, null);
                }
                if (count < 6) {
                    Log.d(TAG, "onError: " + count);
                    playMediaFiles(mediaURL, mimeType);
                    count++;
                }
            }
        }

        public void onSuccess(MediaPlayer.MediaLaunchObject mediaLaunchObject) {
            count = 0;
            Log.d(TAG, "onSuccess: " + mediaLaunchObject.mediaControl.getMediaControl());

            CastScreenApplication.sAppInstance.mLaunchSession = mediaLaunchObject.launchSession;
            CastScreenApplication.sAppInstance.mMediaControl = mediaLaunchObject.mediaControl;
        }
    };

    public void playMediaFiles(String mediaURL, String mimeType) {
        if (CastScreenApplication.sAppInstance.mDevice == null) {
        } else if (CastScreenApplication.sAppInstance.mDevice.isConnected()) {
            this.mimeType = mimeType;
            this.mediaURL = Utils.getIpAddress(requireContext()) + ":" + Utils.PORT + mediaURL;
            Log.d(TAG, "playMediaFiles: " + Utils.getIpAddress(requireContext()) + ":" + Utils.PORT + mediaURL);
            MediaInfo build = new MediaInfo.Builder(mediaURL, mimeType).setTitle("").setDescription("").setIcon("").build();

            if (mimeType.equalsIgnoreCase(Utils.image)) {
                CastScreenApplication.sAppInstance.mDevice.getCapability(MediaPlayer.class).displayImage(build, mLaunchListener);
            }
            if (mimeType.equalsIgnoreCase(Utils.video)) {
                CastScreenApplication.sAppInstance.mDevice.getCapability(MediaPlayer.class).playMedia(build, false, this.mLaunchListener);
            }
        } else {
            Log.d(TAG, "Device not connected");
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }
}
