package com.mozhimen.bluetoothk.impls

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.mozhimen.bluetoothk.cons.CBluetoothK
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.java.util.UtilKUUID
import com.mozhimen.kotlin.utilk.kotlin.bytes2str
import java.io.InputStream
import java.io.OutputStream


/**
 * @ClassName BluetoothhKClientThread
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/11 21:28
 * @Version 1.0
 */
class BluetoothKClientThread : Thread, IUtilK {
    private var _bluetoothSocket: BluetoothSocket? = null
    private var _inputStream: InputStream? = null
    private var _outputStream: OutputStream? = null
    private var _onDataReceiveListener: IA_Listener<String>? = null

    ///////////////////////////////////////////////////////////////////////

    constructor(bluetoothDevice: BluetoothDevice, onDataReceiveListener: IA_Listener<String>) {
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UtilKUUID.get(CBluetoothK.UUID))
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "socket创建失败")
        }
        _bluetoothSocket = bluetoothSocket
        _onDataReceiveListener = onDataReceiveListener
    }

    constructor(bluetoothAdapter: BluetoothAdapter, address: String, onDataReceiveListener: IA_Listener<String>) {
        val bluetoothDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address) ?: return
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UtilKUUID.get(CBluetoothK.UUID))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _bluetoothSocket = bluetoothSocket
        _onDataReceiveListener = onDataReceiveListener
    }

    ///////////////////////////////////////////////////////////////////////

    override fun run() {
        if (_bluetoothSocket == null) {
            UtilKLogWrapper.d(TAG, "run: _bluetoothSocket == null")
            return
        }
        try {
            if (!_bluetoothSocket!!.isConnected) {
                _bluetoothSocket!!.connect()
                UtilKLogWrapper.d(TAG, "run: _bluetoothSocket!!.connect()")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "run: socket连接失败")
            cancel()
            return
        }
        try {
            _inputStream = _bluetoothSocket?.inputStream ?: return
            _outputStream = _bluetoothSocket?.outputStream ?: return

            //一直监听数据流,由于在子线程，故而通过Handler发送给主线程。
            val buffer = ByteArray(1024)
            var byteSize: Int
            while (true) {
                try {
                    byteSize = _inputStream!!.read(buffer)
                    if (byteSize > 0) {
                        _onDataReceiveListener?.invoke(buffer.bytes2str())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel() {
        try {
            _bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "cancel: socket关闭失败")
        }
    }
}
