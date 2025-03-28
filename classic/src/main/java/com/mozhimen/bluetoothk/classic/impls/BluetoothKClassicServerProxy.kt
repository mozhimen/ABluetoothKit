package com.mozhimen.bluetoothk.classic.impls

import com.mozhimen.bluetoothk.classic.BluetoothKClassic
import com.mozhimen.bluetoothk.classic.bases.BaseClassicProxy
import com.mozhimen.bluetoothk.classic.bases.BaseClassicThread
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication

/**
 * @ClassName BluetoothKServerProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/11
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKClassicServerProxy : BaseClassicProxy() {
    @OptIn(OApiInit_InApplication::class)
    override fun getThread(): BaseClassicThread? {
        return if (BluetoothKClassic.instance.getBluetoothAdapter() != null) {
            BluetoothKClassicServerThread(BluetoothKClassic.instance.getBluetoothAdapter()!!, false, _onReadListener)
        } else null
    }
}