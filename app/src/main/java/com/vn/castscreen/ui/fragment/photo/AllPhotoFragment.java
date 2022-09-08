package com.vn.castscreen.ui.fragment.photo;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.vn.castscreen.CastScreenApplication;
import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.databinding.FragmentAllPhotoBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.DetailAlbumPhotoAdapter;
import com.vn.castscreen.utils.Utils;
import com.sdk.core.MediaInfo;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.command.ServiceCommandError;

import java.util.ArrayList;
import java.util.Objects;

public class AllPhotoFragment extends BaseFragment<FragmentAllPhotoBinding> implements OnClickItemListener<String> {

    private String TAG = "ALL_Photo";
    private DetailAlbumPhotoAdapter adapter;
    private String mediaURL;
    private String mimeType;
    private int count = 0;
    private Bundle savedState = null;



    @Override
    protected FragmentAllPhotoBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAllPhotoBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initEvent() {
        Log.d(TAG, "initEvent: " + getImages(requireContext()).size());

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        adapter = new DetailAlbumPhotoAdapter(getContext(), this);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setReverseLayout(true);
//        binding.rcvAllPhoto.setLayoutManager(layoutManager);
        binding.rcvAllPhoto.setLayoutManager(new GridLayoutManager(getContext(), 3));

        binding.rcvAllPhoto.setAdapter(adapter);
        adapter.setData(getImages(requireContext()));
    }

    private ArrayList<String> getImages(Context context) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
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

    @Override
    public void onClickItem(String item) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("item", item);
        count = 0;
        playMediaFiles(item, Utils.image);
        Navigation.findNavController(requireView()).navigate(R.id.action_photoFragment_to_castImageFragment, bundle);
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
            Log.d(TAG, "onSuccess: ok");
            CastScreenApplication.sAppInstance.mLaunchSession = mediaLaunchObject.launchSession;
            CastScreenApplication.sAppInstance.mMediaControl = mediaLaunchObject.mediaControl;
            count = 0;
        }
    };

    public void playMediaFiles(String mediaURL, String mimeType) {
        if (CastScreenApplication.sAppInstance.mDevice == null) {
        } else if (CastScreenApplication.sAppInstance.mDevice.isConnected()) {
            this.mimeType = mimeType;
            this.mediaURL = Utils.getIpAddress(requireContext()) + ":" + Utils.PORT + mediaURL;
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