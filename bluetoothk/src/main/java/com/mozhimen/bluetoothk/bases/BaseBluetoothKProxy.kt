package com.mozhimen.bluetoothk.bases

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper

/**
 * @ClassName BaseBluetoothLProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/12
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
abstract class BaseBluetoothKProxy : BaseWakeBefDestroyLifecycleObserver() {
    protected var _onReadListener: IA_Listener<String>? = null
    private var _thread: BaseBluetoothKThread? = null

    ///////////////////////////////////////////////////////////////////

    abstract fun getThread(): BaseBluetoothKThread?

    ///////////////////////////////////////////////////////////////////

    fun setOnReadListener(listener: IA_Listener<String>) {
        _onReadListener = listener
    }

    fun write(str: String) {
        _thread?.write(str) ?: run {
            UtilKLogWrapper.e(TAG, "write: _thread is null")
        }
    }

    fun start() {
        if (_thread != null) {
            UtilKLogWrapper.d(TAG, "start: _thread != null")
            return
        }
        _thread = getThread()
        _thread!!.start()
    }

    fun stop() {
        _thread?.cancel()
        _thread?.interrupt()
        _thread = null
    }

    ///////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
        super.onDestroy(owner)
    }
}