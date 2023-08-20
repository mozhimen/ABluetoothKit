package com.mozhimen.bluetoothk.temps

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.basick.elemk.android.bluetooth.CBluetoothDevice
import com.mozhimen.basick.stackk.monitor.StackMonitor
import com.mozhimen.bluetoothk.R
import com.mozhimen.bluetoothk.cons.CBluetoothKCons
import com.mozhimen.bluetoothk.helpers.BluetoothKDevicesAdapter


/**
 * @ClassName BluetoothKChooseActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 11:51
 * @Version 1.0
 */
class BluetoothKChooseActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_ENABLE_BT = 0x01
    }

    //////////////////////////////////////////////////////////////////

    private var mContext: Context? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var _bluetoothKDevicesAdapter: BluetoothKDevicesAdapter? = null
    private var mTvRefresh: AppCompatTextView? = null
    private val mPairedDeviceList = ArrayList<String>()
    private val mFoundDeviceList = ArrayList<String>() //0:开始搜索

    private var state = 0
    private var mKey: String? = ""

    // Create a BroadcastReceiver for ACTION_FOUND
    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (CBluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val s = device!!.name + "\n" + device.address
                if (!mFoundDeviceList.contains(s) && !TextUtils.isEmpty(device.name)) {
                    // Add the name and address to an array adapter to show in a ListView
                    mFoundDeviceList.add(device.name + "\n" + device.address)
                    Log.e("TAG", "onReceive: name ${device.name} address ${device.address}")
                    _bluetoothKDevicesAdapter!!.notifyDataSetChanged()
                }
            } else if (CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY == action) {
                mBluetoothAdapter?.cancelDiscovery()
                mTvRefresh?.text = "重新搜索"
                state = 2
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
        mContext = this
        StackMonitor.instance.pushActivity(this)
        mKey = intent.getStringExtra("callback_key")
        // Register the BroadcastReceiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY)
        registerReceiver(mReceiver, filter) // Don't forget to unregister during onDestroy
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "获取蓝牙适配器失败", Toast.LENGTH_SHORT).show()
            StackMonitor.instance.popAllActivity()
        }
        checkBluetoothIsEnable()
        initData()
        initView()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        if (mBluetoothAdapter?.isDiscovering == true) {
            mBluetoothAdapter!!.cancelDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothIsEnable(): Boolean =
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            Toast.makeText(this, "蓝牙未打开", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }

    @SuppressLint("MissingPermission")
    private fun initData() {
        val pairedDevices = mBluetoothAdapter!!.bondedDevices
        // If there are paired devices
        if (pairedDevices.size > 0) {
            // Loop through paired devices
            for (device in pairedDevices) {
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
        _bluetoothKDevicesAdapter = BluetoothKDevicesAdapter(mContext!!, mPairedDeviceList, mFoundDeviceList, mBluetoothAdapter!!, mKey!!)
        mRecyclerView!!.adapter = _bluetoothKDevicesAdapter
        mTvRefresh = findViewById<View>(R.id.tv_refresh) as AppCompatTextView
        mTvRefresh!!.setOnClickListener(View.OnClickListener {
            if (!checkBluetoothIsEnable()) {
                return@OnClickListener
            }
            when (state) {
                0 -> {
                    mBluetoothAdapter!!.startDiscovery()
                    Toast.makeText(mContext, "开始搜索", Toast.LENGTH_SHORT).show()
                    mTvRefresh!!.text = "停止"
                    state = 1
                }

                1 -> {
                    mBluetoothAdapter!!.cancelDiscovery()
                    Toast.makeText(mContext, "停止搜索", Toast.LENGTH_SHORT).show()
                    mTvRefresh!!.text = "重新搜索"
                    state = 2
                }

                2 -> {
                    mFoundDeviceList.clear()
                    _bluetoothKDevicesAdapter!!.notifyDataSetChanged()
                    mBluetoothAdapter!!.startDiscovery()
                    Toast.makeText(mContext, "开始搜索", Toast.LENGTH_SHORT).show()
                    mTvRefresh!!.text = "停止"
                    state = 1
                }
            }
            mBluetoothAdapter!!.startDiscovery()
        })
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "蓝牙打开失败", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "蓝牙打开成功", Toast.LENGTH_SHORT).show()
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

}