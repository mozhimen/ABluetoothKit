package com.mozhimen.bluetoothk.commons

import android.app.Activity
import com.mozhimen.kotlin.elemk.commons.IA_Listener

/**
 * @ClassName IBluetoothKProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
interface IBluetoothKProxy {
    fun setOnReadListener(listener: IA_Listener<String>)
    fun start(activity: Activity)
    fun write(str: String)
    fun stop()
}