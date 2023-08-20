package com.mozhimen.bluetoothk.temps;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mozhimen.basick.stackk.monitor.StackMonitor;
import com.mozhimen.bluetoothk.BluetoothK;
import com.mozhimen.bluetoothk.R;

import java.util.ArrayList;

/**
 * Created by lee on 5/23/16.
 */
@SuppressLint("MissingPermission")
public class BluetoothKDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> mPairedDeviceList;
    private ArrayList<String> mFoundDeviceList;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private String mKey;

    public BluetoothKDevicesAdapter(Context context, ArrayList<String> pairedDeviceList, ArrayList<String> foundDeviceList, BluetoothAdapter bluetoothAdapter, String key) {
        mFoundDeviceList = foundDeviceList;
        mPairedDeviceList = pairedDeviceList;
        mBluetoothAdapter = bluetoothAdapter;
        mContext = context;
        mKey = key;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 0) {
            View viewWithTitle = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_list_with_title, parent, false);
            return new ViewHolderWithTitle(viewWithTitle);
        } else {
            View viewWithoutTitle = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_list, parent, false);
            return new ViewHolder(viewWithoutTitle);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < mPairedDeviceList.size()) {
            String s = mPairedDeviceList.get(position);
            String mac = s.substring(s.lastIndexOf("\n") + 1);
            String name = s.substring(0,s.lastIndexOf("\n"));

            if (holder instanceof ViewHolderWithTitle) {
                ((ViewHolderWithTitle) holder).mTvBleName.setText(name);
                ((ViewHolderWithTitle) holder).mTvBleMac.setText(mac);
                ((ViewHolderWithTitle) holder).mTvTitle.setText("连接过的设备");
            } else {
                ((ViewHolder) holder).mTvBleName.setText(name);
                ((ViewHolder) holder).mTvBleMac.setText(mac);
            }

        } else {
            String s = mFoundDeviceList.get(position - mPairedDeviceList.size());
            String mac = s.substring(s.lastIndexOf("\n") + 1);
            String name = s.substring(0,s.lastIndexOf("\n"));

            if (holder instanceof ViewHolderWithTitle) {
                ((ViewHolderWithTitle) holder).mTvBleName.setText(name);
                ((ViewHolderWithTitle) holder).mTvBleMac.setText(mac);
                ((ViewHolderWithTitle) holder).mTvTitle.setText("搜索到的设备");
            } else {
                ((ViewHolder) holder).mTvBleName.setText(name);
                ((ViewHolder) holder).mTvBleMac.setText(mac);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {

        //带头的为0，不带头的为1
        if (position == 0 || position == mPairedDeviceList.size()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return mPairedDeviceList.size() + mFoundDeviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public AppCompatTextView mTvBleName;
        public AppCompatTextView mTvBleMac;
        public ViewHolder(final View itemView) {
            super(itemView);
            mTvBleMac = (AppCompatTextView) itemView.findViewById(R.id.tv_ble_mac);
            mTvBleName = (AppCompatTextView) itemView.findViewById(R.id.tv_ble_name);
            itemView.findViewById(R.id.ll_content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBluetoothAdapter.cancelDiscovery();
                    v.getContext().sendBroadcast(new Intent(BluetoothK.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY));
                    int position = getLayoutPosition();
                    String s = "";
                    if (position < mPairedDeviceList.size()) {
                        s = mPairedDeviceList.get(position);
                    } else {
                        s = mFoundDeviceList.get(position - mPairedDeviceList.size());
                    }
                    String mac = s.substring(s.lastIndexOf("\n") + 1);
                    String name = s.substring(0,s.lastIndexOf("\n") );

                    BluetoothK.executeMacCallback(name,mac,mKey);
                    StackMonitor.getInstance().popAllActivity();
                }
            });
        }
    }

    public class ViewHolderWithTitle extends ViewHolder {

        public AppCompatTextView mTvTitle;

        public ViewHolderWithTitle(View itemView) {
            super(itemView);
            mTvTitle = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
        }
    }
}
