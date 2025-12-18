package com.mozhimen.bluetoothk.temps

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.stackk.monitor.StackKMonitor
import com.mozhimen.bluetoothk.R
import com.mozhimen.bluetoothk.cons.CBluetoothKCons
import com.mozhimen.kotlin.utilk.android.content.startContext

/**
 * @ClassName BluetoothKConnectActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/18 12:07
 * @Version 1.0
 */
class BluetoothKConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetoothk_activity_connect)
        supportActionBar?.hide()
        StackKMonitor.instance.pushActivity(this)

        val mProgressBar = findViewById<View>(R.id.btk_conn_progress) as? ProgressBar?
        mProgressBar?.isIndeterminate = true

        intent.getStringExtra(CBluetoothKCons.EXTRA_BLUETOOTH_MAC)?.let {
            if (it.isNotEmpty()) {
                startContext<BluetoothKChooseActivity>()
                StackKMonitor.instance.popActivity(this)
            }
        }
        mProgressBar?.visibility = View.GONE

//        Toast.makeText(mContext, "连接成功", Toast.LENGTH_SHORT).show();
//        MedBluetooth.executeBluetoothConnectCallback(mac, mKey);
//        BluetoothScreenManger.getScreenManger().popAllActivity();
//
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
}