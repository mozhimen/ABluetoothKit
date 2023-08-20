package com.mozhimen.bluetoothk.helpers

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import com.mozhimen.basick.elemk.android.content.bases.BaseBroadcastReceiver
import com.mozhimen.basick.utilk.android.util.dt
import com.mozhimen.basick.utilk.android.util.wt
import com.mozhimen.bluetoothk.MedBluetooth
import com.mozhimen.bluetoothk.cons.CBluetoothDevice


/**
 * @ClassName BluetoothKStateChangeReceiver
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/17 11:03
 * @Version 1.0
 */
class BluetoothKStateChangeReceiver : BaseBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        "onReceive action = $action".dt(TAG)

        if (action == CBluetoothDevice.ACTION_ACL_DISCONNECTED) {
            val device = intent.getParcelableExtra<Parcelable>(CBluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice?
            if (device == null) {
                "onReceive device == null".wt(TAG)
                return
            }
            val mac = device.address
            "onReceive mac = $mac".dt(TAG)
            MedBluetooth.executeBluetoothDisconnectedCallback(mac)
        }
    }
}