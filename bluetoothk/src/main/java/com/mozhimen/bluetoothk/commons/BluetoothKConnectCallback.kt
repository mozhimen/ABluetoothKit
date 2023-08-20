package com.mozhimen.bluetoothk.commons

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Looper
import com.mozhimen.bluetoothk.BluetoothK


/**
 * @ClassName BluetoothKConnectCallback
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/17 11:16
 * @Version 1.0
 */
abstract class BluetoothKConnectCallback {
    /**
     * 连接成功或失败后调用
     * @param socket 获得的socket
     * @param device 本次连接的设备，可存下来方便下次自动重连，就不用每次都选择了。
     * @param e 错误
     */
    abstract fun connected(socket: BluetoothSocket?, device: BluetoothDevice?, e: Exception?)

    /**
     * 连接断开后调用，原理为监听系统广播
     */
    abstract fun disconnected()

    open fun internalConnected(socket: BluetoothSocket?, device: BluetoothDevice?, e: Exception?) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (BluetoothK.instance._handler?.post { connected(socket, device, e) } != true) {
            }
        } else {
            connected(socket, device, e)
        }
    }

    open fun internalDisconnected() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (BluetoothK.instance._handler?.post { disconnected() } != true) {
            }
        } else {
            disconnected()
        }
    }
}