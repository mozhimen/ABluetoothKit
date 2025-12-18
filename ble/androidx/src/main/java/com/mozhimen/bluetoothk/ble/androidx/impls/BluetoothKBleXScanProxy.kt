package com.mozhimen.bluetoothk.ble.androidx.impls

import android.app.Activity
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.ScanFilter
import androidx.bluetooth.ScanResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKScanListener
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKScanProxy
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
import com.mozhimen.bluetoothk.ble.androidx.BluetoothKBleX
import com.mozhimen.bluetoothk.ble.androidx.commons.IBluetoothKBleXScanListener
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

/**
 * @ClassName BluetoothKBleXScanProxy
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/26 20:24
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKBleXScanProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKScanProxy<ScanResult> {
    private var _scanJob: Job? = null
    private var _bluetoothKBleXScanListener: IBluetoothKBleXScanListener? = null
    private var _isScanning: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (newValue)
            _bluetoothKBleXScanListener?.onStart()
        else
            _bluetoothKBleXScanListener?.onStop()
    }
    private var _scanFilters: List<ScanFilter>? = null

    //////////////////////////////////////////////////////////////////////////

    fun setScanFilters(scanFilters: List<ScanFilter>) {
        _scanFilters = scanFilters
    }

    fun setBluetoothKBleXScanListener(listener: IBluetoothKBleXScanListener) {
        _bluetoothKBleXScanListener = listener
    }

    //////////////////////////////////////////////////////////////////////////

    override fun startScan(activity: Activity) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            _scanJob = (activity as LifecycleOwner).lifecycleScope.launch {
                try {
                    BluetoothLe(activity.applicationContext).scan().collect { scanResult: ScanResult ->
                        if (!scanResult.device.name.isNullOrEmpty()) {
                            UtilKLogWrapper.d(TAG, "onScanResult: scanResult $scanResult") // result.getScanRecord() 获取BLE广播数据
                            _bluetoothKBleXScanListener?.onFound(scanResult.also { BluetoothKBleX.instance.addScanResult(it) })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.also { _isScanning = true }
        })
    }

    override fun isScanning(): Boolean =
        _isScanning

    override fun cancelScan() {
        if (isScanning()) {
            _scanJob?.cancel()
            _scanJob = null
            _isScanning = false
        }
    }

    override fun startBound(activity: Activity, obj: ScanResult) {
        _bluetoothKBleXScanListener?.onBonding(obj)
        BluetoothKBleX.instance.connect(obj.deviceAddress.address, activity) {
            _bluetoothKBleXScanListener?.onBonded(obj)
        }
    }

    //////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        _bluetoothKBleXScanListener = null
        cancelBound()
        cancelScan()
        super.onDestroy(owner)
    }
}