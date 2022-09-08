package com.vn.castscreen.ui.fragment.video;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.data.entity.FolderMedia;
import com.vn.castscreen.databinding.FragmentAlbumVideoBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.AlbumPhotoAdapter;

import java.util.ArrayList;


public class AlbumVideoFragment extends BaseFragment<FragmentAlbumVideoBinding> implements OnClickItemListener<FolderMedia> {

    private String TAG = "AlbumVideoFragment";

    private AlbumPhotoAdapter adapter;

    @Override
    protected FragmentAlbumVideoBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAlbumVideoBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initEvent() {

        Log.d(TAG, "initEvent: ");
        adapter = new AlbumPhotoAdapter(requireContext(), this);
        binding.rcvAlbumVideo.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcvAlbumVideo.setAdapter(adapter);
        adapter.setData(getAllFolderImage());
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: ");

    }

    @SuppressLint("Recycle")
    private ArrayList<FolderMedia> getAllFolderImage() {
        boolean isFolder = false;
        ArrayList<FolderMedia> allFolderVideo = new ArrayList<>();
        allFolderVideo.clear();

        int position = 0;
        Uri uri;
        Cursor cursor;
        int columnIndexData, columnIndexFolderName;
        String absolutePathOfImage = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME};

        final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        cursor = requireContext().getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(columnIndexData);

            for (int i = 0; i < allFolderVideo.size(); i++) {

                if (cursor.getString(columnIndexFolderName) != null&& allFolderVideo.get(i) != null && allFolderVideo.get(i).getName() != null) {
                    if (allFolderVideo.get(i).getName().equals(cursor.getString(columnIndexFolderName))) {
                        isFolder = true;
                        position = i;
                        break;
                    } else {
                        isFolder = false;
                    }
                }
            }
            if (isFolder) {
                ArrayList<String> listPath = new ArrayList<>();
                listPath.addAll(allFolderVideo.get(position).getListPath());
                listPath.add(absolutePathOfImage);
                allFolderVideo.get(position).setListPath(listPath);

            } else {
                ArrayList<String> al_path = new ArrayList<>();
                al_path.add(absolutePathOfImage);
                FolderMedia folderMedia = new FolderMedia();
                folderMedia.setName(cursor.getString(columnIndexFolderName));
                folderMedia.setListPath(al_path);
                allFolderVideo.add(folderMedia);
            }
        }


        return allFolderVideo;
    }

    @Override
    public void onClickItem(FolderMedia item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("folderMedia", item);
        Navigation.findNavController(requireView()).navigate(R.id.action_videoFragment_to_detailAlbumsVideoFragment, bundle);
    }
}