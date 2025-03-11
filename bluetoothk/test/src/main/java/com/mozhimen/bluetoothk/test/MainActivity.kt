package com.mozhimen.bluetoothk.test

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.test.databinding.ActivityMainBinding
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.content.gainParcelableExtra
import com.mozhimen.kotlin.utilk.android.content.startActivityForResult
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDB
import java.io.InputStream
import java.util.UUID

class MainActivity : BaseActivityVDB<ActivityMainBinding>() {
    companion object {
        const val REQUEST_CODE_BLUETOOTH = 1001
    }

    private var _mac: String? = null
    override fun initView(savedInstanceState: Bundle?) {
        vdb.mainBtnConnect.setOnClickListener {
            if (!_mac.isNullOrEmpty()) {
                startGetData(_mac!!)
            } else {
                startActivityForResult<BluetoothActivity>(REQUEST_CODE_BLUETOOTH)
            }
        }
    }

    private var _thread: BluetoothReadDataThread? = null
    private fun startGetData(mac: String) {
        if (_thread == null) {
            _thread = BluetoothReadDataThread(mac)
        }
        _thread!!.start()
    }

    override fun onDestroy() {
        _thread?.stopThread()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == CActivity.RESULT_OK) {
            val bluetoothDevice = data?.gainParcelableExtra<BluetoothDevice>(BluetoothActivity.EXTRA_BLUETOOTH_ADDRESS)
            if (bluetoothDevice != null) {
                vdb.mainTxtAddress.text = bluetoothDevice.address.also { _mac=it }
            }
        }
    }

    @SuppressLint("MissingPermission")

    class BluetoothReadDataThread : Thread, IUtilK {
        private var _bluetoothSocket: BluetoothSocket? = null
        private var _inputStream: InputStream? = null
        private var _stop = false

        //////////////////////////////////////////////////////////////////////

        constructor(bluetoothDevice: BluetoothDevice) {
            var bluetoothSocket: BluetoothSocket? = null
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _bluetoothSocket = bluetoothSocket
        }

        @OptIn(OApiInit_InApplication::class)
        constructor(address: String) {
            val bluetoothDevice: BluetoothDevice = BluetoothK.instance.getBluetoothAdapter()?.getRemoteDevice(address) ?: return
            var bluetoothSocket: BluetoothSocket? = null
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _bluetoothSocket = bluetoothSocket
        }

        //////////////////////////////////////////////////////////////////////

        override fun run() {
            try {
                if (_bluetoothSocket == null) {
                    UtilKLogWrapper.d(TAG, "run: Socket == null")
                    return
                }

                if (!_bluetoothSocket!!.isConnected) {
                    _bluetoothSocket!!.connect()
                    UtilKLogWrapper.d(TAG, "run: _bluetoothSocket!!.connect()")
                }

                _inputStream = _bluetoothSocket!!.inputStream
                if (_inputStream == null) {
                    UtilKLogWrapper.d(TAG, "run: InputStream == null")
                    return
                }

                val buffer = ByteArray(1024)
                while (!_stop) {
                    var byteSize = 0
                    try {
                        byteSize = _inputStream!!.read(buffer)
                        if (byteSize > 0) {
                            UtilKLogWrapper.d(TAG, "run: byteSize $byteSize buffer $buffer")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
//                        if (!_bluetoothSocket!!.isConnected) {
//                            stopThread()
//                        }
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
                _bluetoothSocket?.outputStream?.close()
                _inputStream?.close()
                _stop = true
                this.interrupt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}