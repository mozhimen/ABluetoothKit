package com.mozhimen.bluetoothk.temps

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.elemk.android.bluetooth.CBluetoothDevice
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.kotlin.elemk.kotlin.properties.VarProperty_Set
import com.mozhimen.kotlin.stackk.monitor.StackMonitor
import com.mozhimen.kotlin.utilk.android.util.it
import com.mozhimen.kotlin.utilk.android.util.wt
import com.mozhimen.kotlin.utilk.android.widget.showToast
import com.mozhimen.kotlin.utilk.bases.IUtilK
import com.mozhimen.bluetoothk.R
import com.mozhimen.bluetoothk.cons.CBluetoothKCons
import com.mozhimen.bluetoothk.cons.EBluetoothKState

/**
 * @ClassName BluetoothKChooseActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 11:51
 * @Version 1.0
 */
@SuppressLint("MissingPermission", "NotifyDataSetChanged")
class BluetoothKChooseActivity : AppCompatActivity(), IUtilK {

    private var _bluetoothAdapter: BluetoothAdapter? = null
    private var _bluetoothKDevicesAdapter: BluetoothKDevicesAdapter? = null

    private var _recyclerView: RecyclerView? = null
    private var _textViewRefresh: TextView? = null
    private val _pairedDevices = ArrayList<String>()
    private val _foundedDevices = ArrayList<String>() //0:开始搜索

    private var _bluetoothKState by VarProperty_Set(EBluetoothKState.IDLE) { field, value ->
        when (value) {
            EBluetoothKState.STOP -> {
                if (field == EBluetoothKState.SEARCH)
                    "停止搜索".showToast()
                _bluetoothAdapter?.cancelDiscovery()
                _textViewRefresh?.text = "重新搜索"
            }

            EBluetoothKState.SEARCH -> {
                if (field == EBluetoothKState.STOP) {
                    _foundedDevices.clear()
                    _bluetoothKDevicesAdapter?.notifyDataSetChanged()
                }
                "开始搜索".showToast()
                _bluetoothAdapter!!.startDiscovery()
                _textViewRefresh!!.text = "停止搜索"
            }

            else -> {}
        }
        true
    }
    private var _callbackKey: String? = ""

    // Create a BroadcastReceiver for ACTION_FOUND
    @SuppressLint("NotifyDataSetChanged")
    private val _bluetoothSearchReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                CBluetoothDevice.ACTION_FOUND -> {// When discovery finds a device
                    val bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(CBluetoothDevice.EXTRA_DEVICE)// Get the BluetoothDevice object from the Intent
                    val deviceName = bluetoothDevice?.name ?: return
                    val s = deviceName + "\n" + bluetoothDevice.address
                    if (!_foundedDevices.contains(s) && bluetoothDevice.name.isNotEmpty()) {
                        _foundedDevices.add(bluetoothDevice.name + "\n" + bluetoothDevice.address)// Add the name and address to an array adapter to show in a ListView
                        "onReceive: name ${bluetoothDevice.name} address ${bluetoothDevice.address}".wt(TAG)
                        _bluetoothKDevicesAdapter!!.notifyDataSetChanged()
                    }
                }

                CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY -> {
                    _bluetoothKState = EBluetoothKState.STOP
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetoothk_activity_choose)

        val toolbar = findViewById<View>(R.id.btk_choose_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.str_bluetooth_choose)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        StackMonitor.instance.pushActivity(this)

        _callbackKey = intent.getStringExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY)

        // Register the BroadcastReceiver
        val intentFilter = IntentFilter(CBluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(CBluetoothKCons.INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY)
        registerReceiver(_bluetoothSearchReceiver, intentFilter) // Don't forget to unregister during onDestroy

        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (_bluetoothAdapter == null) {
            "获取蓝牙适配器失败".showToast()
            StackMonitor.instance.popAllActivity()
        }
        checkBluetoothIsEnable()
        initData()
        initView()
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothIsEnable(): Boolean =
        if (!_bluetoothAdapter!!.isEnabled) {
            "蓝牙未打开".showToast()
            startActivityForResult(Intent(CBluetoothAdapter.ACTION_REQUEST_ENABLE), CBluetoothKCons.REQUEST_CODE_OPEN_BT)
            false
        } else true

    private fun initData() {
        val pairedDevices = _bluetoothAdapter!!.bondedDevices
        if (pairedDevices.isNotEmpty()) { // If there are paired devices
            for (device in pairedDevices) {// Loop through paired devices
                val s = "${device.name}\n${device.address}"// Add the name and address to an array adapter to show in a ListView
                if (!_pairedDevices.contains(s)) {
                    _pairedDevices.add("${device.name}\n${device.address}")
                }
            }
            "initData: pairedDevices $pairedDevices".it(TAG)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        _recyclerView = findViewById<View>(R.id.btk_choose_rv) as RecyclerView
        _recyclerView!!.layoutManager = LinearLayoutManager(this)
        _bluetoothKDevicesAdapter = BluetoothKDevicesAdapter(_pairedDevices, _foundedDevices, _bluetoothAdapter!!, _callbackKey!!)
        _recyclerView!!.adapter = _bluetoothKDevicesAdapter
        _textViewRefresh = findViewById<View>(R.id.btk_choose_txt_search) as TextView
        _textViewRefresh!!.setOnClickListener(View.OnClickListener {
            if (!checkBluetoothIsEnable())
                return@OnClickListener
            _bluetoothKState = when (_bluetoothKState) {
                EBluetoothKState.IDLE -> EBluetoothKState.SEARCH
                EBluetoothKState.SEARCH -> EBluetoothKState.STOP
                EBluetoothKState.STOP -> EBluetoothKState.SEARCH
            }
        })
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
        unregisterReceiver(_bluetoothSearchReceiver)
        if (_bluetoothAdapter?.isDiscovering == true)
            _bluetoothAdapter?.cancelDiscovery()
    }

    ////////////////////////////////////////////////////////////////////////////////////

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CBluetoothKCons.REQUEST_CODE_OPEN_BT -> {
                if (resultCode == CActivity.RESULT_CANCELED)
                    "蓝牙打开失败".showToast()
                else
                    "蓝牙打开成功".showToast()
            }
        }
    }
}