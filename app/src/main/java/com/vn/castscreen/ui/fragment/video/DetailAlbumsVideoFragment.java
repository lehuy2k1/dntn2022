package com.vn.castscreen.ui.fragment.video;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.vn.castscreen.CastScreenApplication;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.data.entity.FolderMedia;
import com.vn.castscreen.databinding.FragmentDetailAlbumsVideoBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.DetailAlbumVideoAdapter;
import com.vn.castscreen.utils.Utils;
import com.sdk.core.MediaInfo;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.command.ServiceCommandError;

import java.util.Objects;


public class DetailAlbumsVideoFragment extends BaseFragment<FragmentDetailAlbumsVideoBinding> implements OnClickItemListener<String> {
    private FolderMedia folderMedia;
    private DetailAlbumVideoAdapter adapter;
    private String mediaURL;
    private String mimeType;
    private String TAG = "DETAIL_ALBUMS_VIDEo";
    private int count;



    @Override
    protected FragmentDetailAlbumsVideoBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDetailAlbumsVideoBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new DetailAlbumVideoAdapter(getContext(), this);
        binding.rcvDetailAlbum.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rcvDetailAlbum.setAdapter(adapter);
        if (getArguments() != null) {
            folderMedia = getArguments().getParcelable("folderMedia");
            binding.tvTitleDetailAlbum.setText(folderMedia.getName());
            adapter.setData(folderMedia.getListPath());
        }
    }


    @Override
    protected void initEvent() {
        binding.imvBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });
    }

    @Override
    public void onClickItem(String item) {
        count = 0;
        playMediaFiles(item, Utils.video);
    }

    private MediaPlayer.LaunchListener mLaunchListener = new MediaPlayer.LaunchListener() {
        @Override
        public void onError(ServiceCommandError serviceCommandError) {

            Log.d(TAG, "onError: " + serviceCommandError.getLocalizedMessage());
            if (Objects.requireNonNull(serviceCommandError.getLocalizedMessage()).equalsIgnoreCase("Internal Server Error")) {

                if (CastScreenApplication.sAppInstance.mLaunchSession != null) {
                    CastScreenApplication.sAppInstance.mDevice.getMediaPlayer().closeMedia(CastScreenApplication.sAppInstance.mLaunchSession, null);
                }
                if (count < 6){
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