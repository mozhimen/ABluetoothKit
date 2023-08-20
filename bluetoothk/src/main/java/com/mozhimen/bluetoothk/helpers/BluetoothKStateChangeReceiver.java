package com.mozhimen.bluetoothk.helpers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mozhimen.bluetoothk.BluetoothK;

/**
 * Created by lee on 5/30/16.
 */
public class BluetoothKStateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.i("BluetoothStateChange", "action = " + action);

        if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String mac = device.getAddress();
            Log.i("BluetoothStateChange", "mac = " + mac);
            BluetoothK.executeBluetoothDisconnectedCallback(mac);
        }
    }
}
