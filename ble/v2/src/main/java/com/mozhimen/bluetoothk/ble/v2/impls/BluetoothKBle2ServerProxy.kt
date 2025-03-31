//package com.mozhimen.bluetoothk.ble.v2.impls
//
//import android.app.Activity
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothGatt
//import android.bluetooth.BluetoothGattCharacteristic
//import android.bluetooth.BluetoothGattDescriptor
//import android.bluetooth.BluetoothGattServer
//import android.bluetooth.BluetoothGattServerCallback
//import android.bluetooth.BluetoothGattService
//import android.bluetooth.le.AdvertiseCallback
//import android.bluetooth.le.AdvertiseData
//import android.bluetooth.le.AdvertiseSettings
//import android.bluetooth.le.BluetoothLeAdvertiser
//import android.os.ParcelUuid
//import androidx.lifecycle.LifecycleOwner
//import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
//import com.mozhimen.bluetoothk.ble.BluetoothKBle
//import com.mozhimen.bluetoothk.basic.annors.AConnectState
//import com.mozhimen.bluetoothk.basic.commons.IBluetoothKProxy
//import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
//import com.mozhimen.bluetoothk.ble.v2.cons.CBluetoothKBle2
//import com.mozhimen.kotlin.elemk.commons.IA_Listener
//import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
//import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
//import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
//import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
//import com.mozhimen.kotlin.utilk.android.bluetooth.UtilKBluetoothManager
//import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
//import com.mozhimen.kotlin.utilk.kotlin.bytes2str
//import com.mozhimen.kotlin.utilk.kotlin.str2uUID
//
///**
// * @ClassName BluetoothKBleClientProxy
// * @Description TODO
// * @Author mozhimen
// * @Date 2025/3/14
// * @Version 1.0
// */
//@OApiInit_ByLazy
//@OApiCall_BindLifecycle
//@OApiCall_BindViewLifecycle
//class BluetoothKBle2ServerProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKProxy<IA_Listener<String>> {
//    private var _bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null // BLE广播
//    private var _bluetoothGattServer: BluetoothGattServer? = null // BLE服务端
//    private var _onReadListener: IA_Listener<String>? = null
//    private var _bluetoothState: Int = AConnectState.STATE_DISCONNECTED
//
//    // BLE广播Callback
//    private val _advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
//        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
//            UtilKLogWrapper.d(TAG, "onStartSuccess: BLE广播开启成功")
//            _bluetoothState = AConnectState.STATE_CONNECTED
//        }
//
//        override fun onStartFailure(errorCode: Int) {
//            UtilKLogWrapper.d(TAG, "onStartFailure: BLE广播开启失败,错误码:$errorCode")
//            _bluetoothState = AConnectState.STATE_CONNECT_FAIL
//        }
//    }
//
//    // BLE服务端Callback
//    private val _bluetoothGattServerCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback() {
//        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
//            UtilKLogWrapper.d(TAG, "onConnectionStateChange:${device.name},${device.address},${status},${newState}")
//            UtilKLogWrapper.d(
//                TAG,
//                if (status == 0)
//                    if (newState == 2)
//                        "与[${device}]连接成功"
//                    else
//                        "与[${device}]连接断开"
//                else
//                    "与[${device}]连接出错,错误码:$status"
//            )
//        }
//
//        override fun onServiceAdded(status: Int, service: BluetoothGattService) {
//            UtilKLogWrapper.d(TAG, "onServiceAdded:${status},${service.uuid}")
//            UtilKLogWrapper.d(
//                TAG,
//                if (status == 0)
//                    "添加服务[${service.uuid}]成功"
//                else
//                    "添加服务[${service.uuid}]失败,错误码:$status"
//            )
//        }
//
//        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
//            UtilKLogWrapper.d(TAG, "onCharacteristicReadRequest: ${device.name} ${device.address} ${requestId} ${offset} ${characteristic.uuid}")
//            _bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.value) // 响应客户端
//            UtilKLogWrapper.d(TAG, "onCharacteristicReadRequest: 客户端读取Characteristic[${characteristic.uuid}]: ${characteristic.value}")
//        }
//
//        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, requestBytes: ByteArray) {
//            // 获取客户端发过来的数据
//            UtilKLogWrapper.d(TAG, "onCharacteristicWriteRequest: ${device.name} ${device.address} $requestId ${characteristic.uuid} $preparedWrite $responseNeeded $offset ${requestBytes.bytes2str()}")
//            _bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes) // 响应客户端
//            UtilKLogWrapper.d(TAG, "onCharacteristicWriteRequest: 客户端写入Characteristic[${characteristic.uuid}]:${requestBytes.bytes2str()}")
//
//            characteristic.setValue(requestBytes)
//            _bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, false);
//            _onReadListener?.invoke(requestBytes.bytes2str())
//        }
//
//        override fun onDescriptorReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor) {
//            UtilKLogWrapper.d(TAG, "onDescriptorReadRequest: ${device.name} ${device.address} $requestId $offset ${descriptor.uuid}")
//            _bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null) // 响应客户端
//            UtilKLogWrapper.d(TAG, "客户端读取Descriptor[${descriptor.uuid}]:null")
//        }
//
//        override fun onDescriptorWriteRequest(device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {
//            // 获取客户端发过来的数据
//            val valueStr = value.contentToString()
//            UtilKLogWrapper.d(TAG, "onDescriptorWriteRequest: ${device.name} ${device.address} $requestId ${descriptor.uuid} $preparedWrite $responseNeeded $offset $valueStr")
//            _bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value) // 响应客户端
//            UtilKLogWrapper.d(TAG, "onDescriptorWriteRequest: 客户端写入Descriptor[${descriptor.uuid}]:$valueStr")
//        }
//
//        override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
//            UtilKLogWrapper.d(TAG, "onExecuteWrite: ${device.name} ${device.address} $requestId $execute")
//        }
//
//        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
//            UtilKLogWrapper.d(TAG, "onNotificationSent: ${device.name} ${device.address} $status")
//        }
//
//        override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
//            UtilKLogWrapper.d(TAG, "onMtuChanged: ${device.name} ${device.address} $mtu")
//        }
//    }
//
//    ///////////////////////////////////////////////////////////////////////////////
//
//    override fun setListener(listener: IA_Listener<String>) {
//        _onReadListener = listener
//    }
//
//    @OptIn(OApiInit_InApplication::class)
//    override fun start(activity: Activity) {
//        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
//            if (BluetoothKBle.instance.getBluetoothLeAdvertiser() != null) {
//                _bluetoothLeAdvertiser = BluetoothKBle.instance.getBluetoothLeAdvertiser()
//                //广播设置(必须)
//                val advertiseSettings = AdvertiseSettings.Builder()
//                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //广播模式: 低功耗,平衡,低延迟
//                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //发射功率级别: 极低,低,中,高
//                    .setTimeout(0)
//                    .setConnectable(true) //能否连接,广播分为可连接广播和不可连接广播
//                    .build()
//                //广播数据(必须，广播启动就会发送)
//                val advertiseData = AdvertiseData.Builder()
//                    .setIncludeDeviceName(true) //包含蓝牙名称
//                    .setIncludeTxPowerLevel(true) //包含发射功率级别
//                    .addManufacturerData(1, byteArrayOf(23, 33)) //设备厂商数据，自定义
//                    .build()
//
//                //扫描响应数据(可选，当客户端扫描时才发送)
//                val scanResponse = AdvertiseData.Builder()
//                    .addManufacturerData(2, byteArrayOf(66, 66)) //设备厂商数据，自定义
//                    .addServiceUuid(ParcelUuid(CBluetoothKBle2.UUID_BLE_SERVICE.str2uUID())) //服务UUID
//                    //                .addServiceData(new ParcelUuid(UUID_SERVICE), new byte[]{2}) //服务数据，自定义
//                    .build()
//                _bluetoothLeAdvertiser!!.startAdvertising(advertiseSettings, advertiseData, scanResponse, _advertiseCallback).also {
//                    _bluetoothState = AConnectState.STATE_CONNECTING
//                }
//
//                // 注意：必须要开启可连接的BLE广播，其它设备才能发现并连接BLE服务端!
//                // =============启动BLE蓝牙服务端=====================================================================================
//                val bluetoothManager = UtilKBluetoothManager.get(activity.applicationContext)
//                val bluetoothGattService = BluetoothGattService(CBluetoothKBle2.UUID_BLE_SERVICE.str2uUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY)
//
//                //添加可读+通知characteristic
//                bluetoothGattService.addCharacteristic(
//                    BluetoothGattCharacteristic(CBluetoothKBle2.UUID_BLE_CHARACTERISTIC_READ.str2uUID(), BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ).apply {
//                        addDescriptor(BluetoothGattDescriptor(CBluetoothKBle2.UUID_BLE_DESCRIPTOR.str2uUID(), BluetoothGattCharacteristic.PERMISSION_WRITE))
//                    }
//                )
//
//                //添加可写characteristic
//                bluetoothGattService.addCharacteristic(
//                    BluetoothGattCharacteristic(CBluetoothKBle2.UUID_BLE_CHARACTERISTIC_WRITE.str2uUID(), BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE)
//                )
//                _bluetoothGattServer = bluetoothManager.openGattServer(activity.applicationContext, _bluetoothGattServerCallback)
//                _bluetoothGattServer!!.addService(bluetoothGattService)
//            }
//        })
//    }
//
//    override fun write(str: String) {
//
//    }
//
//    override fun stop() {
//        _bluetoothLeAdvertiser?.stopAdvertising(_advertiseCallback)
//        _bluetoothGattServer?.close()
//    }
//
//    ///////////////////////////////////////////////////////////////////////////////
//
//    override fun onDestroy(owner: LifecycleOwner) {
//        stop()
//        super.onDestroy(owner)
//    }
//}