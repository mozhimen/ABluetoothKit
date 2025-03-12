package com.mozhimen.bluetoothk.impls

import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.bases.BaseBluetoothKProxy
import com.mozhimen.bluetoothk.bases.BaseBluetoothKThread
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
class BluetoothKServerProxy : BaseBluetoothKProxy() {
    @OptIn(OApiInit_InApplication::class)
    override fun getThread(): BaseBluetoothKThread? {
        return if (BluetoothK.instance.getBluetoothAdapter() != null) {
            BluetoothKServerThread(BluetoothK.instance.getBluetoothAdapter()!!, false, _onReadListener)
        } else null
    }
}