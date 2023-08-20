package com.mozhimen.bluetoothk.temps;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.mozhimen.basick.stackk.monitor.StackMonitor;
import com.mozhimen.bluetoothk.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BluetoothKOpenActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 0x01;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_bluetooth);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        StackMonitor.getInstance().pushActivity(this);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "打开蓝牙成功", Toast.LENGTH_SHORT).show();
                String key = getIntent().getStringExtra("callback_key");
                if (key != null) {
                    Intent intent = new Intent(this, BluetoothKChooseActivity.class);
                    intent.putExtra("callback_key", key);
                    startActivity(intent);
                }
                StackMonitor.getInstance().popActivity(this);
            } else {
                Toast.makeText(this, "用户取消打开蓝牙", Toast.LENGTH_SHORT).show();
                StackMonitor.getInstance().popActivity(this);
            }
        }
    }
}
