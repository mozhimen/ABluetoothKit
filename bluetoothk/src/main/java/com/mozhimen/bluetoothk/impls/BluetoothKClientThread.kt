package com.mozhimen.bluetoothk.impls

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.mozhimen.bluetoothk.bases.BaseBluetoothKThread
import com.mozhimen.bluetoothk.cons.CBluetoothK
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.java.util.UtilKUUID
import com.mozhimen.kotlin.utilk.kotlin.bytes2str
import com.mozhimen.kotlin.utilk.kotlin.str2uUID


/**
 * @ClassName BluetoothhKClientThread
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/11 21:28
 * @Version 1.0
 */
class BluetoothKClientThread : BaseBluetoothKThread {

    @JvmOverloads
    constructor(bluetoothDevice: BluetoothDevice, clearText: Boolean = false, onReadListener: IA_Listener<String>? = null) {
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = if (!clearText) {
                bluetoothDevice.createRfcommSocketToServiceRecord(CBluetoothK.UUID.str2uUID())
            } else {
                bluetoothDevice.createInsecureRfcommSocketToServiceRecord(CBluetoothK.UUID.str2uUID())
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
                bluetoothDevice.createRfcommSocketToServiceRecord(CBluetoothK.UUID.str2uUID())
            } else {
                bluetoothDevice.createInsecureRfcommSocketToServiceRecord(CBluetoothK.UUID.str2uUID())
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
