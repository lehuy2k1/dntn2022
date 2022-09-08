package com.vn.castscreen.ui.fragment.video;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.vn.castscreen.CastScreenApplication;
import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.databinding.FragmentAllVideoBinding;
import com.vn.castscreen.databinding.FragmentVideoBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.DetailAlbumVideoAdapter;
import com.vn.castscreen.utils.Utils;
import com.sdk.core.MediaInfo;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.command.ServiceCommandError;

import java.util.ArrayList;
import java.util.Objects;

public class AllVideoFragment extends BaseFragment<FragmentAllVideoBinding> implements OnClickItemListener<String> {

    private DetailAlbumVideoAdapter adapter;
    private String mediaURL;
    private String mimeType;
    private int count = 0;
    private String TAG = " AllVideoFragment";



    @Override
    protected FragmentAllVideoBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAllVideoBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initEvent() {
        binding.rcvAllVideo.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new DetailAlbumVideoAdapter(requireContext(), this);
        adapter.setData(getVideos(requireContext()));
        binding.rcvAllVideo.setAdapter(adapter);
    }

    private ArrayList<String> getVideos(Context context) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME};

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    @Override
    public void onClickItem(String item) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("item", item);
//        count = 0;
//        playMediaFiles(item, Utils.video);
        Navigation.findNavController(requireView()).navigate(R.id.action_videoFragment_to_castVideoFragment, bundle);
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
            this.mediaURL = Utils.getIpAddress(getContext().getApplicationContext()) + ":" + Utils.PORT + mediaURL;
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
}