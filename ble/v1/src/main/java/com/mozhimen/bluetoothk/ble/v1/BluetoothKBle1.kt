package com.mozhimen.bluetoothk.ble.v1

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
class BluetoothKBle1 : BluetoothKBasic() {
    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = BluetoothKBle1()
    }
}