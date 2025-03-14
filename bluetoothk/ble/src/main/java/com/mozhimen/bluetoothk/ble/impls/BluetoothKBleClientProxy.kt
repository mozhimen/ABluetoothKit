package com.mozhimen.bluetoothk.ble.impls

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
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
class BluetoothKBleClientProxy : BaseWakeBefDestroyLifecycleObserver() {
    private var mBluetoothGatt: BluetoothGatt? = null

    // 与服务端连接的Callback
    @SuppressLint("MissingPermission")
    var mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val dev = gatt.device
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.name, dev.address, status, newState))
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true
                gatt.discoverServices() //启动服务发现
            } else {
                isConnected = false
                closeConn()
            }
            logTv(String.format(if (status == 0) (if (newState == 2) "与[%s]连接成功" else "与[%s]连接断开") else ("与[%s]连接出错,错误码:$status"), dev))
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i(TAG, String.format("onServicesDiscovered:%s,%s,%s", gatt.device.name, gatt.device.address, status))
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
                    Log.i(TAG, "onServicesDiscovered:$allUUIDs")
                    logTv("发现服务$allUUIDs")
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            Log.i(TAG, String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            logTv("读取Characteristic[$uuid]:\n$valueStr")
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            Log.i(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            logTv("写入Characteristic[$uuid]:\n$valueStr")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            Log.i(TAG, String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr))
            logTv("通知Characteristic[$uuid]:\n$valueStr")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            val uuid = descriptor.uuid
            val valueStr = descriptor.value.contentToString()
            Log.i(TAG, String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            logTv("读取Descriptor[$uuid]:\n$valueStr")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            val uuid = descriptor.uuid
            val valueStr = descriptor.value.contentToString()
            Log.i(TAG, String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            logTv("写入Descriptor[$uuid]:\n$valueStr")
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        closeConn()
        super.onDestroy(owner)
    }
    ///////////////////////////////////////////////////////////////////////////////

    // 扫描BLE
    fun reScan(view: View?) {
        if (mBleDevAdapter.isScanning) APP.toast("正在扫描...", 0)
        else mBleDevAdapter.reScan()
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 读取数据成功会回调->onCharacteristicChanged()
    fun read(view: View?) {
        val service = getGattService(BleServerActivity.UUID_SERVICE)
        if (service != null) {
            val characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY) //通过UUID获取可读的Characteristic
            mBluetoothGatt!!.readCharacteristic(characteristic)
        }
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    fun write(view: View?) {
        val service = getGattService(BleServerActivity.UUID_SERVICE)
        if (service != null) {
            val text = mWriteET!!.text.toString()
            val characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_WRITE) //通过UUID获取可写的Characteristic
            characteristic.setValue(text.toByteArray()) //单次最多20个字节
            mBluetoothGatt!!.writeCharacteristic(characteristic)
        }
    }

    // 设置通知Characteristic变化会回调->onCharacteristicChanged()
    fun setNotify(view: View?) {
        val service = getGattService(BleServerActivity.UUID_SERVICE)
        if (service != null) {
            // 设置Characteristic通知
            val characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY) //通过UUID获取可通知的Characteristic
            mBluetoothGatt!!.setCharacteristicNotification(characteristic, true)

            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
            val descriptor = characteristic.getDescriptor(BleServerActivity.UUID_DESC_NOTITY)
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    private fun closeConn() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt!!.disconnect()
            mBluetoothGatt!!.close()
        }
    }

    // 获取Gatt服务
    private fun getGattService(uuid: UUID): BluetoothGattService? {
        if (!isConnected) {
            APP.toast("没有连接", 0)
            return null
        }
        val service = mBluetoothGatt!!.getService(uuid)
        if (service == null) APP.toast("没有找到服务UUID=$uuid", 0)
        return service
    }
}