package com.mozhimen.bluetoothk.ble.v2.impls

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.annors.AConnectState
import com.mozhimen.bluetoothk.ble.v2.cons.CBluetoothKBle2
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKProxy
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
import com.mozhimen.bluetoothk.ble.v2.BluetoothKBle2
import com.mozhimen.bluetoothk.ble.v2.commons.IBluetoothKBle2ClientListener
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothGatt
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.util.uUID2str
import com.mozhimen.kotlin.utilk.kotlin.bytes2str
import com.mozhimen.kotlin.utilk.kotlin.str2bytes
import com.mozhimen.kotlin.utilk.kotlin.str2uUID
import java.util.UUID

/**
 * @ClassName BluetoothKBleClientProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKBle2ClientProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKProxy<IBluetoothKBle2ClientListener> {
    private var _mac: String = ""
    private var _bluetoothGatt: BluetoothGatt? = null
    private var _iBluetoothKBleClientListener: IBluetoothKBle2ClientListener? = null
    private var _bluetoothState: Int = AConnectState.STATE_DISCONNECTED
        set(value) {
            when (value) {
                AConnectState.STATE_DISCONNECTED -> _iBluetoothKBleClientListener?.onDisConnected()
                AConnectState.STATE_CONNECTING -> _iBluetoothKBleClientListener?.onConnecting()
                AConnectState.STATE_CONNECTED -> _iBluetoothKBleClientListener?.onConnected()
                AConnectState.STATE_CONNECT_FAIL -> _iBluetoothKBleClientListener?.onConnectFail()
            }
            field = value
        }

    private val _bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            UtilKLogWrapper.d(TAG, "onConnectionStateChange: name ${gatt.device.name} address ${gatt.device.address} status $status newState $newState")
            UtilKLogWrapper.d(TAG, "onConnectionStateChange: ${if (status == 0) if (newState == 2) "连接成功" else "连接断开" else "连接出错, 错误码:$status"}")
            if (/*status == CBluetoothGatt.GATT_SUCCESS && */newState == CBluetoothGatt.STATE_CONNECTED) {
                UtilKLogWrapper.d(TAG, "onConnectionStateChange: connected")
                gatt.discoverServices().also {
                    _bluetoothState = AConnectState.STATE_CONNECTED
                }//启动服务发现
            } else if (newState == CBluetoothGatt.STATE_DISCONNECTED) {
                UtilKLogWrapper.d(TAG, "onConnectionStateChange: connect fail")
                stop().also {
                    _bluetoothState = AConnectState.STATE_DISCONNECTED
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            UtilKLogWrapper.d(TAG, "onServicesDiscovered: name ${gatt.device.name} address ${gatt.device.address} status $status")
            if (status == CBluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
//                for (bluetoothGattService in gatt.services) {
//                    val allUUIDs = StringBuilder("UUIDs={S=${bluetoothGattService.uuid}")
//                    for (characteristic in bluetoothGattService.characteristics) {
//                        allUUIDs.append(",C=").append(characteristic.uuid)
//                        for (descriptor in characteristic.descriptors)
//                            allUUIDs.append(",D=").append(descriptor.uuid)
//                        //
//                        UtilKLogWrapper.d(TAG, "onConnectionStateChange: uuid:${characteristic.uuid}")
//                        if (characteristic.uuid.uUID2str() == CBluetoothKBle2.UUID_BLE_SERVICE) {
//
//                        }
//                    }
//                    UtilKLogWrapper.d(TAG, "onConnectionStateChange: onServicesDiscovered:$allUUIDs")
//                    UtilKLogWrapper.d(TAG, "onConnectionStateChange: 发现服务$allUUIDs")
//                }
                _iBluetoothKBleClientListener?.onServicesDiscovered(gatt)
                //enable notification
//                enableNotification()
            }
        }

//        private fun enableNotification() {
//            if (_bluetoothGatt == null) {
//                _bluetoothState = AConnectState.STATE_CONNECT_FAIL
//                return
//            }
//
//            // 获取蓝牙设备的特征
//            val bluetoothGattCharacteristic: BluetoothGattCharacteristic? = getBluetoothGattCharacteristic(CBluetoothKBle2.UUID_BLE_CHARACTERISTIC_READ.str2uUID())
//            if (bluetoothGattCharacteristic == null) {
//                _bluetoothState = AConnectState.STATE_CONNECT_FAIL
//                return
//            }
//
//            // 获取蓝牙设备特征的描述符
//            val bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(CBluetoothKBle2.UUID_BLE_DESCRIPTOR.str2uUID()).apply {
//                setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//            }
//            if (_bluetoothGatt?.writeDescriptor(bluetoothGattDescriptor) == true) {
//                // 蓝牙设备在数据改变时，通知App，App在收到数据后回调onCharacteristicChanged方法
//                _bluetoothGatt?.setCharacteristicNotification(bluetoothGattCharacteristic, true)
//            }
//        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//            val strUUID = characteristic.uuid.uUID2str()
//            val strValue = characteristic.value.bytes2str()
//            UtilKLogWrapper.d(TAG, "onCharacteristicChanged: name ${gatt.device.name} address ${gatt.device.address} strUUID $strUUID strValue $strValue")
//            UtilKLogWrapper.d("onCharacteristicChanged: 通知Characteristic[$strUUID]:$strValue")
//            if (strUUID == CBluetoothKBle2.UUID_BLE_CHARACTERISTIC_READ) {
//                _onReadListener?.invoke(strValue)
//            }
            _iBluetoothKBleClientListener?.onCharacteristicChanged(gatt, characteristic)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//            val strUUID = characteristic.uuid.uUID2str()
//            val strValue = characteristic.value.bytes2str()
//            UtilKLogWrapper.d(TAG, "onCharacteristicRead: name ${gatt.device.name} address ${gatt.device.address} strUUID $strUUID strValue $strValue status $status")
//            UtilKLogWrapper.d("onCharacteristicRead: 读取Characteristic[$strUUID]:$strValue")
            _iBluetoothKBleClientListener?.onCharacteristicRead(gatt, characteristic)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//            val strUUID = characteristic.uuid.uUID2str()
//            val strValue = characteristic.value.bytes2str()
//            UtilKLogWrapper.d(TAG, "onCharacteristicWrite: name ${gatt.device.name} address ${gatt.device.address} strUUID $strUUID strValue $strValue status $status")
//            UtilKLogWrapper.d("onCharacteristicWrite: 写入Characteristic[$strUUID]:$strValue")
            _iBluetoothKBleClientListener?.onCharacteristicWrite(gatt, characteristic)
        }

        @Deprecated("Deprecated in Java")
        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
//            val strUUID = descriptor.uuid.uUID2str()
//            val strValue = descriptor.value.contentToString()
//            UtilKLogWrapper.d(TAG, "onDescriptorRead: name ${gatt.device.name} address ${gatt.device.address} strUUID $strUUID strValue $strValue status $status")
//            UtilKLogWrapper.d("onDescriptorRead: 读取Descriptor[$strUUID]:$strValue")
            _iBluetoothKBleClientListener?.onDescriptorRead(gatt, descriptor)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
//            val strUUID = descriptor.uuid.uUID2str()
//            val strValue = descriptor.value.contentToString()
//            UtilKLogWrapper.d(TAG, "onDescriptorWrite: name ${gatt.device.name} address ${gatt.device.address} strUUID $strUUID strValue $strValue status $status")
//            UtilKLogWrapper.d("onDescriptorWrite: 写入Descriptor[$strUUID]:$strValue")
            _iBluetoothKBleClientListener?.onDescriptorWrite(gatt, descriptor)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    // 获取Gatt服务
    fun getBluetoothGattService(uuid: UUID): BluetoothGattService? =
        _bluetoothGatt?.getService(uuid)

    fun getBluetoothGattCharacteristic(uuid: UUID): BluetoothGattCharacteristic? =
        getBluetoothGattService(uuid)?.getCharacteristic(uuid)

    ///////////////////////////////////////////////////////////////////////////////

    fun setMac(mac: String) {
        _mac = mac
    }

    override fun setListener(listener: IBluetoothKBle2ClientListener) {
        _iBluetoothKBleClientListener = listener
    }

    @OptIn(OApiInit_InApplication::class)
    override fun start(activity: Activity) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            if (BluetoothKBle2.instance.getBluetoothAdapter() != null && _mac.isNotEmpty()) {
                val bluetoothDevice: BluetoothDevice? = BluetoothKBle2.instance.getBluetoothAdapter()?.getRemoteDevice(_mac)
                if (bluetoothDevice != null) {
                    _bluetoothGatt = bluetoothDevice.connectGatt(_context, false, _bluetoothGattCallback).also {
                        _bluetoothState = AConnectState.STATE_CONNECTING
                    }
                }
            }
        })
    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    override fun stop() {
        _bluetoothGatt?.disconnect()
        _bluetoothGatt?.close()
        _bluetoothGatt = null
    }

    ///////////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
        _iBluetoothKBleClientListener = null
        super.onDestroy(owner)
    }

    ///////////////////////////////////////////////////////////////////////////////


    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 读取数据成功会回调->onCharacteristicChanged()
    fun read(): String {
        val bluetoothGattCharacteristic = getBluetoothGattCharacteristic(CBluetoothKBle2.UUID_BLE_CHARACTERISTIC_READ.str2uUID()) //通过UUID获取可读的Characteristic
        if (bluetoothGattCharacteristic != null) {
            _bluetoothGatt?.readCharacteristic(bluetoothGattCharacteristic)
            return bluetoothGattCharacteristic.value?.bytes2str() ?: ""
        }
        return ""
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    override fun write(str: String) {
        val bluetoothGattCharacteristic = getBluetoothGattCharacteristic(CBluetoothKBle2.UUID_BLE_CHARACTERISTIC_WRITE.str2uUID())//通过UUID获取可写的Characteristic
        if (bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic.setValue(str.str2bytes()) //单次最多20个字节
            _bluetoothGatt?.writeCharacteristic(bluetoothGattCharacteristic)
        }
    }

    // 设置通知Characteristic变化会回调->onCharacteristicChanged()
//    fun notify() {
//        val service = getBluetoothGattService(CBluetoothKBle.UUID_SERVICE.str2uUID())
//        if (service != null) {
//            // 设置Characteristic通知
//            val characteristic = service.getCharacteristic(CBluetoothKBle.UUID_CHARACTERISTIC_READ.str2uUID()) //通过UUID获取可通知的Characteristic
//            _bluetoothGatt?.setCharacteristicNotification(characteristic, true)
//
//            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
//            val descriptor = characteristic.getDescriptor(CBluetoothKBle.UUID_DESCRIPTOR.str2uUID())
//            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//            _bluetoothGatt?.writeDescriptor(descriptor)
//        }
//    }
}