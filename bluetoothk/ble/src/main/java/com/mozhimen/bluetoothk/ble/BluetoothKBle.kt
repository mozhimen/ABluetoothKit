package com.mozhimen.bluetoothk.ble

import android.bluetooth.le.BluetoothLeScanner
import androidx.annotation.RequiresApi
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.kotlin.elemk.android.os.cons.CVersCode
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication

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

    ///////////////////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = BluetoothKBle()
    }
}