package com.mozhimen.bluetoothk.helpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.basick.stackk.monitor.StackMonitor
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.R
import com.mozhimen.bluetoothk.cons.CBluetoothKCons

/**
 * @ClassName BluetoothKDevicesAdapter
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 11:43
 * @Version 1.0
 */
class BluetoothKDevicesAdapter(context: Context, pairedDeviceList: ArrayList<String>, foundDeviceList: ArrayList<String>, bluetoothAdapter: BluetoothAdapter, key: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mPairedDeviceList: ArrayList<String> = pairedDeviceList
    private var mFoundDeviceList: ArrayList<String> = foundDeviceList
    private var mBluetoothAdapter: BluetoothAdapter = bluetoothAdapter
    private var mContext: Context = context
    private var mKey: String = key

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val viewWithTitle: View = LayoutInflater.from(parent.context).inflate(R.layout.item_bluetooth_list_with_title, parent, false)
            ViewHolderWithTitle(viewWithTitle)
        } else {
            val viewWithoutTitle: View = LayoutInflater.from(parent.context).inflate(R.layout.item_bluetooth_list, parent, false)
            ViewHolder(viewWithoutTitle)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < mPairedDeviceList.size) {
            val s = mPairedDeviceList[position]
            val mac = s.substring(s.lastIndexOf("\n") + 1)
            val name = s.substring(0, s.lastIndexOf("\n"))
            if (holder is ViewHolderWithTitle) {
                holder.mTvBleName.setText(name)
                holder.mTvBleMac.setText(mac)
                holder.mTvTitle.text = "连接过的设备"
            } else {
                (holder as ViewHolder).mTvBleName.text = name
                holder.mTvBleMac.text = mac
            }
        } else {
            val s = mFoundDeviceList[position - mPairedDeviceList.size]
            val mac = s.substring(s.lastIndexOf("\n") + 1)
            val name = s.substring(0, s.lastIndexOf("\n"))
            if (holder is ViewHolderWithTitle) {
                holder.mTvBleName.setText(name)
                holder.mTvBleMac.setText(mac)
                holder.mTvTitle.text = "搜索到的设备"
            } else {
                (holder as ViewHolder).mTvBleName.text = name
                holder.mTvBleMac.text = mac
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        //带头的为0，不带头的为1
        return if (position == 0 || position == mPairedDeviceList.size) {
            0
        } else {
            1
        }
    }

    override fun getItemCount(): Int {
        return mPairedDeviceList.size + mFoundDeviceList.size
    }


    @SuppressLint("MissingPermission")
    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTvBleName: AppCompatTextView
        var mTvBleMac: AppCompatTextView

        init {
            mTvBleMac = itemView.findViewById<View>(R.id.tv_ble_mac) as AppCompatTextView
            mTvBleName = itemView.findViewById<View>(R.id.tv_ble_name) as AppCompatTextView
            itemView.findViewById<View>(R.id.ll_content).setOnClickListener { v ->
                mBluetoothAdapter.cancelDiscovery()
                v.context.sendBroadcast(Intent(CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY))
                val position = layoutPosition
                var s = ""
                s = if (position < mPairedDeviceList.size) {
                    mPairedDeviceList.get(position)
                } else {
                    mFoundDeviceList.get(position - mPairedDeviceList.size)
                }
                val mac = s.substring(s.lastIndexOf("\n") + 1)
                val name = s.substring(0, s.lastIndexOf("\n"))
                BluetoothK.instance.executeMacCallback(name, mac, mKey)
                StackMonitor.instance.popAllActivity()
            }
        }
    }


    inner class ViewHolderWithTitle(itemView: View) : ViewHolder(itemView) {
        var mTvTitle: AppCompatTextView

        init {
            mTvTitle = itemView.findViewById<View>(R.id.tv_title) as AppCompatTextView
        }
    }
}