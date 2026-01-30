package com.mozhimen.bluetoothk.ble.v1.impls

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKScanProxy
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
import com.mozhimen.bluetoothk.ble.v1.BluetoothKBle1
import com.mozhimen.bluetoothk.ble.v1.commons.IBluetoothKBle1ScanListener
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.os.UtilKHandlerWrapper
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import java.util.UUID
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
class BluetoothKBle1ScanProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKScanProxy<BluetoothDevice,IBluetoothKBle1ScanListener> {
    private var _bluetoothKBle1ScanListener: IBluetoothKBle1ScanListener? = null
    private var _isScanning: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (newValue)
            _bluetoothKBle1ScanListener?.onStart()
        else
            _bluetoothKBle1ScanListener?.onStop()
    }

    private val _scanCallback: BluetoothAdapter.LeScanCallback = object : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            if (scanRecord != null && device != null) {
                UtilKLogWrapper.d(TAG, "onScanResult: device $device rssi $rssi") // result.getScanRecord() 获取BLE广播数据
                _bluetoothKBle1ScanListener?.onFound(device)
            }
        }
    }
    private var _serviceUuids: Array<UUID>? = null

    //////////////////////////////////////////////////////////////////////////

    fun setServiceUuids(serviceUuids: Array<UUID>) {
        _serviceUuids = serviceUuids
    }

    override fun setBluetoothScanListener(listener: IBluetoothKBle1ScanListener) {
        _bluetoothKBle1ScanListener = listener
    }

    @OptIn(OApiInit_InApplication::class)
    override fun startScan(activity: Activity) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            if (BluetoothKBle1.instance.getBluetoothAdapter() == null)
                return@requestBluetoothBlePermission
            if (!BluetoothKBle1.instance.isBluetoothEnabled())
                BluetoothKBle1.instance.getBluetoothAdapter()?.enable()
            cancelScan()
            BluetoothKBle1.instance.getBluetoothAdapter()?.startLeScan(_serviceUuids, _scanCallback).also {
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
            BluetoothKBle1.instance.getBluetoothAdapter()?.stopLeScan(_scanCallback).also {
                _isScanning = false
            }
        }
    }

//    override fun startBound(bluetoothDevice: BluetoothDevice) {
//        cancelBound()
//        _bluetoothKBle2ScanListener?.onBonded(bluetoothDevice)
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
        _bluetoothKBle1ScanListener = null
        super.onDestroy(owner)
    }
}