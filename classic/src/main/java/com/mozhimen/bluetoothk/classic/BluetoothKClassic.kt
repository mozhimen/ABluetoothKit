package com.mozhimen.bluetoothk.classic

import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.commons.IUtilK

/**
 * @ClassName BluetoothK
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/19
 * @Version 1.0
 */
@OApiInit_InApplication
open class BluetoothKClassic : IUtilK {
    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = BluetoothKClassic()
    }
}