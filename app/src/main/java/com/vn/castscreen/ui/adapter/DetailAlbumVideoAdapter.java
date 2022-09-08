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

public class DetailAlbumVideoAdapter extends RecyclerView.Adapter<DetailAlbumVideoAdapter.DetailAlbumVideoViewHolder> {

    private Context mContext;
    private ArrayList<String> mData;
    private OnClickItemListener<String> onClickItemListener;
    private LayoutInflater mLayoutInflater;

    public DetailAlbumVideoAdapter(Context mContext, OnClickItemListener<String> onClickItemListener) {
        this.mContext = mContext;
        this.onClickItemListener = onClickItemListener;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setData(ArrayList<String> list){
        this.mData = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DetailAlbumVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_detail_album_video, parent, false);
        return new DetailAlbumVideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailAlbumVideoViewHolder holder, int position) {
        String data = mData.get(position);
        Glide.with(mContext).load("file://" + data)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imvThumb);

        holder.itemView.setOnClickListener(v -> {
            onClickItemListener.onClickItem(data);
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class DetailAlbumVideoViewHolder extends RecyclerView.ViewHolder {

        private ImageView imvThumb;

        public DetailAlbumVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imvThumb = itemView.findViewById(R.id.imv_thumb_video);
        }
    }
}
