package com.mozhimen.bluetoothk.basic

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKStateListener
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothDevice
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName BluetoothKBasic
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/25
 * @Version 1.0
 */
@OApiInit_InApplication
open class BluetoothKBasic : IUtilK {
    private var _bluetoothKEventListeners: CopyOnWriteArrayList<IBluetoothKStateListener> = CopyOnWriteArrayList<IBluetoothKStateListener>()
    private var _bluetoothAdapter: BluetoothAdapter? = null
        get() {
            if (field != null) return field
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return bluetoothAdapter.also { field = it }
        }
    private var _isInit = AtomicBoolean(false)
    private val _btReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                CBluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    /* if (PrinterHelper.IsOpened()) {PrinterHelper.portClose()}*/
                    UtilKLogWrapper.d(TAG, "_btReceiver: ACTION_ACL_DISCONNECTED")
                    _bluetoothKEventListeners.forEach { it.onDisconnect() }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED->{
                    when (intent.getIntExtra(CBluetoothAdapter.EXTRA_STATE, CBluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_OFF -> {
                            /* if (PrinterHelper.IsOpened()) {PrinterHelper.portClose()}*/
                            UtilKLogWrapper.d(TAG, "_btReceiver: STATE_OFF")
                            _bluetoothKEventListeners.forEach { it.onOff() }
                        }

                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            UtilKLogWrapper.d(TAG, "_btReceiver: STATE_TURNING_OFF")
                            _bluetoothKEventListeners.forEach { it.onTurningOff() }
                        }

                        BluetoothAdapter.STATE_ON -> {
                            UtilKLogWrapper.d(TAG, "_btReceiver: STATE_ON")
                            _bluetoothKEventListeners.forEach { it.onOn() }
                        }

                        BluetoothAdapter.STATE_TURNING_ON -> {
                            UtilKLogWrapper.d(TAG, "_btReceiver: STATE_TURNING_ON")
                            _bluetoothKEventListeners.forEach { it.onTurningOn() }
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    fun init(context: Context) {
        if (_isInit.compareAndSet(false, true)) {
            val intentFilterBt = IntentFilter()
            intentFilterBt.addAction(CBluetoothDevice.ACTION_ACL_DISCONNECTED)
            intentFilterBt.addAction(CBluetoothAdapter.ACTION_STATE_CHANGED)
            context.registerReceiver(_btReceiver, intentFilterBt)
        }
    }

    fun addBluetoothKEventListener(listener: IBluetoothKStateListener) {
        if (!_bluetoothKEventListeners.contains(listener))
            _bluetoothKEventListeners.add(listener)
    }

    fun removeBluetoothKEventListener(listener: IBluetoothKStateListener) {
        if (_bluetoothKEventListeners.contains(listener))
            _bluetoothKEventListeners.remove(listener)
    }

    fun clearBluetoothKEventListeners() {
        _bluetoothKEventListeners.clear()
    }

    fun release(context: Context) {
        context.unregisterReceiver(_btReceiver)
        clearBluetoothKEventListeners()
    }

    fun isBluetoothEnabled(): Boolean =
        getBluetoothAdapter()?.isEnabled == true

    fun getBluetoothAdapter(): BluetoothAdapter? =
        _bluetoothAdapter
}