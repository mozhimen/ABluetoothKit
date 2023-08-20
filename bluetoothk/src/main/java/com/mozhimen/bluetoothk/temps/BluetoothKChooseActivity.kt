package com.mozhimen.bluetoothk.temps

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.basick.elemk.android.bluetooth.CBluetoothDevice
import com.mozhimen.basick.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.basick.stackk.monitor.StackMonitor
import com.mozhimen.basick.utilk.android.content.startActivityForResult
import com.mozhimen.basick.utilk.android.widget.showToast
import com.mozhimen.bluetoothk.R
import com.mozhimen.bluetoothk.cons.CBluetoothKCons
import com.mozhimen.bluetoothk.cons.EBluetoothKState
import com.mozhimen.bluetoothk.helpers.BluetoothKDevicesAdapter

/**
 * @ClassName BluetoothKChooseActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 11:51
 * @Version 1.0
 */
class BluetoothKChooseActivity : AppCompatActivity() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var _bluetoothKDevicesAdapter: BluetoothKDevicesAdapter? = null
    private var mTvRefresh: AppCompatTextView? = null
    private val mPairedDeviceList = ArrayList<String>()
    private val mFoundDeviceList = ArrayList<String>() //0:开始搜索

    private var _bluetoothKState = EBluetoothKState.IDLE
    private var mKey: String? = ""

    // Create a BroadcastReceiver for ACTION_FOUND
    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                CBluetoothDevice.ACTION_FOUND -> {// When discovery finds a device
                    val device = intent.getParcelableExtra<BluetoothDevice>(CBluetoothDevice.EXTRA_DEVICE)// Get the BluetoothDevice object from the Intent
                    val s = device!!.name + "\n" + device.address
                    if (!mFoundDeviceList.contains(s) && device.name.isNotEmpty()) {
                        // Add the name and address to an array adapter to show in a ListView
                        mFoundDeviceList.add(device.name + "\n" + device.address)
                        Log.e("TAG", "onReceive: name ${device.name} address ${device.address}")
                        _bluetoothKDevicesAdapter!!.notifyDataSetChanged()
                    }
                }

                CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY -> {
                    mBluetoothAdapter?.cancelDiscovery()
                    mTvRefresh?.text = "重新搜索"
                    _bluetoothKState = EBluetoothKState.STOP
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_bluetooth)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.choose_bluetooth_activity_title)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        StackMonitor.instance.pushActivity(this)

        mKey = intent.getStringExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY)

        // Register the BroadcastReceiver
        val intentFilter = IntentFilter(CBluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY)
        registerReceiver(mReceiver, intentFilter) // Don't forget to unregister during onDestroy

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            "获取蓝牙适配器失败".showToast()
            StackMonitor.instance.popAllActivity()
        }
        checkBluetoothIsEnable()
        initData()
        initView()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CBluetoothKCons.REQUEST_CODE_OPEN_BT -> {
                if (resultCode == RESULT_CANCELED) {
                    "蓝牙打开失败".showToast()
                } else {
                    "蓝牙打开成功".showToast()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            StackMonitor.instance.popAllActivity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        if (mBluetoothAdapter?.isDiscovering == true) {
            mBluetoothAdapter!!.cancelDiscovery()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    private fun checkBluetoothIsEnable(): Boolean =
        if (!mBluetoothAdapter!!.isEnabled) {
            "蓝牙未打开".showToast()
            startActivityForResult(Intent(CBluetoothAdapter.ACTION_REQUEST_ENABLE), CBluetoothKCons.REQUEST_CODE_OPEN_BT)
            false
        } else true

    @SuppressLint("MissingPermission")
    private fun initData() {
        val pairedDevices = mBluetoothAdapter!!.bondedDevices
        if (pairedDevices.size > 0) { // If there are paired devices
            for (device in pairedDevices) {// Loop through paired devices
                // Add the name and address to an array adapter to show in a ListView
                val s = "${device.name}\n${device.address}"
                if (!mPairedDeviceList.contains(s)) {
                    mPairedDeviceList.add("${device.name}\n${device.address}")
                    Log.e("TAG", "onReceive1: ${device.name}\n${device.address}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun initView() {
        mRecyclerView = findViewById<View>(R.id.rv_bluetooth_device_list) as RecyclerView
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)
        _bluetoothKDevicesAdapter = BluetoothKDevicesAdapter(this, mPairedDeviceList, mFoundDeviceList, mBluetoothAdapter!!, mKey!!)
        mRecyclerView!!.adapter = _bluetoothKDevicesAdapter
        mTvRefresh = findViewById<View>(R.id.tv_refresh) as AppCompatTextView
        mTvRefresh!!.setOnClickListener(View.OnClickListener {
            if (!checkBluetoothIsEnable()) {
                return@OnClickListener
            }
            when (_bluetoothKState) {
                EBluetoothKState.IDLE -> {
                    mBluetoothAdapter!!.startDiscovery()
                    "开始搜索".showToast()
                    mTvRefresh!!.text = "停止"
                    _bluetoothKState = EBluetoothKState.SEARCH
                }

                EBluetoothKState.SEARCH -> {
                    mBluetoothAdapter!!.cancelDiscovery()
                    "停止搜索".showToast()
                    mTvRefresh!!.text = "重新搜索"
                    _bluetoothKState = EBluetoothKState.STOP
                }

                EBluetoothKState.STOP -> {
                    mFoundDeviceList.clear()
                    _bluetoothKDevicesAdapter!!.notifyDataSetChanged()
                    mBluetoothAdapter!!.startDiscovery()
                    "开始搜索".showToast()
                    mTvRefresh!!.text = "停止"
                    _bluetoothKState = EBluetoothKState.SEARCH
                }
            }
            mBluetoothAdapter!!.startDiscovery()
        })
    }
}