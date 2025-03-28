package com.mozhimen.bluetoothk.basic.commons

import android.app.Activity

/**
 * @ClassName IBluetoothKProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
interface IBluetoothKProxy<I> {
    fun setListener(listener: I){}
    fun start(activity: Activity){}
    fun write(str: String){}
    fun stop(){}
}