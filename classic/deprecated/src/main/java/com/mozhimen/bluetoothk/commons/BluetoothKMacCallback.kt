package com.mozhimen.bluetoothk.commons


/**
 * @ClassName BluetoothKMacCallback
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/17 11:15
 * @Version 1.0
 */
abstract class BluetoothKMacCallback {
    abstract fun getMac(mac: String?): String?
}