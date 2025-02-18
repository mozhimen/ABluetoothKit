package com.mozhimen.bluetoothk.commons

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket

/**
 * @ClassName IBluetoothKEventListener
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/18
 * @Version 1.0
 */
interface IBluetoothKEventListener {
    /**
     * 连接成功或失败后调用
     * @param socket 获得的socket
     * @param device 本次连接的设备，可存下来方便下次自动重连，就不用每次都选择了。
     * @param e 错误
     */
    fun onConnect(socket: BluetoothSocket?, device: BluetoothDevice?, e: Exception?)

    /**
     * 连接断开后调用，原理为监听系统广播
     */
    fun onDisconnect()
    fun onClosing()
    fun onClose()
    fun onOpening()
    fun onOpen()
}