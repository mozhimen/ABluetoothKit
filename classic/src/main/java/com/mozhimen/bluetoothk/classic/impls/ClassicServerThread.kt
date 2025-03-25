package com.mozhimen.bluetoothk.classic.impls

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
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
class ClassicServerThread : BaseClassicThread {

    private var _bluetoothServerSocket: BluetoothServerSocket? = null

    @JvmOverloads
    constructor(bluetoothAdapter: BluetoothAdapter, clearText: Boolean = false, onReadListener: IA_Listener<String>? = null) {
        var bluetoothServerSocket: BluetoothServerSocket? = null
        try {
            bluetoothServerSocket = if (!clearText)
                bluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG, CBluetoothKClassic.UUID.str2uUID())
            else
                bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(TAG, CBluetoothKClassic.UUID.str2uUID())
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "constructor: createRfcommSocketToServiceRecord fail ${e.message}")
        }
        _bluetoothServerSocket = bluetoothServerSocket
        _onReadListener = onReadListener
    }

    ///////////////////////////////////////////////////////////////////////

    override fun run() {
        _run = true
        if (_bluetoothServerSocket == null) {
            UtilKLogWrapper.d(TAG, "run: _bluetoothSocket == null")
            return
        }
        var bluetoothSocket: BluetoothSocket?
        while (_run) {
            try {
                bluetoothSocket = _bluetoothServerSocket?.accept() ?: throw Exception("bluetoothSocket is null")
                startRead(bluetoothSocket)
                bluetoothSocket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                UtilKLogWrapper.e(TAG, "run: server socket fail ${e.message}")
            }
        }
    }
}
