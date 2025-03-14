package com.mozhimen.bluetoothk

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.basick.utils.requireContext
import com.mozhimen.bluetoothk.commons.IBluetoothKScanListener
import com.mozhimen.bluetoothk.commons.IBluetoothKScanProxy
import com.mozhimen.bluetoothk.utils.BluetoothKUtil
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothDevice
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.content.gainParcelableExtra
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName BluetoothKScanProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/19
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
open class BluetoothKScanProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKScanProxy {

    private var _bluetoothKScanListener: IBluetoothKScanListener? = null
    private var _bluetoothDevices: ConcurrentHashMap<String, BluetoothDevice> = ConcurrentHashMap<String, BluetoothDevice>()

    private val _broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            when (action) {
                CBluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    UtilKLogWrapper.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED")
                }

                CBluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    UtilKLogWrapper.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED")
                    _bluetoothKScanListener?.onStop()
                }

                CBluetoothDevice.ACTION_FOUND -> {//scan start
                    UtilKLogWrapper.d(TAG, "onReceive: ACTION_FOUND")
                    val bluetoothDevice = intent.gainParcelableExtra<BluetoothDevice>(CBluetoothDevice.EXTRA_DEVICE)
                    if (bluetoothDevice != null) {
                        _bluetoothKScanListener?.onFound(bluetoothDevice)
                    }
                }

                CBluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bluetoothDevice = intent.gainParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    when (bluetoothDevice?.bondState) {
                        CBluetoothDevice.BOND_BONDING -> {
                            UtilKLogWrapper.d(TAG, "onReceive: ACTION_BOND_STATE_CHANGED BOND_BONDING......")
                            if (_bluetoothDevices.contains(bluetoothDevice) && _bluetoothDevices[bluetoothDevice.address] != null) {
                                _bluetoothKScanListener?.onBonding(bluetoothDevice)
                            }
                        }

                        CBluetoothDevice.BOND_BONDED -> {
                            UtilKLogWrapper.d(TAG, "onReceive: ACTION_BOND_STATE_CHANGED BOND_BONDED")
                            if (_bluetoothDevices.contains(bluetoothDevice) && _bluetoothDevices[bluetoothDevice.address] != null) {
                                _bluetoothDevices.remove(bluetoothDevice.address)
                                _bluetoothKScanListener?.onBonded(bluetoothDevice)
                            }
                        }

                        CBluetoothDevice.BOND_NONE -> {
                            UtilKLogWrapper.d(TAG, "onReceive: ACTION_BOND_STATE_CHANGED BOND_NONE")
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    fun setBluetoothKScanListener(listener: IBluetoothKScanListener) {
        _bluetoothKScanListener = listener
    }

    @OptIn(OApiInit_InApplication::class)
    override fun startScan(activity: Activity) {
        BluetoothKUtil.requestBluetoothPermission(activity) {
            if (BluetoothK.instance.getBluetoothAdapter() == null)
                return@requestBluetoothPermission
            if (!BluetoothK.instance.isBluetoothEnabled())
                BluetoothK.instance.getBluetoothAdapter()?.enable()
            cancelScan()
            BluetoothK.instance.getBluetoothAdapter()?.startDiscovery()
        }
    }

    @OptIn(OApiInit_InApplication::class)
    override fun isScanning(): Boolean =
        BluetoothK.instance.getBluetoothAdapter()?.isDiscovering == true

    @OptIn(OApiInit_InApplication::class)
    override fun cancelScan() {
        if (isScanning()) {
            BluetoothK.instance.getBluetoothAdapter()?.cancelDiscovery()
        }
        _bluetoothDevices.clear()
    }

    fun startBound(bluetoothDevice: BluetoothDevice) {
        if (!_bluetoothDevices.contains(bluetoothDevice.address)) {
            _bluetoothDevices[bluetoothDevice.address] = bluetoothDevice
        }
        _bluetoothDevices[bluetoothDevice.address]?.createBond()
    }

    //////////////////////////////////////////////////////////////////////////

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        val intentFilter = IntentFilter()
        intentFilter.addAction(CBluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(CBluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(CBluetoothDevice.ACTION_FOUND) // 用BroadcastReceiver来取得搜索结果
        intentFilter.addAction(CBluetoothDevice.ACTION_BOND_STATE_CHANGED)
        owner.requireContext().registerReceiver(_broadcastReceiver, intentFilter)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        cancelScan()
        _bluetoothKScanListener = null
        owner.requireContext().unregisterReceiver(_broadcastReceiver)
        super.onDestroy(owner)
    }
}