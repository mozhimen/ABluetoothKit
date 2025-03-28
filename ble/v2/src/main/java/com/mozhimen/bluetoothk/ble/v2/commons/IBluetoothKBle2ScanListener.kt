package com.mozhimen.bluetoothk.ble.v2.commons

import android.bluetooth.le.ScanResult
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKScanListener

/**
 * @ClassName IBluetoothKBleScanListener
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
interface IBluetoothKBle2ScanListener : IBluetoothKScanListener {
    fun onFound(scanResult: ScanResult) {
        onFound(scanResult.device)
    }
}