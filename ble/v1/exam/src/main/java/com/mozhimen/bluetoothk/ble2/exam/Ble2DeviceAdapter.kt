package com.mozhimen.bluetoothk.ble2.exam

import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.widget.TextView
import com.mozhimen.kotlin.lintk.optins.OApiCall_Recycle
import com.mozhimen.xmlk.adapterk.list.AdapterKList
import com.mozhimen.xmlk.vhk.VHK

/**
 * @ClassName ListViewAdapter
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
@OptIn(OApiCall_Recycle::class)
class Ble2DeviceAdapter(list: List<BluetoothDevice>) : AdapterKList<BluetoothDevice>(list.toMutableList(),R.layout.simple_expandable_list_item_1) {
    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onBindView(holder: VHK, data: BluetoothDevice, position: Int) {
        holder.findViewById<TextView>(R.id.text1).text = """
            ${data.name}
            ${data.address}
            """.trimIndent()
    }
}