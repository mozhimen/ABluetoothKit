package com.mozhimen.bluetoothk.helpers

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.mozhimen.kotlin.elemk.android.content.bases.BaseBroadcastReceiver
import com.mozhimen.kotlin.utilk.android.util.d
import com.mozhimen.kotlin.utilk.android.util.w
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothDevice

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
        "onReceive action = $action".d(TAG)

        if (action == CBluetoothDevice.ACTION_ACL_DISCONNECTED) {
            val device = intent.getParcelableExtra<Parcelable>(CBluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice?
            if (device == null) {
                "onReceive device == null".w(TAG)
                return
            }
            val mac = device.address
            "onReceive mac = $mac".d(TAG)
            BluetoothK.instance.executeBluetoothDisconnectedCallback(mac)
        }
    }
}