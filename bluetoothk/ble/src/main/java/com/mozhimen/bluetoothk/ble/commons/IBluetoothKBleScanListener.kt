package com.mozhimen.bluetoothk.ble.commons

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

/**
 * @ClassName IBluetoothKBleScanListener
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
interface IBluetoothKBleScanListener {
    fun onStart(){}
    fun onFound(scanResult: ScanResult){}
    fun onBonding(scanResult: ScanResult){}
    fun onBonded(scanResult: ScanResult){}
    fun onStop(){}
}