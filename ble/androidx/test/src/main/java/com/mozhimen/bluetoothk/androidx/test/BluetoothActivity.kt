package com.mozhimen.bluetoothk.androidx.test

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.ScanResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.mozhimen.bluetoothk.androidx.test.databinding.ActivityBluetoothBinding
import com.mozhimen.bluetoothk.ble.androidx.BluetoothKBleXScanProxy
import com.mozhimen.bluetoothk.ble.androidx.commons.IBluetoothKBleXScanListener
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.bluetooth.isBondState_BOND_BONDED
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
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

    private var _scanResults: MutableList<ScanResult> = ArrayList()
    private var _baseQuickAdapter: BaseQuickAdapter<ScanResult, VHKLifecycle2> = object : BaseQuickAdapter<ScanResult, VHKLifecycle2>(_scanResults) {
        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: VHKLifecycle2, position: Int, item: ScanResult?) {
            super.onBindViewHolder(holder, position, item)
            item ?: return
            item.device.name?.ifNotEmptyOr({
                holder.findViewById<TextView>(android.R.id.text1).setText(it)
            }, {
                holder.findViewById<TextView>(android.R.id.text1).setText("Null")
            }) ?: kotlin.run {
                holder.findViewById<TextView>(android.R.id.text1).setText("Null")
            }
            item.deviceAddress.address.ifNotEmptyOr({
                holder.findViewById<TextView>(android.R.id.text2).setText(it)
            }, {
                holder.findViewById<TextView>(android.R.id.text2).setText("Null")
            })
        }

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VHKLifecycle2 {
            return VHKLifecycle2(parent, android.R.layout.simple_list_item_2)
        }
    }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    private val _bluetoothKBleXScanProxy by lazy { BluetoothKBleXScanProxy() }

    ///////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class, OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    @SuppressLint("MissingPermission")
    override fun initView(savedInstanceState: Bundle?) {
        vdb.recyHistory.setLayoutManager(LinearLayoutManager(this))
        vdb.recyHistory.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        vdb.recyHistory.setAdapter(_baseQuickAdapter)
        _baseQuickAdapter.setOnItemClickListener(BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
//            if (_scanResults[position].isBondState_BOND_BONDED()) {
//                setResult(CActivity.RESULT_OK, Intent().apply {
//                    putExtra(EXTRA_BLUETOOTH_ADDRESS, _scanResults[position].address)
//                })
//                finish()
//            } else {
//                _bluetoothKBle2ScanProxy.startBound(_scanResults[position])
//            }
        })
        vdb.swipeRefresh.setColorSchemeResources(R.color.black)
        vdb.swipeRefresh.setOnRefreshListener {
            startScan()
        }
    }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    override fun initObserver() {
        _bluetoothKBleXScanProxy.apply {
            setBluetoothKBleScanListener(object : IBluetoothKBleXScanListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onFound(scanResult: androidx.bluetooth.ScanResult) {
                    UtilKLogWrapper.d(TAG, "onFound: ${scanResult.device.address}")
                    if (_scanResults.containsBy { it.address == scanResult.device.address })
                        return
                    _scanResults.add(scanResult.device)
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
    }

    override fun initEvent() {
        startScan()
    }

    ///////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun startScan() {
        _scanResults.clear()
        _baseQuickAdapter.notifyDataSetChanged()
        _bluetoothKBle2ScanProxy.startScan(this)
    }
}