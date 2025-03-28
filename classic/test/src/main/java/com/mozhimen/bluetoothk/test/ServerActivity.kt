package com.mozhimen.bluetoothk.test

import android.os.Bundle
import com.mozhimen.bluetoothk.classic.impls.BluetoothKClassicServerProxy
import com.mozhimen.bluetoothk.test.databinding.ActivityServerBinding
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDBVM

class ServerActivity : BaseActivityVDBVM<ActivityServerBinding, ServerViewModel>() {
    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    private val _bluetoothKServerProxy: BluetoothKClassicServerProxy by lazy { BluetoothKClassicServerProxy() }

    ///////////////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    override fun initView(savedInstanceState: Bundle?) {
        _bluetoothKServerProxy.apply {
            bindLifecycle(this@ServerActivity)
            setListener {
                vm.liveData.postValue(it)
            }
            start(this@ServerActivity)
        }
        vdb.serverBtnWrite.setOnClickListener {
            val data = vdb.serverEditData.text.trim().toString()
            if (data.isNotEmpty()) {
                UtilKLogWrapper.d(TAG, "initView: data $data")
                _bluetoothKServerProxy.write(data)
            }
        }
    }

    override fun bindViewVM(vdb: ActivityServerBinding) {
        vdb.vm = vm
    }
}