package com.mozhimen.bluetoothk.ble2.exam

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

/**
 * @ClassName CBle2
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/25
 * @Version 1.0
 */
object CBle2 {
    var gattCharacteristic_write: BluetoothGattCharacteristic? = null
    var gattCharacteristic_notify: BluetoothGattCharacteristic? = null

    val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID: UUID = UUID
        .fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val UUID_UNLOCK_DATA_SERVICE: String = "0000fff0-0000-1000-8000-00805f9b34fb"
    const val UUID_UNLOCK_DATA_NOTIFY: String = "0000fff1-0000-1000-8000-00805f9b34fb"
    const val UUID_UNLOCK_DATA_WRITE: String = "0000fff2-0000-1000-8000-00805f9b34fb"
    const val ACTION_BLE_GET_DATA: String = "com.senssunhealth.ACTION_BLE_GET_DATA"
    const val ACTION_BLE_MANUFACTIRE_DATA: String = "com.senssunhealth.ACTION_BLE_MANUFACTIRE_DATA"
    const val ACTION_BLE_CONNECT_STATE: String = "com.senssunhealth.ACTION_BLE_CONNECT_STATE"

    const val TYPE_DATA_ALL_USERINFO: String = "com.senssunhealth.TYPE_DATA_USERINFOS"
    const val TYPE_DATA_FAT_RATIO: String = "com.senssunhealth.TYPE_DATA_USERINFOS"
    const val TYPE_DATA_TEMP_WEIGHT: String = "com.senssunhealth.TYPE_DATA_TEMP_WEIGHT"

    //体重秤与体脂秤数据传输协议  03xx
    const val FUCTION_NUM: Int = 0x03

    //智能终端至硬件设备的数据请求／应答  data 00（正常接收） /0a(中断)
    const val REQUEST_APP_TO_HARDWARE: Int = 0x00

    //硬件设备至智能终端的应答／请求
    const val REPONSE_HARDWARE_TO_APP: Int = 0x80

    const val STATE_DISCONNECTED = 0
    const val STATE_CONNECTING = 1
    const val STATE_CONNECTED = 2

    const val OPTION_SYNC_DATA_DEFAULED: Byte = 0x00
    const val OPTION_DELETE_USER_DATA: Byte = 0xaa.toByte()
}