package com.mozhimen.bluetoothk.ble.commons

import android.bluetooth.le.ScanResult
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKScanListener

/**
 * @ClassName IBluetoothKBleScanListener
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
interface IBluetoothKBleScanListener : IBluetoothKScanListener {
    fun onFound(scanResult: ScanResult) {
        onFound(scanResult.device)
    }
}