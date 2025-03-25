package com.mozhimen.bluetoothk.classic.impls

import com.mozhimen.bluetoothk.classic.BluetoothKClassic
import com.mozhimen.bluetoothk.classic.bases.BaseClassicProxy
import com.mozhimen.bluetoothk.classic.bases.BaseClassicThread
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication

/**
 * @ClassName BluetoothKClientProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/11
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class ClassicClientProxy : BaseClassicProxy() {

    private var _mac: String? = null

    fun setMac(mac: String) {
        _mac = mac
    }

    @OptIn(OApiInit_InApplication::class)
    override fun getThread(): BaseClassicThread? {
        return if (BluetoothKClassic.instance.getBluetoothAdapter() != null && !_mac.isNullOrEmpty()) {
            ClassicClientThread(BluetoothKClassic.instance.getBluetoothAdapter()!!, _mac!!, false, _onReadListener)
        } else null
    }
}