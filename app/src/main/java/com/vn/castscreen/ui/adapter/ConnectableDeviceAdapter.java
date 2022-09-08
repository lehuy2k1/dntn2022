package com.vn.castscreen.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vn.castscreen.R;
import com.vn.castscreen.ui.OnClickItemListener;
import com.sdk.device.ConnectableDevice;

import java.util.ArrayList;

public class ConnectableDeviceAdapter extends RecyclerView.Adapter<ConnectableDeviceAdapter.ConnectableDeviceViewHolder> {

    private Context mContext;
    private ArrayList<ConnectableDevice> mData;
    private OnClickItemListener<ConnectableDevice> onClickItemListener;
    private LayoutInflater mLayoutInflater;

    public ConnectableDeviceAdapter(Context mContext, OnClickItemListener<ConnectableDevice> onClickItemListener) {
        this.mContext = mContext;
        this.onClickItemListener = onClickItemListener;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setData(ArrayList<ConnectableDevice> list){
        this.mData = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConnectableDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_connectable_device, parent, false);
        return new ConnectableDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectableDeviceViewHolder holder, int position) {
        ConnectableDevice device = mData.get(position);
        holder.tvNameDevice.setText(device.getFriendlyName());
        holder.tvProtocol.setText(device.getServiceId());

        holder.itemView.setOnClickListener(v -> onClickItemListener.onClickItem(device));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public class ConnectableDeviceViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNameDevice;
        private TextView tvProtocol;

        public ConnectableDeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNameDevice = itemView.findViewById(R.id.tv_name_device);
            tvProtocol = itemView.findViewById(R.id.tv_protocol_device);
        }
    }
}
