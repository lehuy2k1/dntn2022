package com.vn.castscreen.ui.fragment.photo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.vn.castscreen.CastScreenApplication;
import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.data.entity.FolderMedia;
import com.vn.castscreen.databinding.FragmentDetailAlbumsBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.DetailAlbumPhotoAdapter;
import com.vn.castscreen.utils.Utils;
import com.sdk.core.MediaInfo;
import com.sdk.service.capability.MediaPlayer;
import com.sdk.service.command.ServiceCommandError;

import java.util.Objects;


public class DetailAlbumsPhotoFragment extends BaseFragment<FragmentDetailAlbumsBinding> implements OnClickItemListener<String> {

    private FolderMedia folderMedia;
    private DetailAlbumPhotoAdapter adapter;
    private String mediaURL;
    private String mimeType;
    private String TAG = "DETAIL_ALBUMS_PHOTO";
    private int count = 0;

    @Override
    protected FragmentDetailAlbumsBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDetailAlbumsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new DetailAlbumPhotoAdapter(getContext(), this);
        binding.rcvDetailAlbum.setLayoutManager(new GridLayoutManager(getContext(), 3));
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
//            Navigation.findNavController(v).popBackStack();
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onClickItem(String item) {
        count = 0;
        playMediaFiles(item, Utils.image);
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