package com.mozhimen.bluetoothk.commons

import android.app.Activity

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
}