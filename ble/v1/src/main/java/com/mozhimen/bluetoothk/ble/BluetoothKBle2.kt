package com.mozhimen.bluetoothk.ble

import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import com.mozhimen.bluetoothk.basic.BluetoothKBasic
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication

/**
 * @ClassName BluetoothKBle
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
@OApiInit_InApplication
class BluetoothKBle2 : BluetoothKBasic() {
    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = BluetoothKBle2()
    }
}