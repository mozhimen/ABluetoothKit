package com.mozhimen.bluetoothk.classic.bases

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKProxy
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
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
abstract class BaseClassicProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKProxy<IA_Listener<String>> {
    protected var _onReadListener: IA_Listener<String>? = null
    private var _thread: BaseClassicThread? = null

    ///////////////////////////////////////////////////////////////////

    abstract fun getThread(): BaseClassicThread?

    ///////////////////////////////////////////////////////////////////

    override fun setListener(listener: IA_Listener<String>) {
        _onReadListener = listener
    }

    override fun write(str: String) {
        _thread?.write(str) ?: run {
            UtilKLogWrapper.e(TAG, "write: _thread is null")
        }
    }

    override fun start(activity: Activity) {
        UtilBluetooth.requestBluetoothPermission(activity, onGranted = {
            if (_thread != null) {
                UtilKLogWrapper.d(TAG, "start: _thread != null")
                return@requestBluetoothPermission
            }
            _thread = getThread()
            _thread!!.start()
        })
    }

    override fun stop() {
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