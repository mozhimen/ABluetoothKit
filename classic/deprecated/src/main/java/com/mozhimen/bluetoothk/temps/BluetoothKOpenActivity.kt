package com.mozhimen.bluetoothk.temps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.bluetoothk.cons.CBluetoothKCons
import com.mozhimen.kotlin.elemk.android.app.cons.CActivity
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothAdapter
import com.mozhimen.kotlin.utilk.android.content.startContext
import com.mozhimen.kotlin.utilk.android.widget.showToast
import com.mozhimen.stackk.monitor.StackKMonitor

/**
 * @ClassName BluetoothKOpenActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 12:10
 * @Version 1.0
 */
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class BluetoothKOpenActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        StackKMonitor.instance.pushActivity(this)
        startActivityForResult(
            Intent(CBluetoothAdapter.ACTION_REQUEST_ENABLE),
            CBluetoothKCons.REQUEST_CODE_OPEN_BT
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CBluetoothKCons.REQUEST_CODE_OPEN_BT -> {
                if (resultCode == CActivity.RESULT_OK) {
                    "打开蓝牙成功".showToast()
                    intent.getStringExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY)?.let {
                        startContext<BluetoothKConnectActivity> {
                            putExtra(CBluetoothKCons.EXTRA_CALLBACK_KEY, it)
                        }
                    }
                } else {
                    "用户取消打开蓝牙".showToast()
                }
                StackKMonitor.instance.popActivity(this)
            }
        }
    }
}