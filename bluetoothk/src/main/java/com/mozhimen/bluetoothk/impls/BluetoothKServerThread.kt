package com.mozhimen.bluetoothk.impls

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import com.mozhimen.bluetoothk.cons.CBluetoothK
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.java.util.UtilKUUID


/**
 * @ClassName BluetoothhKClientThread
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/11 21:28
 * @Version 1.0
 */
class BluetoothKServerThread : Thread, IUtilK {
    private var _bluetoothServerSocket: BluetoothServerSocket? = null
    private var _run = true

    @JvmOverloads
    constructor(bluetoothAdapter: BluetoothAdapter, clearText: Boolean = false) {
        var bluetoothServerSocket: BluetoothServerSocket? = null
        try {
            bluetoothServerSocket = if (!clearText)
                bluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG, UtilKUUID.get(CBluetoothK.UUID))
            else
                bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(TAG, UtilKUUID.get(CBluetoothK.UUID))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _bluetoothServerSocket = bluetoothServerSocket
    }

    ///////////////////////////////////////////////////////////////////////

    override fun run() {
        if (_bluetoothServerSocket == null) {
            UtilKLogWrapper.d(TAG, "run: _bluetoothSocket == null")
            return
        }

        while (_run) {
            var bluetoothSocket: BluetoothSocket? = null
            try {
                bluetoothSocket = _bluetoothServerSocket?.accept()
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
            if (bluetoothSocket != null) {
                try {
                    managerInfo(bluetoothSocket)
                    bluetoothSocket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                break
            }
        }
    }

    fun cancel() {
        try {
            _bluetoothServerSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun managerInfo(bluetoothSocket: BluetoothSocket?) {
    }
}
