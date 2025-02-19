package com.mozhimen.bluetoothk

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.basick.utils.requireContext
import com.mozhimen.bluetoothk.commons.IBluetoothKScanListener
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothDevice
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
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
class BluetoothKScanProxy : BaseWakeBefDestroyLifecycleObserver() {

    private var _bluetoothKScanListener: IBluetoothKScanListener? = null
    private var _bluetoothDevices: ConcurrentHashMap<String, BluetoothDevice> = ConcurrentHashMap<String, BluetoothDevice>()

    private val _btReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            when (action) {
                CBluetoothDevice.ACTION_FOUND -> {//scan start
                    val bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(CBluetoothDevice.EXTRA_DEVICE)
                    UtilKLogWrapper.d(TAG, "_btReceiver: $bluetoothDevice")
                    if (bluetoothDevice != null) {
                        _bluetoothKScanListener?.onFound(bluetoothDevice)
                    }
//                    if (device!!.bluetoothClass.majorDeviceClass == 1536) {
//                        if (mTodata != null) {
//                            mTodata.succeed(device)
//                        }
//                    }
                }

                CBluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bluetoothDevice: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (bluetoothDevice?.bondState) {
                        CBluetoothDevice.BOND_BONDING -> {
                            UtilKLogWrapper.d(TAG, "btReceiver:  BOND_BONDING......")
                            if (_bluetoothDevices.contains(bluetoothDevice) && _bluetoothDevices[bluetoothDevice.address] != null) {
                                _bluetoothKScanListener?.onBonding(bluetoothDevice)
                            }
                        }

                        CBluetoothDevice.BOND_BONDED -> {
                            UtilKLogWrapper.d(TAG, "btReceiver:  BOND_BONDED")
                            if (_bluetoothDevices.contains(bluetoothDevice) && _bluetoothDevices[bluetoothDevice.address] != null) {
                                _bluetoothDevices.remove(bluetoothDevice.address)
                                _bluetoothKScanListener?.onBonded(bluetoothDevice)
                            }
                        }

                        CBluetoothDevice.BOND_NONE -> {
                            UtilKLogWrapper.d(TAG, "BOND_NONE")
                        }

                        else -> {}
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    UtilKLogWrapper.d("Print", "DISCOVERY_FINISHED")
                }
            }
        }
    }

    fun startScan(context: Context) {
        if (BluetoothK.instance.getBluetoothAdapter() == null)
            return
        if (!BluetoothK.instance.isBluetoothEnabled())
            BluetoothK.instance.getBluetoothAdapter()?.enable()
        BluetoothK.instance.requestBluetoothPermission(context) {
            cancelScan()
            BluetoothK.instance.getBluetoothAdapter()?.startDiscovery()
        }
    }

    fun isScaning(): Boolean =
        BluetoothK.instance.getBluetoothAdapter()?.isDiscovering == true

    fun cancelScan() {
        if (isScaning()) {
            BluetoothK.instance.getBluetoothAdapter()?.cancelDiscovery()
        }
        _bluetoothDevices.clear()
        _bluetoothKScanListener = null
    }

    fun startBound(bluetoothDevice: BluetoothDevice) {
        if (!_bluetoothDevices.contains(bluetoothDevice.address)) {
            _bluetoothDevices[bluetoothDevice.address] = bluetoothDevice
        }
        _bluetoothDevices[bluetoothDevice.address]?.createBond()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        val intentFilter = IntentFilter()
        intentFilter.addAction(CBluetoothDevice.ACTION_FOUND) // 用BroadcastReceiver来取得搜索结果
        intentFilter.addAction(CBluetoothDevice.ACTION_BOND_STATE_CHANGED)
        intentFilter.addAction(CBluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        owner.requireContext().registerReceiver(_btReceiver, intentFilter)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        cancelScan()
        owner.requireContext().unregisterReceiver(_btReceiver)
        super.onDestroy(owner)
    }
}