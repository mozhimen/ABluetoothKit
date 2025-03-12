package com.mozhimen.bluetoothk.impls

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.cons.CBluetoothK
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.java.io.flushClose
import com.mozhimen.kotlin.utilk.java.util.UtilKUUID

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
class BluetoothKClientProxy : BaseWakeBefDestroyLifecycleObserver() {
    private var _thread: BluetoothKClientThread? = null

    fun startClient(mac: String, block: IA_Listener<String>) {
        if (_thread == null) {
            _thread = BluetoothKClientThread(BluetoothK.instance.getBluetoothAdapter(),mac,block)
        }
        if (!_thread!!.isInterrupted) {
            _thread!!.stopThread()
            _thread!!.interrupt()
        }
        _thread!!.start()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        _thread?.stopThread()
        _thread = null
        super.onDestroy(owner)
    }
}