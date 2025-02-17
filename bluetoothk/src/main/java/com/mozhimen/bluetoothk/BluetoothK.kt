package com.mozhimen.bluetoothk

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.mozhimen.kotlin.utilk.android.content.startContext
import com.mozhimen.kotlin.utilk.android.os.UtilKHandler
import com.mozhimen.kotlin.utilk.android.util.d
import com.mozhimen.bluetoothk.commons.BluetoothKConnectCallback
import com.mozhimen.bluetoothk.commons.BluetoothKConnectWithDataManageCallback
import com.mozhimen.bluetoothk.commons.BluetoothKMacCallback
import com.mozhimen.bluetoothk.commons.BluetoothKSocketConnectedCallback
import com.mozhimen.bluetoothk.cons.CBluetoothKCons
import com.mozhimen.bluetoothk.exceptions.BluetoothKSupportException
import com.mozhimen.bluetoothk.helpers.BluetoothKConnectThread
import com.mozhimen.bluetoothk.helpers.BluetoothKConnectThread.Companion.startUniqueConnectThread
import com.mozhimen.bluetoothk.helpers.BluetoothKReadDataThread
import com.mozhimen.bluetoothk.temps.BluetoothKChooseActivity
import com.mozhimen.bluetoothk.temps.BluetoothKConnectActivity
import com.mozhimen.bluetoothk.temps.BluetoothKOpenActivity
import com.mozhimen.kotlin.utilk.android.os.UtilKLooperWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import java.io.IOException


/**
 * @ClassName BluetoothK
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 12:13
 * @Version 1.0
 */
class BluetoothK : IUtilK {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        val instance = INSTANCE.holder
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    var _handler: Handler? = null

    private var _context: Context? = null
    private var _bluetoothAdapter: BluetoothAdapter? = null
    private val _bluetoothKConnectCallbackMap = HashMap<String, BluetoothKConnectCallback>()
    private val _bluetoothKReadDataThreadMap = HashMap<String, BluetoothKReadDataThread>()
    private val _bluetoothKConnectThreadMap = HashMap<String, BluetoothKConnectThread>() //防止同一mac地址多次连接。
    private val _bluetoothSocketMap = HashMap<String, BluetoothSocket>()
    private val _macToKeyMap = HashMap<String, String>()

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param context                      上下文
     * @param mac                          如果以前有保存蓝牙mac地址，则可以直接输入
     * @param showConnectBluetoothActivity 是否显示等待界面，若后台有自动重连请设置为false，不然每次连接都转圈圈。。。
     * @param bluetoothKMacCallback        连接建立和取消连接后调用的回调函数
     */
    fun getBTMac(context: Context, mac: String, showConnectBluetoothActivity: Boolean, bluetoothKMacCallback: BluetoothKMacCallback) {
        _context = context
        context.startContext<BluetoothKChooseActivity>() {
            putExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY, "1-*1")
        }
    }

    fun getSocketFromMap(mac: String): BluetoothSocket? =
        _bluetoothSocketMap[mac]

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param context  上下文
     * @param callback 连接建立和取消连接后调用的回调函数
     */
    fun connectBluetooth(context: Context, callback: BluetoothKMacCallback) {
        getBTMac(context, "", false, callback)
    }

    /**
     * @param context                  上下文
     * @param callback 连接建立和取消连接后调用的回调函数
     */
    fun connectBluetooth(context: Context, callback: BluetoothKConnectCallback) {
        connectBluetooth(context, "", false, callback)
    }

    /**
     * @param context                      上下文
     * @param mac                          如果以前有保存蓝牙mac地址，则可以直接输入
     * @param isShowConnectBluetoothActivity 是否显示等待界面，若后台有自动重连请设置为false，不然每次连接都转圈圈。。。
     * @param callback     连接建立和取消连接后调用的回调函数
     */
    fun connectBluetooth(context: Context, mac: String, isShowConnectBluetoothActivity: Boolean, callback: BluetoothKConnectCallback) {

        //确认在主线程中
        if (_handler == null && !UtilKLooperWrapper.isLooperMain()) {
            throw IllegalStateException("please call MedBluetooth.connect in main thread")
        } else {
            if (_handler == null)
                _handler = Handler()
        }
        _context = context
        ("connectBluetooth: before put in map callback is BluetoothKConnectWithDataManageCallback == " + (callback is BluetoothKConnectWithDataManageCallback)).d(TAG)

        //如果mac地址对应的callback key已经存在
        val key: String = if (_macToKeyMap[mac] != null) _macToKeyMap[mac]!! else (Math.random() * 10000000).toInt().toString() + ""

        //todo mac 为空
        Log.i("BluetoothStateChange", "final mac = $mac")
        Log.i("BluetoothStateChange", "final key = " + _macToKeyMap[mac])
        _bluetoothKConnectCallbackMap[key] = callback
        ("connectBluetooth: after put in map callback is BluetoothKConnectWithDataManageCallback == " + (_bluetoothKConnectCallbackMap[key] is BluetoothKConnectWithDataManageCallback) + " callbackid = " + _bluetoothKConnectCallbackMap[key].toString()).d(
            TAG
        )

        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (_bluetoothAdapter == null)
            executeBluetoothConnectCallback(null, null, BluetoothKSupportException("Can't get default bluetooth adapter"), key)

        if (!_bluetoothAdapter!!.isEnabled) {
            context.startContext<BluetoothKOpenActivity>() {
                putExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY, key)
            }
            return
        }
        if (mac.isEmpty()) {
            context.startContext<BluetoothKChooseActivity>() {
                putExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY, key)
            }
        } else if (isShowConnectBluetoothActivity) {
            context.startContext<BluetoothKConnectActivity>() {
                putExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY, key)
                putExtra(CBluetoothKCons.EXTRA_MAC_ADDRESS, mac)
            }
        } else if (!isShowConnectBluetoothActivity) {
            startUniqueConnectThread(context, mac, object : BluetoothKSocketConnectedCallback() {
                override fun done(socket: BluetoothSocket?, device: BluetoothDevice?, e: IOException?) {
                    if (e != null)
                        e.printStackTrace()
                    else
                        executeBluetoothConnectCallback(socket, device, e, key)
                }
            })
        }
    }

    fun removeMacFromMap(mac: String) {
        _bluetoothKConnectThreadMap.remove(mac)
    }

    fun removeSocketFromMap(mac: String) {
        _bluetoothSocketMap.remove(mac)
    }

    fun addSocketToMap(mac: String, socket: BluetoothSocket) {
        _bluetoothSocketMap[mac] = socket
    }

    fun disconnect(mac: String?) {
        executeBluetoothDisconnectedCallback(mac)
    }

    fun disconnectAll() {
        val macs: Set<String?> = _macToKeyMap.keys
        for (mac: String? in macs) {
            executeBluetoothDisconnectedCallback(mac)
        }
    }

    fun executeMacCallback(btName: String, mac: String, key: String?) {
        val callback = _bluetoothKConnectCallbackMap[key] as? BluetoothKConnectWithDataManageCallback?
        callback?.getMac(btName, mac)
    }

    fun executeBluetoothDisconnectedCallback(mac: String?) {
        val key = _macToKeyMap[mac]
        Log.i("BluetoothStateChange", "disconnect mac = $mac")
        Log.i("BluetoothStateChange", "disconnect key = " + _macToKeyMap[mac])
        if (_bluetoothKConnectCallbackMap[key] != null) {
            val thread = _bluetoothKReadDataThreadMap[key]
            if (thread != null) {
                Log.d(TAG, "executeBluetoothDisconnectedCallback: thread == $thread")
                thread.stopThread()
                thread.interrupt()
            }
            //send broadcast to user
            val intent = Intent(CBluetoothKCons.INTENT_BLUETOOTH_DISCONNECTED)
            intent.putExtra(CBluetoothKCons.EXTRA_BLUETOOTH_MAC, mac)
            _context?.sendBroadcast(intent)
            _bluetoothKConnectCallbackMap[key]?.internalDisconnected()
            _bluetoothKConnectCallbackMap.remove(key)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    protected fun executeBluetoothConnectCallback(socket: BluetoothSocket?, device: BluetoothDevice?, e: Exception?, key: String) {
        if (_macToKeyMap[device!!.address] == null) {
            _macToKeyMap[device.address] = key
        }
        ("executeBluetoothConnectCallback: mBluetoothConnectCallbackMap.get(key) instanceof BluetoothKConnectWithDataManageCallback == " + (_bluetoothKConnectCallbackMap[key] is BluetoothKConnectWithDataManageCallback) + " callbackid = " + _bluetoothKConnectCallbackMap[key].toString()).d(
            TAG
        )
        if (e == null && _bluetoothKConnectCallbackMap[key] is BluetoothKConnectWithDataManageCallback) {
            Log.d(TAG, "executeBluetoothConnectCallback: mBluetoothConnectCallbackMap.get(key) instanceof BluetoothKConnectWithDataManageCallback and e == $e")
            val thread = BluetoothKReadDataThread(socket, _bluetoothKConnectCallbackMap[key] as? BluetoothKConnectWithDataManageCallback?)
            val oldThread = _bluetoothKReadDataThreadMap[key]
            if (oldThread == null || oldThread.state == Thread.State.TERMINATED) {
                Log.d(TAG, "executeBluetoothConnectCallback: oldThread == null")
                _bluetoothKReadDataThreadMap[key] = thread
                thread.start()
            } else {
                Log.d(TAG, "executeBluetoothConnectCallback: oldThread != null")
                oldThread.interrupt()
                _bluetoothKReadDataThreadMap[key] = thread
                thread.start()
            }
            Log.d(TAG, "executeBluetoothConnectCallback: thread == $thread")
        } else {
            Log.d(
                TAG,
                "executeBluetoothConnectCallback: mBluetoothConnectCallbackMap.get(key) is BluetoothKConnectWithDataManageCallback == " +
                        (_bluetoothKConnectCallbackMap[key] is BluetoothKConnectWithDataManageCallback)
            )
        }
        _bluetoothKConnectCallbackMap[key]!!.internalConnected(socket, device, e)
        //        mBluetoothConnectCallback.internalConnected(socket, device, e);
        Log.i("BluetoothStateChange", "connect mac = " + device.address)
        Log.i("BluetoothStateChange", "connect key = " + _macToKeyMap[device.address])
        if (e != null) {
            _bluetoothKConnectCallbackMap.remove(key)
            Log.i("BluetoothStateChange", "e != null mac = " + device.address)
            Log.i("BluetoothStateChange", "e != null key = " + _macToKeyMap[device.address])
        } else {
            val intent = Intent(CBluetoothKCons.INTENT_BLUETOOTH_CONNECTED)
            intent.putExtra(CBluetoothKCons.EXTRA_BLUETOOTH_MAC, device.address)
            _context?.sendBroadcast(intent)
        }
    }

    protected fun getConnectThreadByMac(mac: String): BluetoothKConnectThread? =
        _bluetoothKConnectThreadMap[mac]

    protected fun addConnectThreadToMap(mac: String, thread: BluetoothKConnectThread) {
        _bluetoothKConnectThreadMap[mac] = thread
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private object INSTANCE {
        @SuppressLint("StaticFieldLeak")
        val holder = BluetoothK()
    }
}