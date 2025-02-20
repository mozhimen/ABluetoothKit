package com.mozhimen.bluetoothk.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mozhimen.bluetoothk.test.databinding.ActivityMainBinding
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.utilk.android.content.startActivityForResult
import com.mozhimen.kotlin.utilk.kotlin.ifNotEmpty
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDB

class MainActivity : BaseActivityVDB<ActivityMainBinding>() {
    companion object {
        const val REQUEST_CODE_BLUETOOTH = 1001
    }

    override fun initView(savedInstanceState: Bundle?) {
        vdb.mainBtn.setOnClickListener {
            startActivityForResult<BluetoothActivity>(REQUEST_CODE_BLUETOOTH)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == CActivity.RESULT_OK) {
            data?.getStringExtra(BluetoothActivity.EXTRA_BLUETOOTH_ADDRESS)?.ifNotEmpty {
                vdb.mainBtn.text = it
            }
        }
    }
}