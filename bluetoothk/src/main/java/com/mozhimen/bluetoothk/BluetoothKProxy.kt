package com.mozhimen.bluetoothk

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.basick.utils.requireContext
import com.mozhimen.bluetoothk.commons.IBluetoothKEventListener
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothDevice
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper

/**
 * @ClassName BluetoothK
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/18
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKProxy : BaseWakeBefDestroyLifecycleObserver() {
    companion object {

    }

    private var _bluetoothKEventListener: IBluetoothKEventListener? = null


    private val _btReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (CBluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
/*                try {
                    PrinterHelper.portClose()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                vdb.txtTips.setText(R.string.activity_main_tips)*/
                UtilKLogWrapper.d(TAG, "_btReceiver: ACTION_ACL_DISCONNECTED")
                _bluetoothKEventListener?.onDisconnect()
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                val state = intent.getIntExtra(
                    CBluetoothAdapter.EXTRA_STATE,
                    CBluetoothAdapter.ERROR
                )
                when (state) {
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
                        _bluetoothKEventListener?.onClose()
                    }

                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        UtilKLogWrapper.d(TAG, "_btReceiver: STATE_TURNING_OFF")
                        _bluetoothKEventListener?.onClosing()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        UtilKLogWrapper.d(TAG, "_btReceiver: STATE_ON")
                        _bluetoothKEventListener?.onOpen()
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {
                        UtilKLogWrapper.d(TAG, "_btReceiver: STATE_TURNING_ON")
                        _bluetoothKEventListener?.onOpening()
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////

    fun setBluetoothKEventListener(listener: IBluetoothKEventListener) {
        _bluetoothKEventListener = listener
    }

    ////////////////////////////////////////////////////////////////////

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        //bt
        val intentFilterBt = IntentFilter()
        intentFilterBt.addAction(CBluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilterBt.addAction(CBluetoothAdapter.ACTION_STATE_CHANGED)
        owner.requireContext().registerReceiver(_btReceiver, intentFilterBt)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        //bt
        owner.requireContext().unregisterReceiver(_btReceiver)
        super.onDestroy(owner)
    }
}