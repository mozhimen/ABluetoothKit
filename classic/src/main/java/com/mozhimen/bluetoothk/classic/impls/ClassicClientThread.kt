package com.mozhimen.bluetoothk.classic.impls

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.mozhimen.bluetoothk.classic.bases.BaseClassicThread
import com.mozhimen.bluetoothk.classic.cons.CBluetoothKClassic
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.str2uUID


/**
 * @ClassName BluetoothhKClientThread
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/11 21:28
 * @Version 1.0
 */
class ClassicClientThread : BaseClassicThread {

    @JvmOverloads
    constructor(bluetoothDevice: BluetoothDevice, clearText: Boolean = false, onReadListener: IA_Listener<String>? = null) {
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = if (!clearText) {
                bluetoothDevice.createRfcommSocketToServiceRecord(CBluetoothKClassic.UUID.str2uUID())
            } else {
                bluetoothDevice.createInsecureRfcommSocketToServiceRecord(CBluetoothKClassic.UUID.str2uUID())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "constructor: createRfcommSocketToServiceRecord fail ${e.message}")
        }
        _bluetoothSocket = bluetoothSocket
        _onReadListener = onReadListener
    }

    @JvmOverloads
    constructor(bluetoothAdapter: BluetoothAdapter, address: String, clearText: Boolean = false, onReadListener: IA_Listener<String>? = null) {
        val bluetoothDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address) ?: return
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = if (!clearText) {
                bluetoothDevice.createRfcommSocketToServiceRecord(CBluetoothKClassic.UUID.str2uUID())
            } else {
                bluetoothDevice.createInsecureRfcommSocketToServiceRecord(CBluetoothKClassic.UUID.str2uUID())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _bluetoothSocket = bluetoothSocket
        _onReadListener = onReadListener
    }

    ///////////////////////////////////////////////////////////////////////

    override fun run() {
        _run = true
        if (_bluetoothSocket == null) {
            UtilKLogWrapper.d(TAG, "run: _bluetoothSocket == null")
            return
        }
        startRead(_bluetoothSocket!!)
    }
}
