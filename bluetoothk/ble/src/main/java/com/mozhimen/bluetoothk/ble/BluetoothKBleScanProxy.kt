package com.mozhimen.bluetoothk.ble

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.ble.commons.IBluetoothKBleScanListener
import com.mozhimen.bluetoothk.commons.IBluetoothKScanProxy
import com.mozhimen.bluetoothk.utils.BluetoothKUtil
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothGatt
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
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

    //    private var _bluetoothDevices: ConcurrentHashMap<String, ScanResult> = ConcurrentHashMap<String, ScanResult>()
    private var _isScanning: AtomicBoolean = AtomicBoolean(false)
    private var _isConnected: AtomicBoolean = AtomicBoolean(false)
    private var _bluetoothGatt: BluetoothGatt? = null

    private val _scanCallback: ScanCallback = object : ScanCallback() {
        // 扫描Callback
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device != null) {
//                if (!_bluetoothDevices.containsKey(result.device.address)) {
//                    _bluetoothDevices[result.device.address] = result
//                    notifyDataSetChanged()
                UtilKLogWrapper.d(TAG, "onScanResult: callbackType $callbackType result $result") // result.getScanRecord() 获取BLE广播数据
                _bluetoothKBleScanListener?.onFound(result)
//                }
            }
        }
    }

    private val _bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val bluetoothDevice = gatt.device
            UtilKLogWrapper.d(TAG, "onConnectionStateChange: name ${bluetoothDevice.name} address ${bluetoothDevice.address} status $status newState $newState")
            UtilKLogWrapper.d(TAG, "onConnectionStateChange: ${if (status == 0) if (newState == 2) "连接成功" else "连接断开" else "连接出错, 错误码:$status"}")
            if (status == CBluetoothGatt.GATT_SUCCESS && newState == CBluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices().also { _isConnected.compareAndSet(false, true) } //启动服务发现
            } else {
                cancelBound().also { _isConnected.compareAndSet(true, false) }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            UtilKLogWrapper.d(TAG, String.format("onServicesDiscovered:%s,%s,%s", gatt!!.device.name, gatt.device.address, status))
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                for (service in gatt.services) {
                    val allUUIDs = StringBuilder(
                        """
                UUIDs={
                S=${service.uuid}
                """.trimIndent()
                    )
                    for (characteristic in service.characteristics) {
                        allUUIDs.append(",\nC=").append(characteristic.uuid)
                        for (descriptor in characteristic.descriptors) allUUIDs.append(",\nD=").append(descriptor.uuid)
                    }
                    allUUIDs.append("}")
                    UtilKLogWrapper.d(TAG, "onServicesDiscovered:$allUUIDs")
                    UtilKLogWrapper.d("发现服务$allUUIDs")
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            UtilKLogWrapper.d(TAG, String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            UtilKLogWrapper.d("读取Characteristic[$uuid]:\n$valueStr")
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            UtilKLogWrapper.d(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            UtilKLogWrapper.d("写入Characteristic[$uuid]:\n$valueStr")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            UtilKLogWrapper.d(TAG, String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr))
            UtilKLogWrapper.d("通知Characteristic[$uuid]:\n$valueStr")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            val uuid = descriptor.uuid
            val valueStr = descriptor.value.contentToString()
            UtilKLogWrapper.d(TAG, String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            UtilKLogWrapper.d("读取Descriptor[$uuid]:\n$valueStr")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            val uuid = descriptor.uuid
            val valueStr = descriptor.value.contentToString()
            UtilKLogWrapper.d(TAG, String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            UtilKLogWrapper.d("写入Descriptor[$uuid]:\n$valueStr")
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
            BluetoothKBle.instance.getBluetoothLeScanner()?.startScan(_scanCallback).also {
                _isScanning.compareAndSet(false, true)
            }
        }
    }

    override fun isScanning(): Boolean =
        _isScanning.get()

    @OptIn(OApiInit_InApplication::class)
    override fun cancelScan() {
        if (isScanning()) {
            BluetoothKBle.instance.getBluetoothLeScanner()?.stopScan(_scanCallback).also {
                _isScanning.compareAndSet(true, false)
            }
        }
//        _bluetoothDevices.clear()
    }

    override fun startBound(bluetoothDevice: BluetoothDevice) {
//        if (!_bluetoothDevices.contains(bluetoothDevice.address)) {
//            _bluetoothDevices[bluetoothDevice.address] = bluetoothDevice
//        }
        cancelBound()
        _bluetoothGatt = bluetoothDevice.connectGatt(_context, false, _bluetoothGattCallback)
    }

    fun cancelBound() {
        _bluetoothGatt?.disconnect()
        _bluetoothGatt?.close()
        _bluetoothGatt = null
    }

    //////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        cancelBound()
        cancelScan()
        _bluetoothKBleScanListener = null
        super.onDestroy(owner)
    }
}