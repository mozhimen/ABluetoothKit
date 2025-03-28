package com.mozhimen.bluetoothk.ble.v1.commons

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

/**
 * @ClassName IBluetoothKBle2ClientListener
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/25
 * @Version 1.0
 */
interface IBluetoothKBle1ClientListener {
    fun onDisConnected() {}
    fun onConnecting() {}
    fun onConnected() {}
    fun onConnectFail() {}
    fun onServicesDiscovered(gatt: BluetoothGatt) {}
    fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)
    fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)
    fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)
    fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor)
    fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor)
}