package com.mozhimen.bluetoothk.helpers;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mozhimen.bluetoothk.BluetoothK;
import com.mozhimen.bluetoothk.commons.BluetoothKConnectWithDataManageCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lee on 6/3/16.
 */
public class BluetoothKReadDataThread extends Thread {

    private BluetoothSocket mSocket;
    private BluetoothKConnectWithDataManageCallback mConnectCallback;
    private InputStream inputStream;
    private static final String TAG = "BluetoothReadDataThread";
    private Boolean mStop = false;

    public BluetoothKReadDataThread(BluetoothSocket socket, BluetoothKConnectWithDataManageCallback connectCallback) {
        mSocket = socket;
        mConnectCallback = connectCallback;
    }

    public void run() {

        try {
            if (mSocket == null) {
//                mConnectCallback.internalDataMange(0, null, new Exception("Socket 为空"));
                Log.d(TAG, "run: Socket == null");
                return;
            }

            inputStream = mSocket.getInputStream();

            if (inputStream == null) {
//                mConnectCallback.internalDataMange(0, null, new Exception("InputStream 创建失败"));
                Log.d(TAG, "run: InputStream == null");
                return;
            }

            byte[] buffer = new byte[1024];

            while (!mStop) {
                int bytes = 0;

                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {

//                        mConnectCallback.internalDataMange(bytes, buffer, null);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
//                    mConnectCallback.internalDataMange(bytes, buffer, new Exception("处理数据出错！"));
                    if (!mSocket.isConnected()) {
                        inputStream.close();
                        BluetoothK.executeBluetoothDisconnectedCallback(mSocket.getRemoteDevice().getAddress());
                    }
                }
            }

        } catch (Exception e) {
//            mConnectCallback.internalDataMange(0, null, new Exception("处理数据线程出错！"));
        }

    }

    public void stopThread() {
        try {
            inputStream.close();
            OutputStream outputStream = mSocket.getOutputStream();
            outputStream.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mStop = true;
    }


}
