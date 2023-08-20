package com.mozhimen.bluetoothk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mozhimen.basick.utilk.android.content.UtilKContextStart;
import com.mozhimen.bluetoothk.commons.BluetoothKConnectCallback;
import com.mozhimen.bluetoothk.commons.BluetoothKConnectWithDataManageCallback;
import com.mozhimen.bluetoothk.commons.BluetoothKMacCallback;
import com.mozhimen.bluetoothk.commons.SocketConnectedCallback;
import com.mozhimen.bluetoothk.exceptions.BluetoothKSupportException;
import com.mozhimen.bluetoothk.helpers.BluetoothKReadDataThread;
import com.mozhimen.bluetoothk.helpers.BluetoothKConnectThread;
import com.mozhimen.bluetoothk.temps.BluetoothKChooseActivity;
import com.mozhimen.bluetoothk.temps.BluetoothKConnectActivity;
import com.mozhimen.bluetoothk.temps.BluetoothKOpenActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by lee on 5/23/16.
 */
public class BluetoothK {

    public static final String INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY = "cc.liyongzhi.action.BLUETOOTH_ADAPTER_CANCEL_DISCOVERY";

    public static final String INTENT_BLUETOOTH_DISCONNECTED = "cc.liyongzhi.action.BLUETOOTH_DISCONNECTED";
    public static final String INTENT_BLUETOOTH_CONNECTED = "cc.liyongzhi.action.BLUETOOTH_CONNECTED";
    public static final String EXTRA_BLUETOOTH_MAC = "mac";

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mBluetoothSocket;
    private static HashMap<String, BluetoothKConnectCallback> mBluetoothConnectCallbackMap = new HashMap<>();
    private static HashMap<String, BluetoothKReadDataThread> mBluetoothReadDataThreadMap = new HashMap<>();
    private static HashMap<String, String> mMacToKey = new HashMap<>();
    public static Handler mHandler;
    private static Context mContext;
    private static HashMap<String, BluetoothKConnectThread> mBluetoothMap = new HashMap<>(); //防止同一mac地址多次连接。
    private static HashMap<String, BluetoothSocket> mSocketMap = new HashMap<>();
    private static final String TAG = "MedBluetooth";

    /**
     * @param context                  上下文
     * @param bluetoothKConnectCallback 连接建立和取消连接后调用的回调函数
     */
    public static void connectBluetooth(Context context, BluetoothKConnectCallback bluetoothKConnectCallback) {
        connectBluetooth(context, "", false, bluetoothKConnectCallback);
    }

    /**
     * @param context  上下文
     * @param callback 连接建立和取消连接后调用的回调函数
     */
    public static void connectBluetooth(Context context, BluetoothKMacCallback callback) {
        getBTMac(context, "", false, callback);
    }


    /**
     * @param context                      上下文
     * @param mac                          如果以前有保存蓝牙mac地址，则可以直接输入
     * @param showConnectBluetoothActivity 是否显示等待界面，若后台有自动重连请设置为false，不然每次连接都转圈圈。。。
     * @param bluetoothConnectCallback     连接建立和取消连接后调用的回调函数
     */
    public static void getBTMac(final Context context, String mac, Boolean showConnectBluetoothActivity,
            BluetoothKMacCallback bluetoothConnectCallback) {
        mContext = context;
        Intent intent = new Intent(context, BluetoothKChooseActivity.class);
        intent.putExtra("callback_key", "1-*1");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //         Activity activity = (Activity) context;
        context.startActivity(intent);
    }


    /**
     * @param context                      上下文
     * @param mac                          如果以前有保存蓝牙mac地址，则可以直接输入
     * @param showConnectBluetoothActivity 是否显示等待界面，若后台有自动重连请设置为false，不然每次连接都转圈圈。。。
     * @param bluetoothKConnectCallback     连接建立和取消连接后调用的回调函数
     */
    public static void connectBluetooth(final Context context, String mac, Boolean showConnectBluetoothActivity,
            BluetoothKConnectCallback bluetoothKConnectCallback) {

        //确认在主线程中
        if (mHandler == null && Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("please call MedBluetooth.connect in main thread");
        } else {
            if (mHandler == null) {
                mHandler = new Handler();
            }
        }

        mContext = context;

        Log.d(TAG,
                "connectBluetooth: before put in map bluetoothConnectCallback instanceof BluetoothConnectWithDataManageCallback == " +
                        (bluetoothKConnectCallback instanceof BluetoothKConnectWithDataManageCallback));


        //如果mac地址对应的callback key已经存在
        final String key = mMacToKey.get(mac) != null ? mMacToKey.get(mac) : (int) (Math.random() * 10000000) + "";

        //todo mac 为空
        Log.i("BluetoothStateChange", "final mac = " + mac);
        Log.i("BluetoothStateChange", "final key = " + mMacToKey.get(mac));

        mBluetoothConnectCallbackMap.put(key, bluetoothKConnectCallback);

        Log.d(TAG,
                "connectBluetooth: after put in map bluetoothConnectCallback instanceof BluetoothConnectWithDataManageCallback == " +
                        (mBluetoothConnectCallbackMap.get(key) instanceof BluetoothKConnectWithDataManageCallback) +
                        " callbackid = " + mBluetoothConnectCallbackMap.get(key).toString());


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            executeBluetoothConnectCallback(null, null,
                    new BluetoothKSupportException("Can't get default bluetooth adapter"), key);
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(context, BluetoothKOpenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("callback_key", key);
            context.startActivity(intent);
            return;
        }

        if (mac == null || mac.equals("")) {
            Intent intent = new Intent(context, BluetoothKChooseActivity.class);
            intent.putExtra("callback_key", key);
            UtilKContextStart.startContext(context,intent);

            //            intent.putExtra("activity",activity);
            //            activity.startActivityForResult(intent,125);
        } else if (showConnectBluetoothActivity) {
            Intent intent = new Intent(context, BluetoothKConnectActivity.class);
            intent.putExtra("callback_key", key);
            intent.putExtra("bluetooth_mac_address", mac);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (!showConnectBluetoothActivity) {
            BluetoothKConnectThread.startUniqueConnectThread(context, mac, new SocketConnectedCallback() {
                @Override
                public void done(BluetoothSocket socket, BluetoothDevice device, IOException e) {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        BluetoothK.executeBluetoothConnectCallback(socket, device, e, key);
                    }
                }
            });
        }
    }

    public static void disconnect(String mac) {
        executeBluetoothDisconnectedCallback(mac);
    }

    public static void disconnectAll() {
        Set<String> macs = mMacToKey.keySet();
        for (String mac : macs) {
            executeBluetoothDisconnectedCallback(mac);
        }
    }

    public static void executeMacCallback(String btName, String mac, String key) {
        BluetoothKConnectWithDataManageCallback bluetoothConnectWithDataManageCallback = (BluetoothKConnectWithDataManageCallback) mBluetoothConnectCallbackMap
                .get(key);
        bluetoothConnectWithDataManageCallback.getMac(btName, mac);
    }

    protected static void executeBluetoothConnectCallback(BluetoothSocket socket, BluetoothDevice device, Exception e,
            String key) {

        if (mMacToKey.get(device.getAddress()) == null) {
            mMacToKey.put(device.getAddress(), key);
        }

        Log.d(TAG,
                "executeBluetoothConnectCallback: mBluetoothConnectCallbackMap.get(key) instanceof BluetoothConnectWithDataManageCallback == " +
                        (mBluetoothConnectCallbackMap.get(key) instanceof BluetoothKConnectWithDataManageCallback) +
                        " callbackid = " + mBluetoothConnectCallbackMap.get(key).toString());


        if (e == null && mBluetoothConnectCallbackMap.get(key) instanceof BluetoothKConnectWithDataManageCallback) {
            Log.d(TAG,
                    "executeBluetoothConnectCallback: mBluetoothConnectCallbackMap.get(key) instanceof BluetoothConnectWithDataManageCallback and e == " +
                            e);
            BluetoothKReadDataThread thread = new BluetoothKReadDataThread(socket,
                    (BluetoothKConnectWithDataManageCallback) mBluetoothConnectCallbackMap.get(key));
            BluetoothKReadDataThread oldThread = mBluetoothReadDataThreadMap.get(key);
            if (oldThread == null || oldThread.getState() == Thread.State.TERMINATED) {
                Log.d(TAG, "executeBluetoothConnectCallback: oldThread == null");
                mBluetoothReadDataThreadMap.put(key, thread);
                thread.start();
            } else {
                Log.d(TAG, "executeBluetoothConnectCallback: oldThread != null");
                oldThread.interrupt();
                mBluetoothReadDataThreadMap.put(key, thread);
                thread.start();
            }
            Log.d(TAG, "executeBluetoothConnectCallback: thread == " + thread.toString());
        } else {
            Log.d(TAG,
                    "executeBluetoothConnectCallback: mBluetoothConnectCallbackMap.get(key) instanceof BluetoothConnectWithDataManageCallback == " +
                            (mBluetoothConnectCallbackMap.get(key) instanceof BluetoothKConnectWithDataManageCallback));
        }


        mBluetoothConnectCallbackMap.get(key).internalConnected(socket, device, e);
        //        mBluetoothConnectCallback.internalConnected(socket, device, e);

        Log.i("BluetoothStateChange", "connect mac = " + device.getAddress());
        Log.i("BluetoothStateChange", "connect key = " + mMacToKey.get(device.getAddress()));


        if (e != null) {
            mBluetoothConnectCallbackMap.remove(key);
            Log.i("BluetoothStateChange", "e != null mac = " + device.getAddress());
            Log.i("BluetoothStateChange", "e != null key = " + mMacToKey.get(device.getAddress()));
        } else {
            Intent intent = new Intent(INTENT_BLUETOOTH_CONNECTED);
            intent.putExtra(EXTRA_BLUETOOTH_MAC, device.getAddress());
            mContext.sendBroadcast(intent);
        }
    }

    public static void executeBluetoothDisconnectedCallback(String mac) {

        String key = mMacToKey.get(mac);
        Log.i("BluetoothStateChange", "disconnect mac = " + mac);
        Log.i("BluetoothStateChange", "disconnect key = " + mMacToKey.get(mac));
        if (mBluetoothConnectCallbackMap.get(key) != null) {
            BluetoothKReadDataThread thread = mBluetoothReadDataThreadMap.get(key);
            if (thread != null) {
                Log.d(TAG, "executeBluetoothDisconnectedCallback: thread == " + thread.toString());
                thread.stopThread();
                thread.interrupt();
            }
            //send broadcast to user
            Intent intent = new Intent(INTENT_BLUETOOTH_DISCONNECTED);
            intent.putExtra(EXTRA_BLUETOOTH_MAC, mac);
            mContext.sendBroadcast(intent);

            mBluetoothConnectCallbackMap.get(key).internalDisconnected();
            mBluetoothConnectCallbackMap.remove(key);
        }
    }

    protected static BluetoothKConnectThread getConnectThreadByMac(String mac) {
        return mBluetoothMap.get(mac);
    }

    protected static void addConnectThreadToMap(String mac, BluetoothKConnectThread thread) {
        mBluetoothMap.put(mac, thread);
    }

    public static void removeMacFromMap(String mac) {
        mBluetoothMap.remove(mac);
    }

    static void addSocketToMap(String mac, BluetoothSocket socket) {
        mSocketMap.put(mac, socket);
    }

    static void removeSocketFromMap(String mac) {
        mSocketMap.remove(mac);
    }

    static BluetoothSocket getSocketFromMap(String mac) {
        return mSocketMap.get(mac);
    }
}
