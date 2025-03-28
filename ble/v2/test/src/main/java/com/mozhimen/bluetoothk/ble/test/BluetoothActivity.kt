package com.mozhimen.bluetoothk.ble.test

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.mozhimen.bluetoothk.ble.BluetoothKBle
import com.mozhimen.bluetoothk.ble.BluetoothKBleScanProxy
import com.mozhimen.bluetoothk.ble.v2.commons.IBluetoothKBle2ScanListener
import com.mozhimen.bluetoothk.ble.test.databinding.ActivityBluetoothBinding
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.bluetooth.isBondState_BOND_BONDED
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.android.widget.showToast
import com.mozhimen.kotlin.utilk.kotlin.collections.containsBy
import com.mozhimen.kotlin.utilk.kotlin.ifNotEmptyOr
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDB
import com.mozhimen.xmlk.vhk.VHKLifecycle2

/**
 * @ClassName BTActivity2
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/2/19 0:19
 * @Version 1.0
 */
class BluetoothActivity : BaseActivityVDB<ActivityBluetoothBinding>() {
    companion object {
        const val EXTRA_BLUETOOTH_ADDRESS = "EXTRA_BLUETOOTH_ADDRESS"
    }

    ///////////////////////////////////////////////////////////////////////////////

    private var _bluetoothDevices: MutableList<BluetoothDevice> = ArrayList()
    private var _baseQuickAdapter: BaseQuickAdapter<BluetoothDevice, VHKLifecycle2> = object : BaseQuickAdapter<BluetoothDevice, VHKLifecycle2>(_bluetoothDevices) {
        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: VHKLifecycle2, position: Int, item: BluetoothDevice?) {
            super.onBindViewHolder(holder, position, item)
            item ?: return
            item.name?.ifNotEmptyOr({
                holder.findViewById<TextView>(android.R.id.text1).setText(it)
            }, {
                holder.findViewById<TextView>(android.R.id.text1).setText("Null")
            }) ?: kotlin.run {
                holder.findViewById<TextView>(android.R.id.text1).setText("Null")
            }
            item.address?.ifNotEmptyOr({
                holder.findViewById<TextView>(android.R.id.text2).setText(item.address)
            }, {
                holder.findViewById<TextView>(android.R.id.text2).setText("Null")
            }) ?: kotlin.run {
                holder.findViewById<TextView>(android.R.id.text2).setText("Null")
            }
        }

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VHKLifecycle2 {
            return VHKLifecycle2(parent, android.R.layout.simple_list_item_2)
        }
    }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    private val _bluetoothKBleScanProxy by lazy { BluetoothKBleScanProxy() }

    ///////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    override fun initData(savedInstanceState: Bundle?) {
        if (BluetoothKBle.instance.getBluetoothAdapter() == null) {
            "没有找到蓝牙适配器".showToast()
            onBackPressed()
            return
        }
        super.initData(savedInstanceState)
    }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    @SuppressLint("MissingPermission")
    override fun initView(savedInstanceState: Bundle?) {
        vdb.recyHistory.setLayoutManager(LinearLayoutManager(this))
        vdb.recyHistory.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        vdb.recyHistory.setAdapter(_baseQuickAdapter)
        _baseQuickAdapter.setOnItemClickListener(BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            if (_bluetoothDevices[position].isBondState_BOND_BONDED()) {
                setResult(CActivity.RESULT_OK, Intent().apply {
                    putExtra(EXTRA_BLUETOOTH_ADDRESS, _bluetoothDevices[position].address)
                })
                finish()
            } else {
                _bluetoothKBleScanProxy.startBound(_bluetoothDevices[position])
            }
        })
        vdb.swipeRefresh.setColorSchemeResources(R.color.black)
        vdb.swipeRefresh.setOnRefreshListener {
            startScan()
        }
    }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    override fun initObserver() {
        _bluetoothKBleScanProxy.apply {
            setBluetoothKBleScanListener(object : IBluetoothKBle2ScanListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onFound(scanResult: ScanResult) {
                    UtilKLogWrapper.d(TAG, "onFound: ${scanResult.device.address}")
                    if (_bluetoothDevices.containsBy { it.address == scanResult.device.address })
                        return
                    _bluetoothDevices.add(scanResult.device)
                    _baseQuickAdapter.notifyDataSetChanged()
                    if (vdb.swipeRefresh.isRefreshing)
                        vdb.swipeRefresh.isRefreshing = false
                }

                override fun onBonded(bluetoothDevice: BluetoothDevice) {
                    setResult(CActivity.RESULT_OK, Intent().apply {
                        putExtra(EXTRA_BLUETOOTH_ADDRESS, bluetoothDevice.address)
                    })
                    finish()
                }
            })
            bindLifecycle(this@BluetoothActivity)
        }

        startScan()
    }

    ///////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun startScan() {
        _bluetoothDevices.clear()
        _baseQuickAdapter.notifyDataSetChanged()
        _bluetoothKBleScanProxy.startScan(this)
    }
}