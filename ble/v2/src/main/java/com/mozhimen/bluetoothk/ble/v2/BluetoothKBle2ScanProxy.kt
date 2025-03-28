package com.mozhimen.bluetoothk.ble.v2

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKScanProxy
import com.mozhimen.bluetoothk.ble.v2.commons.IBluetoothKBle2ScanListener
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
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
class BluetoothKBle2ScanProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKScanProxy {
    private var _bluetoothKBleScanListener: IBluetoothKBle2ScanListener? = null
    private var _isScanning: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (newValue)
            _bluetoothKBleScanListener?.onStart()
        else
            _bluetoothKBleScanListener?.onStop()
    }

    private val _scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device != null) {
                UtilKLogWrapper.d(TAG, "onScanResult: callbackType $callbackType result $result") // result.getScanRecord() 获取BLE广播数据
                _bluetoothKBleScanListener?.onFound(result)
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    fun setBluetoothKBleScanListener(listener: IBluetoothKBle2ScanListener) {
        _bluetoothKBleScanListener = listener
    }

    @OptIn(OApiInit_InApplication::class)
    override fun startScan(activity: Activity) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            if (BluetoothKBle2.instance.getBluetoothAdapter() == null)
                return@requestBluetoothBlePermission
            if (!BluetoothKBle2.instance.isBluetoothEnabled())
                BluetoothKBle2.instance.getBluetoothAdapter()?.enable()
            cancelScan()
            BluetoothKBle2.instance.getBluetoothLeScanner()?.startScan(_scanCallback).also {
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
        _bluetoothKBleScanListener = null
        super.onDestroy(owner)
    }
}