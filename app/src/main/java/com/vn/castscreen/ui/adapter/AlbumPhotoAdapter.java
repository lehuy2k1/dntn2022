package com.vn.castscreen.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vn.castscreen.R;
import com.vn.castscreen.data.entity.FolderMedia;
import com.vn.castscreen.ui.OnClickItemListener;

import java.util.ArrayList;

public class AlbumPhotoAdapter extends RecyclerView.Adapter<AlbumPhotoAdapter.AlbumPhotoViewHolder> {

    private Context mContext;
    private ArrayList<FolderMedia> mData;
    private OnClickItemListener<FolderMedia> onClickItemListener;
    private LayoutInflater mLayoutInflater;

    public AlbumPhotoAdapter(Context mContext, OnClickItemListener<FolderMedia> onClickItemListener) {
        this.mContext = mContext;
        this.onClickItemListener = onClickItemListener;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setData(ArrayList<FolderMedia> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_album_photo, parent, false);
        return new AlbumPhotoViewHolder(view);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AlbumPhotoViewHolder holder, int position) {
        FolderMedia folderMedia = mData.get(position);
        Glide.with(mContext).load("file://" + folderMedia.getListPath().get(0))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imvFolder);
        if (folderMedia.getName() == null || folderMedia.getName().equals("")){
            holder.nameFolder.setText(mContext.getString(R.string.unknown));
        }else {
            holder.nameFolder.setText(folderMedia.getName());
        }
        holder.numberFile.setText("(" + folderMedia.getListPath().size() + ")");
        holder.itemView.setOnClickListener(v -> {
            onClickItemListener.onClickItem(folderMedia);
        });
    }
    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class AlbumPhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView imvFolder;
        private TextView nameFolder;
        private TextView numberFile;

        public AlbumPhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            imvFolder = itemView.findViewById(R.id.imv_album);
            nameFolder = itemView.findViewById(R.id.tv_name_folder);
            numberFile = itemView.findViewById(R.id.tv_number_file);
        }
    }
}
