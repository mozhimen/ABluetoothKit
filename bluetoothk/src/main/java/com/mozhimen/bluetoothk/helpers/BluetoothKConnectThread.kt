package com.mozhimen.bluetoothk.helpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.mozhimen.basick.utilk.android.content.startContext
import com.mozhimen.bluetoothk.BluetoothK
import com.mozhimen.bluetoothk.commons.BluetoothKSocketConnectedCallback
import com.mozhimen.bluetoothk.temps.BluetoothKOpenActivity
import java.io.IOException
import java.util.UUID


/**
 * @ClassName BluetoothKConnectThread
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 11:05
 * @Version 1.0
 */
class BluetoothKConnectThread : Thread {
    private var _context: Context
    private var _bluetoothDevice: BluetoothDevice
    private var _bluetoothKSocketConnectedCallback: BluetoothKSocketConnectedCallback
    private var _bluetoothAdapter: BluetoothAdapter
    private var _bluetoothSocket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    private constructor(context: Context, device: BluetoothDevice, socketConnectedCallback: BluetoothKSocketConnectedCallback) {
        _context = context
        _bluetoothDevice = device
        _bluetoothKSocketConnectedCallback = socketConnectedCallback
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        _bluetoothSocket = bluetoothSocket
    }

    @SuppressLint("MissingPermission")
    private constructor(context: Context, address: String, socketConnectedCallback: BluetoothKSocketConnectedCallback) {
        _context = context
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        _bluetoothDevice = _bluetoothAdapter.getRemoteDevice(address)
        _bluetoothKSocketConnectedCallback = socketConnectedCallback
        var bluetoothSocket: BluetoothSocket? = null
        try {
            bluetoothSocket = _bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        _bluetoothSocket = bluetoothSocket
    }

    @SuppressLint("MissingPermission")
    override fun run() {
        if (!_bluetoothAdapter.isEnabled) {
            _context.startContext<BluetoothKOpenActivity>()
            //Todo 连接成功后回调
        }
        _bluetoothAdapter.cancelDiscovery()
        try {
            if (!_bluetoothSocket!!.isConnected)
                _bluetoothSocket!!.connect()
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                _bluetoothSocket!!.close()
            } catch (e1: IOException) {
                _bluetoothKSocketConnectedCallback.internalDone(null, null, e1)
                BluetoothK.instance.removeMacFromMap(_bluetoothDevice.address)
                return
            }
            _bluetoothKSocketConnectedCallback.internalDone(null, null, e)
            BluetoothK.instance.removeMacFromMap(_bluetoothDevice.address)
            return
        }
        _bluetoothKSocketConnectedCallback.internalDone(_bluetoothSocket, _bluetoothDevice, null)
        BluetoothK.instance.removeMacFromMap(_bluetoothDevice.address)
    }

    fun cancel() {
        try {
            _bluetoothSocket!!.close()
        } catch (e: IOException) {
        }
    }

    companion object{
        @JvmStatic
        private val _bluetoothKConnectThread: BluetoothKConnectThread? = null

        @JvmStatic
        fun startUniqueConnectThread(context: Context, address: String, socketConnectedCallback: BluetoothKSocketConnectedCallback) {
//        ConnectBluetoothThread thread = MedBluetooth.getConnectThreadByMac(address);
//        if (thread == null || thread.getState() == State.TERMINATED) {
//            MedBluetooth.addConnectThreadToMap(address, new ConnectBluetoothThread(context, address, socketConnectedCallback));
//            thread = MedBluetooth.getConnectThreadByMac(address);
//        }
//        //线程一旦被终止，就无法使用start在重新启动。
//        if (!thread.isAlive() && thread.getState() == State.NEW) {
//            thread.start();
//        } else {
//            socketConnectedCallback.internalDone(null, null, new IOException("已经有在运行的实例"));
//        }
        }
    }
}