package com.mozhimen.bluetoothk.ble.androidx.commons

/**
 * @ClassName IBluetoothKXClientListener
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/4/6 3:50
 * @Version 1.0
 */
interface IBluetoothKXClientListener {
    fun onDisConnected() {}
    fun onConnecting() {}
    fun onConnected() {}
    fun onConnectFail() {}
    fun onReadGattCharacteristic(bytes: ByteArray) {}
}