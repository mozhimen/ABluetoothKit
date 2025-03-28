package com.mozhimen.bluetoothk.basic.commons

import android.app.Activity
import android.bluetooth.BluetoothDevice

/**
 * @ClassName IBluetoothKBleScanProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
interface IBluetoothKScanProxy {
    fun startScan(activity: Activity)
    fun isScanning(): Boolean
    fun cancelScan()
    fun startBound(bluetoothDevice: BluetoothDevice){}
    fun cancelBound(){}
}