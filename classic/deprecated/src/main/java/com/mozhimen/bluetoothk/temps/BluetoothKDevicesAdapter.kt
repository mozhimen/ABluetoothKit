package com.mozhimen.bluetoothk.temps

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.R
import com.mozhimen.bluetoothk.cons.CBluetoothKCons
import com.mozhimen.stackk.monitor.StackKMonitor

/**
 * @ClassName BluetoothKDevicesAdapter
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 11:43
 * @Version 1.0
 */
class BluetoothKDevicesAdapter(
    private val _pairedDevices: ArrayList<String>,
    private val _foundedDevices: ArrayList<String>,
    private val _bluetoothAdapter: BluetoothAdapter,
    private val _key: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == 0)
            BTViewHolderWithTitle(LayoutInflater.from(parent.context).inflate(R.layout.bluetoothk_item_with_title, parent, false))
        else
            BTViewHolderNoTitle(LayoutInflater.from(parent.context).inflate(R.layout.bluetoothk_item, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < _pairedDevices.size) {
            val s = _pairedDevices[position]
            val name = s.substring(0, s.lastIndexOf("\n"))
            val mac = s.substring(s.lastIndexOf("\n") + 1)
            if (holder is BTViewHolderWithTitle) {
                holder._btkTxtName.text = name
                holder._btkTxtMac.text = mac
                holder._btkTxtTitle.text = "连接过的设备"
            } else {
                (holder as BTViewHolderNoTitle)._btkTxtName.text = name
                holder._btkTxtMac.text = mac
            }
        } else {
            val s = _foundedDevices[position - _pairedDevices.size]
            val name = s.substring(0, s.lastIndexOf("\n"))
            val mac = s.substring(s.lastIndexOf("\n") + 1)
            if (holder is BTViewHolderWithTitle) {
                holder._btkTxtName.text = name
                holder._btkTxtMac.text = mac
                holder._btkTxtTitle.text = "搜索到的设备"
            } else {
                (holder as BTViewHolderNoTitle)._btkTxtName.text = name
                holder._btkTxtMac.text = mac
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (position == 0 || position == _pairedDevices.size) 0 else 1//带头的为0，不带头的为1

    override fun getItemCount(): Int =
        _pairedDevices.size + _foundedDevices.size

    @SuppressLint("MissingPermission")
    open inner class BTViewHolderNoTitle(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var _btkTxtName: TextView
        var _btkTxtMac: TextView

        init {
            _btkTxtMac = itemView.findViewById<View>(R.id.btk_item_txt_mac) as TextView
            _btkTxtName = itemView.findViewById<View>(R.id.btk_item_txt_name) as TextView
            itemView.findViewById<View>(R.id.btk_item_root).setOnClickListener { view ->
                _bluetoothAdapter.cancelDiscovery()
                view.context.sendBroadcast(Intent(CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY))
                val position = layoutPosition
                val s: String = if (position < _pairedDevices.size) {
                    _pairedDevices[position]
                } else {
                    _foundedDevices[position - _pairedDevices.size]
                }
                val mac = s.substring(s.lastIndexOf("\n") + 1)
                val name = s.substring(0, s.lastIndexOf("\n"))
                BluetoothK.instance.executeMacCallback(name, mac, _key)
                StackKMonitor.instance.popAllActivity()
            }
        }
    }

    inner class BTViewHolderWithTitle(itemView: View) : BTViewHolderNoTitle(itemView) {
        var _btkTxtTitle: TextView

        init {
            _btkTxtTitle = itemView.findViewById<View>(R.id.btk_item_title_txt_title) as TextView
        }
    }
}