package com.mozhimen.bluetoothk.impls

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.cons.CBluetoothK
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.java.io.flushClose
import com.mozhimen.kotlin.utilk.java.util.UtilKUUID

/**
 * @ClassName BluetoothKClientProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/11
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKClientProxy : BaseWakeBefDestroyLifecycleObserver() {
    private var _thread: BluetoothDataThread? = null

    fun startCilient(mac: String) {
        if (_thread == null) {
            _thread = BluetoothDataThread(mac)
        }
        if (!_thread!!.isInterrupted) {
            _thread!!.stopThread()
            _thread!!.interrupt()
        }
        _thread!!.start()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        _thread?.stopThread()
        _thread = null
        super.onDestroy(owner)
    }

    //////////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    class BluetoothDataThread : Thread, IUtilK {
        private var _bluetoothSocket: BluetoothSocket? = null
        private var _run = true

        //////////////////////////////////////////////////////////////////////

        //加密传输，Android强制执行配对，弹窗显示配对码
        constructor(bluetoothDevice: BluetoothDevice) {
            var bluetoothSocket: BluetoothSocket? = null
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UtilKUUID.get(CBluetoothK.UUID))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _bluetoothSocket = bluetoothSocket
        }

        //加密传输，Android强制执行配对，弹窗显示配对码
        @OptIn(OApiInit_InApplication::class)
        constructor(address: String) {
            val bluetoothDevice: BluetoothDevice = BluetoothK.instance.getBluetoothAdapter()?.getRemoteDevice(address) ?: return
            var bluetoothSocket: BluetoothSocket? = null
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UtilKUUID.get(CBluetoothK.UUID))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _bluetoothSocket = bluetoothSocket
        }

        //////////////////////////////////////////////////////////////////////

        override fun run() {
            try {
                if (_bluetoothSocket == null) {
                    UtilKLogWrapper.d(TAG, "run: _bluetoothSocket == null")
                    return
                }

                if (!_bluetoothSocket!!.isConnected) {
                    _bluetoothSocket!!.connect()
                    UtilKLogWrapper.d(TAG, "run: _bluetoothSocket!!.connect()")
                }

                val inputStream = _bluetoothSocket!!.inputStream ?: run {
                    UtilKLogWrapper.d(TAG, "run: InputStream == null")
                    return
                }

                val buffer = ByteArray(1024)
                while (_run) {
                    var byteSize = 0
                    try {
                        byteSize = inputStream.read(buffer)
                        if (byteSize > 0) {
                            UtilKLogWrapper.d(TAG, "run: byteSize $byteSize buffer $buffer")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        inputStream.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopThread()
                return
            }
        }

        //////////////////////////////////////////////////////////////////////

        fun stopThread() {
            try {
                _bluetoothSocket?.close()
                _bluetoothSocket?.outputStream?.flushClose()
                _run = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}