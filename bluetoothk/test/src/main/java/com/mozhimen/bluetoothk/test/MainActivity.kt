package com.mozhimen.bluetoothk.test

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import com.mozhimen.bluetoothk.impls.BluetoothKClientProxy
import com.mozhimen.bluetoothk.test.databinding.ActivityMainBinding
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.android.content.gainParcelableExtra
import com.mozhimen.kotlin.utilk.android.content.startActivityForResult
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDB

class MainActivity : BaseActivityVDB<ActivityMainBinding>() {
    companion object {
        const val REQUEST_CODE_BLUETOOTH = 1001
    }

    private var _mac: String? = null
    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    private val _bluetoothServerActivity:BluetoothKClientProxy by lazy { BluetoothKClientProxy() }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    override fun initView(savedInstanceState: Bundle?) {
        _bluetoothServerActivity.bindLifecycle(this)

        vdb.mainBtnConnect.setOnClickListener {
            if (!_mac.isNullOrEmpty()) {
                _bluetoothServerActivity.startCilient(_mac!!)
            } else {
                startActivityForResult<BluetoothActivity>(REQUEST_CODE_BLUETOOTH)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == CActivity.RESULT_OK) {
            val bluetoothDevice = data?.gainParcelableExtra<BluetoothDevice>(BluetoothActivity.EXTRA_BLUETOOTH_ADDRESS)
            if (bluetoothDevice != null) {
                vdb.mainTxtAddress.text = bluetoothDevice.address.also { _mac = it }
            }
        }
    }
}