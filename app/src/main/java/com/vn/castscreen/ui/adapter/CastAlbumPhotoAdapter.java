package com.vn.castscreen.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vn.castscreen.R;
import com.vn.castscreen.ui.OnClickItemListener;

import java.util.ArrayList;

public class CastAlbumPhotoAdapter extends RecyclerView.Adapter<CastAlbumPhotoAdapter.DetailAlbumViewHolder> {
    private Context mContext;
    private ArrayList<String> mData;
    private OnClickItemListener<String> onClickItemListener;
    private LayoutInflater mLayoutInflater;

    public CastAlbumPhotoAdapter(Context mContext, OnClickItemListener<String> onClickItemListener) {
        this.mContext = mContext;
        this.onClickItemListener = onClickItemListener;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setData(ArrayList<String> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DetailAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.layout_item_queue_images, parent, false);
        return new DetailAlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailAlbumViewHolder holder, int position) {
        String path = mData.get(position);
        if (path != null) {
            Glide.with(mContext).load("file://" + path)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.imvDetailAlbumPhoto);
        }

        holder.itemView.setOnClickListener(v -> {
            onClickItemListener.onClickItem(path);
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class DetailAlbumViewHolder extends RecyclerView.ViewHolder {

        private ImageView imvDetailAlbumPhoto;

        public DetailAlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            imvDetailAlbumPhoto = itemView.findViewById(R.id.ic_image);
        }
    }
}
