package com.mozhimen.bluetoothk.ble.androidx.impls

import android.app.Activity
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.GattClientScope
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKProxy
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
import com.mozhimen.bluetoothk.ble.androidx.BluetoothKBleX
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.elemk.commons.IExt_AListener
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

/**
 * @ClassName BluetoothKBleXClientProxy
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/26 20:23
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKBleXClientProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKProxy<IA_Listener<String>> {
    private var _mac: String = ""
    private var _iListener: IA_Listener<String>? = null

    ///////////////////////////////////////////////////////////////////////////////

    fun setMac(mac: String) {
        _mac = mac
    }

    override fun setListener(listener: IA_Listener<String>) {
        _iListener = listener
    }

    fun <R> start(activity: Activity, block: IExt_AListener<GattClientScope, R>) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            if (_mac.isNotEmpty()
                && BluetoothKBleX.instance.getScanResult(_mac) != null
                && BluetoothKBleX.instance.getConnectJob(_mac) == null
            ) {
                BluetoothKBleX.instance.addConnectJob(_mac, BluetoothKBleX.instance.getScope().launch {
                    BluetoothLe(activity.applicationContext).connectGatt(BluetoothKBleX.instance.getBluetoothDevice(_mac)!!) {
                        BluetoothKBleX.instance.addGattClientScope(_mac, this)
                        this.block()
                        awaitCancellation()
                    }
                })
            }
        })
    }

    override fun stop() {
        if (_mac.isNotEmpty()) {
            BluetoothKBleX.instance.removeClientScope(_mac)
            BluetoothKBleX.instance.removeConnectJob(_mac)?.cancel()
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
        super.onDestroy(owner)
    }
}