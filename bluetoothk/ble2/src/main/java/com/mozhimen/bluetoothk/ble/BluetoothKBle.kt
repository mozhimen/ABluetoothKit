package com.mozhimen.bluetoothk.ble

import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.kotlin.str2uUID
import java.util.UUID

/**
 * @ClassName BluetoothKBle
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
@OApiInit_InApplication
class BluetoothKBle : BluetoothK() {

    fun getBluetoothLeScanner(): BluetoothLeScanner? =
        getBluetoothAdapter()?.bluetoothLeScanner

    fun getBluetoothLeAdvertiser(): BluetoothLeAdvertiser? =
        getBluetoothAdapter()?.bluetoothLeAdvertiser

    ///////////////////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = BluetoothKBle()
    }
}