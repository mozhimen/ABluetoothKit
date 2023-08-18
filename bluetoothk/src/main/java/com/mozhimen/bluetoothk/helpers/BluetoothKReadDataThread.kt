package com.mozhimen.bluetoothk.helpers

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.mozhimen.basick.utilk.bases.IUtilK
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.commons.BluetoothKConnectWithDataManageCallback
import java.io.IOException
import java.io.InputStream


/**
 * @ClassName BluetoothKReadDataThread
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/17 11:25
 * @Version 1.0
 */
class BluetoothKReadDataThread : Thread, IUtilK {
    private var _bluetoothSocket: BluetoothSocket? = null
    private var _bluetoothKConnectWithDataManageCallback: BluetoothKConnectWithDataManageCallback? = null
    private var _inputStream: InputStream? = null
    private var _stop = false

    constructor(socket: BluetoothSocket?, connectCallback: BluetoothKConnectWithDataManageCallback?) : super() {
        _bluetoothSocket = socket
        _bluetoothKConnectWithDataManageCallback = connectCallback
    }

    override fun run() {
        try {
            if (_bluetoothSocket == null) {
//                mConnectCallback.internalDataMange(0, null, new Exception("Socket 为空"));
                Log.d(TAG, "run: Socket == null")
                return
            }
            _inputStream = _bluetoothSocket!!.inputStream
            if (_inputStream == null) {
//                mConnectCallback.internalDataMange(0, null, new Exception("InputStream 创建失败"));
                Log.d(TAG, "run: InputStream == null")
                return
            }
            val buffer = ByteArray(1024)
            while (!_stop) {
                var bytes = 0
                try {
                    bytes = _inputStream!!.read(buffer)
                    if (bytes > 0) {
//                        mConnectCallback.internalDataMange(bytes, buffer, null);
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    //                    mConnectCallback.internalDataMange(bytes, buffer, new Exception("处理数据出错！"));
                    if (!_bluetoothSocket!!.isConnected) {
                        _inputStream!!.close()
                        BluetoothK.instance.executeBluetoothDisconnectedCallback(_bluetoothSocket!!.remoteDevice.address)
                    }
                }
            }
        } catch (e: Exception) {
//            mConnectCallback.internalDataMange(0, null, new Exception("处理数据线程出错！"));
        }
    }

    fun stopThread() {
        try {
            _inputStream?.close()
            _bluetoothSocket?.outputStream?.close()
            _bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        _stop = true
    }
}