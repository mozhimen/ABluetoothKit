package com.mozhimen.bluetoothk.temps

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.basick.stackk.monitor.StackMonitor
import com.mozhimen.bluetoothk.R

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
    companion object {
        private const val REQUEST_ENABLE_BT = 0x01
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_bluetooth)
        val actionBar = supportActionBar
        actionBar?.hide()
        StackMonitor.instance.pushActivity(this)
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "打开蓝牙成功", Toast.LENGTH_SHORT).show()
                val key = intent.getStringExtra("callback_key")
                if (key != null) {
                    val intent = Intent(this, BluetoothKChooseActivity::class.java)
                    intent.putExtra("callback_key", key)
                    startActivity(intent)
                }
                StackMonitor.instance.popActivity(this)
            } else {
                Toast.makeText(this, "用户取消打开蓝牙", Toast.LENGTH_SHORT).show()
                StackMonitor.instance.popActivity(this)
            }
        }
    }
}