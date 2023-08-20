package com.mozhimen.bluetoothk.temps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.basick.stackk.monitor.StackMonitor
import com.mozhimen.bluetoothk.R

/**
 * @ClassName BluetoothKConnectActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 12:07
 * @Version 1.0
 */
class BluetoothKConnectActivity : AppCompatActivity() {
    private var mProgressBar: ProgressBar? = null
    private var mContext: Context? = null
    private var mKey: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_bluetooth)
        val actionBar = supportActionBar
        actionBar?.hide()
        StackMonitor.instance.pushActivity(this)
        mContext = this
        initView()
        val mac = intent.getStringExtra("bluetooth_mac_address")
        mKey = intent.getStringExtra("callback_key")
        if (mac == null || mac == "") {
            val intent = Intent(this, BluetoothKChooseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        mProgressBar?.visibility = View.GONE
//        Toast.makeText(mContext, "连接成功", Toast.LENGTH_SHORT).show();
//        MedBluetooth.executeBluetoothConnectCallback(mac, mKey);
//        BluetoothScreenManger.getScreenManger().popAllActivity();

//        ConnectBluetoothThread.startUniqueConnectThread(this, mac, new SocketConnectedCallback() {
//            @Override
//            public void done(BluetoothSocket socket, BluetoothDevice device, IOException e) {
//                if (e != null) {
//                    Toast.makeText(mContext, "连接失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                    BluetoothScreenManger.getScreenManger().popActivity((Activity) mContext);
//                } else {
//                    mProgressBar.setVisibility(View.GONE);
//                    Toast.makeText(mContext, "连接成功", Toast.LENGTH_SHORT).show();
//                    MedBluetooth.executeBluetoothConnectCallback(socket, device, e, mKey);
//                    BluetoothScreenManger.getScreenManger().popAllActivity();
//                }
//            }
//        });
    }

    private fun initView() {
        mProgressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
        if (mProgressBar != null) {
            mProgressBar!!.isIndeterminate = true
        }
    }
}