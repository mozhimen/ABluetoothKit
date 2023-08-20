package com.mozhimen.bluetoothk.commons

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Looper
import com.mozhimen.bluetoothk.MedBluetooth
import java.io.IOException


/**
 * @ClassName SocketConnectedCallback
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/17 11:11
 * @Version 1.0
 */

abstract class BluetoothKSocketConnectedCallback {
    abstract fun done(socket: BluetoothSocket?, device: BluetoothDevice?, e: IOException?)

    fun internalDone(socket: BluetoothSocket?, device: BluetoothDevice?, e: IOException?) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (!MedBluetooth.mHandler.post { done(socket, device, e) }) {
            }
        } else {
            done(socket, device, e)
        }
    }
}
