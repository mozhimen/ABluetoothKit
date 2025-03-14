package com.mozhimen.bluetoothk.ble

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.ble.commons.IBluetoothKBleScanListener
import com.mozhimen.bluetoothk.commons.IBluetoothKScanProxy
import com.mozhimen.bluetoothk.utils.BluetoothKUtil
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

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
class BluetoothKBleScanProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKScanProxy {
    private var _bluetoothKBleScanListener: IBluetoothKBleScanListener? = null
    private var _bluetoothDevices: ConcurrentHashMap<String, BluetoothDevice> = ConcurrentHashMap<String, BluetoothDevice>()
    private var _isScanning: AtomicBoolean = AtomicBoolean(false)

    private val _scanCallback: ScanCallback = object : ScanCallback() {
        // 扫描Callback
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val dev: win.lioil.bluetooth.ble.BleDevAdapter.BleDev = win.lioil.bluetooth.ble.BleDevAdapter.BleDev(result.device, result)
            if (!mDevices.contains(dev)) {
                mDevices.add(dev)
                notifyDataSetChanged()
                Log.i(win.lioil.bluetooth.ble.BleDevAdapter.TAG, "onScanResult: $result") // result.getScanRecord() 获取BLE广播数据
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////

    fun setBluetoothKBleScanListener(listener: IBluetoothKBleScanListener) {
        _bluetoothKBleScanListener = listener
    }

    @OptIn(OApiInit_InApplication::class)
    override fun startScan(activity: Activity) {
        BluetoothKUtil.requestBluetoothPermission(activity) {
            if (BluetoothKBle.instance.getBluetoothAdapter() == null)
                return@requestBluetoothPermission
            if (!BluetoothKBle.instance.isBluetoothEnabled())
                BluetoothKBle.instance.getBluetoothAdapter()?.enable()
            cancelScan()
            BluetoothK.instance.getBluetoothAdapter()?.startDiscovery()
        }
    }

    override fun isScanning(): Boolean =
        _isScanning.get()

    @OptIn(OApiInit_InApplication::class)
    override fun cancelScan() {
        if (isScanning()) {
            BluetoothKBle.instance.getBluetoothLeScanner()?.stopScan(_scanCallback)
        }
        _bluetoothDevices.clear()
    }
}