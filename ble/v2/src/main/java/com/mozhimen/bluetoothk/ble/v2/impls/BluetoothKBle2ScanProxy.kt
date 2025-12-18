package com.mozhimen.bluetoothk.ble.v2.impls

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKScanProxy
import com.mozhimen.bluetoothk.ble.v2.commons.IBluetoothKBle2ScanListener
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
import com.mozhimen.bluetoothk.ble.v2.BluetoothKBle2
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.os.UtilKHandlerWrapper
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import kotlin.properties.Delegates

/**
 * @ClassName BluetooKBleScanProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKBle2ScanProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKScanProxy<BluetoothDevice> {
    private var _bluetoothKBle2ScanListener: IBluetoothKBle2ScanListener? = null
    private var _isScanning: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (newValue)
            _bluetoothKBle2ScanListener?.onStart()
        else
            _bluetoothKBle2ScanListener?.onStop()
    }

    private val _scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device != null) {
                val device = result.device
                val name = device.name ?: "Unknown"
                val address = device.address
                val rssi = result.rssi
                UtilKLogWrapper.d(TAG, "onScanResult: name名称:$name address地址:$address rssi信号强度:$rssi") // result.getScanRecord() 获取BLE广播数据
                _bluetoothKBle2ScanListener?.onFound(result)
                // 在此处处理扫描到的设备信息，如添加到列表、更新UI等
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            // 如果需要处理批量扫描结果，可以在此处进行
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // 根据错误码处理扫描失败情况
            UtilKLogWrapper.e(TAG, "onScanFailed: BLE扫描失败，错误代码: $errorCode")
        }
    }
    private var _scanFilters: List<ScanFilter>? = null
    private var _scanSettings: ScanSettings = ScanSettings.Builder().build()

    //////////////////////////////////////////////////////////////////////////

    fun setScanFilters(scanFilters: List<ScanFilter>) {
        _scanFilters = scanFilters
    }

    fun setScanSettings(scanSettings: ScanSettings) {
        _scanSettings = scanSettings
    }

    fun setBluetoothKBle2ScanListener(listener: Any) {
        _bluetoothKBle2ScanListener = listener
    }

    @OptIn(OApiInit_InApplication::class)
    override fun startScan(activity: Activity) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            if (BluetoothKBle2.instance.getBluetoothAdapter() == null)
                return@requestBluetoothBlePermission
            if (!BluetoothKBle2.instance.isBluetoothEnabled())
                BluetoothKBle2.instance.getBluetoothAdapter()?.enable()
            cancelScan()
            BluetoothKBle2.instance.getBluetoothLeScanner()?.startScan(_scanFilters, _scanSettings, _scanCallback).also {
                _isScanning = true
            }
            UtilKHandlerWrapper.postDelayed(10000L) {
                cancelScan()
            }
        })
    }

    override fun isScanning(): Boolean =
        _isScanning

    @OptIn(OApiInit_InApplication::class)
    override fun cancelScan() {
        if (isScanning()) {
            BluetoothKBle2.instance.getBluetoothLeScanner()?.stopScan(_scanCallback).also {
                _isScanning = false
            }
        }
    }

//    override fun startBound(bluetoothDevice: BluetoothDevice) {
//        cancelBound()
//        _bluetoothKBleScanListener?.onBonded(bluetoothDevice)
//        _bluetoothGatt = bluetoothDevice.connectGatt(_context, false, _bluetoothGattCallback)
//    }

//    fun cancelBound() {
//        _bluetoothGatt?.disconnect()
//        _bluetoothGatt?.close()
//        _bluetoothGatt = null
//        _bluetoothKBleScanListener?.onStop()
//    }

    //////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        cancelBound()
        cancelScan()
        _bluetoothKBle2ScanListener = null
        super.onDestroy(owner)
    }
}