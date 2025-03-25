package com.mozhimen.bluetoothk.classic.bases

import android.bluetooth.BluetoothSocket
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.kotlin.bytes2str
import com.mozhimen.kotlin.utilk.kotlin.str2bytes
import java.io.InputStream
import java.io.OutputStream

/**
 * @ClassName BaseBluetoothKThread
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/12
 * @Version 1.0
 */
open class BaseClassicThread : Thread(), IUtilK {
    protected var _bluetoothSocket: BluetoothSocket? = null
    protected var _inputStream: InputStream? = null
    protected var _outputStream: OutputStream? = null
    protected var _run = true
    protected var _onReadListener: IA_Listener<String>? = null

    fun cancel() {
        UtilKLogWrapper.d(TAG, "cancel: ")
        try {
            _run = false
            _bluetoothSocket?.close()
            _onReadListener = null
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "cancel: socket?.close fail ${e.message}")
        }
    }

    fun write(str: String) {
        try {
            _outputStream?.write(str.str2bytes().also { UtilKLogWrapper.d(TAG, "write: $it") })?:run{
                UtilKLogWrapper.d(TAG, "write: _outputStream is null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e("sendData: fail ${e.message}")
        }
    }

    fun startRead(bluetoothSocket: BluetoothSocket) {
        try {
            if (!bluetoothSocket.isConnected) {
                bluetoothSocket.connect()
                UtilKLogWrapper.e(TAG, "run: _bluetoothSocket!!.connect()")
            }

            _inputStream = bluetoothSocket.inputStream ?: run{
                UtilKLogWrapper.e(TAG, "startRead: inputStream is null")
                return
            }
            _outputStream = bluetoothSocket.outputStream ?: run{
                UtilKLogWrapper.e(TAG, "startRead: outputStream is null")
                return
            }

            //一直监听数据流,由于在子线程，故而通过Handler发送给主线程。
            val buffer = ByteArray(1024)
            var byteSize: Int
            while (_run) {
                try {
                    byteSize = _inputStream!!.read(buffer)
                    if (byteSize > 0) {
                        _onReadListener?.invoke(buffer.bytes2str())
                    }
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UtilKLogWrapper.e(TAG, "run: socket data fail ${e.message}")
        } finally {
            cancel()
        }
    }
}