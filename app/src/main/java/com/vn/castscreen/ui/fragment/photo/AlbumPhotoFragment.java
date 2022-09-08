package com.vn.castscreen.ui.fragment.photo;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.vn.castscreen.databinding.FragmentAlbumBinding;
import com.vn.castscreen.ui.OnClickItemListener;
import com.vn.castscreen.ui.adapter.AlbumPhotoAdapter;

import java.util.ArrayList;

public class AlbumPhotoFragment extends BaseFragment<FragmentAlbumBinding> implements OnClickItemListener<FolderMedia> {

    private String TAG = "ALBUM_PHOTO";

    private final ArrayList<FolderMedia> allFolderImage = new ArrayList<>();
    private boolean isFolder = false;
    private AlbumPhotoAdapter adapter;

    @Override
    protected FragmentAlbumBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAlbumBinding.inflate(inflater, container, false);
    }
    @Override
    protected void initEvent() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new AlbumPhotoAdapter(requireContext(), this);
        binding.rcvAlbumImage.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcvAlbumImage.setAdapter(adapter);
        adapter.setData(getAllFolderImage());

    }

    @SuppressLint("Recycle")
    private ArrayList<FolderMedia> getAllFolderImage() {
//        allFolderImage.clear();
        int position = 0;
        Uri uri;
        Cursor cursor;
        int columnIndexData, columnIndexFolderName;
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = requireContext().getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " ASC");

        columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(columnIndexData);
            for (int i = 0; i < allFolderImage.size(); i++) {
//                if (cursor.getString(columnIndexFolderName) != null) {
                    if (allFolderImage.get(i).getName().equals(cursor.getString(columnIndexFolderName))) {
                        isFolder = true;
                        position = i;
                        break;
                    } else {
                        isFolder = false;
                    }
//                }
            }
            if (isFolder) {
                try {
                    ArrayList<String> listPath = new ArrayList<>(allFolderImage.get(position).getListPath());
                    listPath.add(absolutePathOfImage);
                    allFolderImage.get(position).setListPath(listPath);
                }catch (NullPointerException exception){
                    exception.printStackTrace();
                }
            } else {
                ArrayList<String> al_path = new ArrayList<>();
                al_path.add(absolutePathOfImage);
                FolderMedia folderMedia = new FolderMedia();
                folderMedia.setName(cursor.getString(columnIndexFolderName));
                folderMedia.setListPath(al_path);
                allFolderImage.add(folderMedia);
            }
        }
        return allFolderImage;
    }
    @Override
    public void onClickItem(FolderMedia item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("folderMedia", item);
        Navigation.findNavController(requireView()).navigate(R.id.action_photoFragment_to_detailAlbumsFragment, bundle);
    }
    @Override
    public void onResume() {
        super.onResume();
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();

    }
}