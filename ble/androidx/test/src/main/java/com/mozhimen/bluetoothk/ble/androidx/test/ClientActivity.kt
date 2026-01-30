package com.mozhimen.bluetoothk.ble.androidx.test

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.mozhimen.basick.utils.runOnLifecycleState
import com.mozhimen.bluetoothk.ble.androidx.commons.IBluetoothKBleXClientListener
import com.mozhimen.bluetoothk.ble.androidx.impls.BluetoothKBleXClientProxy
import com.mozhimen.bluetoothk.ble.androidx.test.databinding.ActivityClientBinding
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.android.content.startActivityForResult
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.kotlin.bytes2strHex
import com.mozhimen.kotlin.utilk.kotlin.str2uUID
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDBVM
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.onEach

class ClientActivity : BaseActivityVDBVM<ActivityClientBinding, ClientViewModel>() {
    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    private val _bluetoothKBleXClientProxy: BluetoothKBleXClientProxy by lazy { BluetoothKBleXClientProxy() }

    ///////////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    override fun initView(savedInstanceState: Bundle?) {
        _bluetoothKBleXClientProxy.apply {
            bindLifecycle(this@ClientActivity)
        }
        vdb.clientBtnConnect.setOnClickListener {
            startActivityForResult<BluetoothActivity>(BluetoothActivity.REQUEST_CODE_BLUETOOTH)
        }
    }

    override fun initObserver() {
        runOnLifecycleState(Lifecycle.State.CREATED) {
            vm.flowSubscribeData
                .filterNot { it.size != 11 }
                .distinctUntilChanged { old, new ->
                    old.contentEquals(new)
                }.onEach {
                    UtilKLogWrapper.d(TAG, "onActivityResult: flow bytes size: ${it.size}")
                }.collect {
                    val strHex = it.bytes2strHex()
                    vm.liveData.postValue(strHex)
                }
        }
    }

    override fun bindViewVM(vdb: ActivityClientBinding) {
        vdb.vm = vm
    }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == CActivity.RESULT_OK && requestCode == BluetoothActivity.REQUEST_CODE_BLUETOOTH) {
            val address = data?.getStringExtra(BluetoothActivity.EXTRA_BLUETOOTH_ADDRESS)
            if (!address.isNullOrEmpty()) {
                vdb.clientTxtAddress.text = address
                _bluetoothKBleXClientProxy.apply {
                    setMac(address)
                    setListener(object :IBluetoothKBleXClientListener{
                        override fun onReadGattCharacteristic(bytes: ByteArray) {
                            vm.flowSubscribeData.tryEmit(bytes)
                        }
                    })
                    subscribeToCharacteristic("0000fff0-0000-1000-8000-00805f9b34fb".str2uUID(),"0000fff1-0000-1000-8000-00805f9b34fb".str2uUID())
                }
            }
        }
    }
}