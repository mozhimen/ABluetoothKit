package com.mozhimen.bluetoothk.test

import android.content.Intent
import android.os.Bundle
import com.mozhimen.bluetoothk.impls.BluetoothKClientProxy
import com.mozhimen.bluetoothk.test.databinding.ActivityClientBinding
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.android.content.startActivityForResult
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDBVM

class ClientActivity : BaseActivityVDBVM<ActivityClientBinding, ClientViewModel>() {
    companion object {
        const val REQUEST_CODE_BLUETOOTH = 1001
    }

    ///////////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    private val _bluetoothKClientProxy: BluetoothKClientProxy by lazy { BluetoothKClientProxy() }

    ///////////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    override fun initView(savedInstanceState: Bundle?) {
        _bluetoothKClientProxy.apply {
            bindLifecycle(this@ClientActivity)
            setOnReadListener {
                vm.liveData.postValue(it)
            }
        }
        vdb.clientBtnConnect.setOnClickListener {
            startActivityForResult<BluetoothActivity>(REQUEST_CODE_BLUETOOTH)
        }
        vdb.clientBtnWrite.setOnClickListener {
            val data = vdb.clientEditData.text.trim().toString()
            if (data.isNotEmpty()) {
                _bluetoothKClientProxy.write(data)
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
        if (resultCode == CActivity.RESULT_OK && requestCode == REQUEST_CODE_BLUETOOTH) {
            val address = data?.getStringExtra(BluetoothActivity.EXTRA_BLUETOOTH_ADDRESS)
            if (!address.isNullOrEmpty()) {
                vdb.clientTxtAddress.text = address
                _bluetoothKClientProxy.apply {
                    setMac(address)
                    start()
                }
            }
        }
    }
}