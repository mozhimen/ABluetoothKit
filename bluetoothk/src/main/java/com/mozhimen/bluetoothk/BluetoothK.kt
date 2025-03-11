package com.mozhimen.bluetoothk

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.mozhimen.basick.utils.requireContext
import com.mozhimen.bluetoothk.commons.IBluetoothKScanListener
import com.mozhimen.bluetoothk.commons.IBluetoothKStateListener
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothDevice
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_COARSE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_FINE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_ADVERTISE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_CONNECT
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_SCAN
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.permissionk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsNavHostUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsRequestUtil
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @ClassName BluetoothK
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/19
 * @Version 1.0
 */
@OApiInit_InApplication
class BluetoothK : IUtilK {
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
                    /*                try {
                    PrinterHelper.portClose()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                vdb.txtTips.setText(R.string.activity_main_tips)*/
                    UtilKLogWrapper.d(TAG, "_btReceiver: ACTION_ACL_DISCONNECTED")
                    _bluetoothKEventListeners.forEach { it.onDisconnect() }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED->{
                    when (intent.getIntExtra(CBluetoothAdapter.EXTRA_STATE, CBluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_OFF -> {
                            /*                        if (PrinterHelper.IsOpened()) {
                                Log.d("Print", "BluetoothBroadcastReceiver:Bluetooth close")
                                try {
                                    PrinterHelper.portClose()
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                                vdb.txtTips.setText(R.string.activity_main_tips)
                                Utility.show(this@MainActivity, getString(R.string.activity_main_close))
                            }*/
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
        _bluetoothAdapter?.isEnabled == true

    fun getBluetoothAdapter(): BluetoothAdapter? =
        _bluetoothAdapter

    ///////////////////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = BluetoothK()
    }
}