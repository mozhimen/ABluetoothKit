package com.mozhimen.bluetoothk.ble.test

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.mozhimen.bluetoothk.ble.test.databinding.ActivityClientBinding
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDB
import java.util.UUID

class BleClientActivity : BaseActivityVDB<ActivityClientBinding>() {
    private var mWriteET: EditText? = null
    private var mTips: TextView? = null
    private var mBleDevAdapter: BleDevAdapter? = null
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bleclient)
        val rv: RecyclerView = findViewById(R.id.rv_ble)
        mWriteET = findViewById<EditText>(R.id.et_write)
        mTips = findViewById<TextView>(R.id.tv_tips)
        rv.setLayoutManager(LinearLayoutManager(this))
        mBleDevAdapter = BleDevAdapter(object : Listener() {
            override fun onItemClick(dev: BluetoothDevice) {
                closeConn()
                mBluetoothGatt = dev.connectGatt(this@BleClientActivity, false, mBluetoothGattCallback) // 连接蓝牙设备
                logTv(String.format("与[%s]开始连接............", dev))
            }
        })
        rv.setAdapter(mBleDevAdapter)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeConn()
    }


}