package com.mozhimen.bluetoothk.ble.impls

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.ble.BluetoothKBle2
import com.mozhimen.bluetoothk.basic.annors.AConnectState
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKProxy
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
import com.mozhimen.bluetoothk.ble.commons.IBluetoothKBle2ClientListener
import com.mozhimen.kotlin.elemk.android.bluetooth.cons.CBluetoothGatt
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import java.util.UUID

/**
 * @ClassName BluetoothKBleClientProxy
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKBle2ClientProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKProxy<IBluetoothKBle2ClientListener> {
    private var _mac: String = ""
    private var _bluetoothGatt: BluetoothGatt? = null
    private var _iBluetoothKBle2ClientListener: IBluetoothKBle2ClientListener? = null
    private var _bluetoothState: Int = AConnectState.STATE_DISCONNECTED
        set(value) {
            when (value) {
                AConnectState.STATE_DISCONNECTED -> _iBluetoothKBle2ClientListener?.onDisConnected()
                AConnectState.STATE_CONNECTING -> _iBluetoothKBle2ClientListener?.onConnecting()
                AConnectState.STATE_CONNECTED -> _iBluetoothKBle2ClientListener?.onConnected()
                AConnectState.STATE_CONNECT_FAIL -> _iBluetoothKBle2ClientListener?.onConnectFail()
            }
            field = value
        }

    private val _bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            UtilKLogWrapper.d(TAG, "onConnectionStateChange: name ${gatt.device.name} address ${gatt.device.address} status $status newState $newState")
            UtilKLogWrapper.d(TAG, "onConnectionStateChange: ${if (status == 0) if (newState == 2) "连接成功" else "连接断开" else "连接出错, 错误码:$status"}")
            if (/*status == CBluetoothGatt.GATT_SUCCESS && */newState == CBluetoothGatt.STATE_CONNECTED) {
                UtilKLogWrapper.d(TAG, "onConnectionStateChange: connected")
                gatt.discoverServices().also {
                    _bluetoothState = AConnectState.STATE_CONNECTED
                }//启动服务发现
            } else if (newState == CBluetoothGatt.STATE_DISCONNECTED) {
                UtilKLogWrapper.d(TAG, "onConnectionStateChange: connect fail")
                stop().also {
                    _bluetoothState = AConnectState.STATE_DISCONNECTED
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            UtilKLogWrapper.d(TAG, "onServicesDiscovered: name ${gatt.device.name} address ${gatt.device.address} status $status")
            if (status == CBluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                _iBluetoothKBle2ClientListener?.onServicesDiscovered(gatt)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            _iBluetoothKBle2ClientListener?.onCharacteristicChanged(gatt, characteristic)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            _iBluetoothKBle2ClientListener?.onCharacteristicRead(gatt, characteristic)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            _iBluetoothKBle2ClientListener?.onCharacteristicWrite(gatt, characteristic)
        }

        @Deprecated("Deprecated in Java")
        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            _iBluetoothKBle2ClientListener?.onDescriptorRead(gatt, descriptor)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            _iBluetoothKBle2ClientListener?.onDescriptorWrite(gatt, descriptor)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    fun setMac(mac: String) {
        _mac = mac
    }

    // 获取Gatt服务
    fun getBluetoothGattService(uuid: UUID): BluetoothGattService? =
        _bluetoothGatt?.getService(uuid)

    ///////////////////////////////////////////////////////////////////////////////

    override fun setListener(listener: IBluetoothKBle2ClientListener) {
        _iBluetoothKBle2ClientListener = listener
    }

    @OptIn(OApiInit_InApplication::class)
    override fun start(activity: Activity) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            if (BluetoothKBle2.instance.getBluetoothAdapter() != null && _mac.isNotEmpty()) {
                val bluetoothDevice: BluetoothDevice? = BluetoothKBle2.instance.getBluetoothAdapter()?.getRemoteDevice(_mac)
                if (bluetoothDevice != null) {
                    _bluetoothGatt = bluetoothDevice.connectGatt(_context, false, _bluetoothGattCallback).also {
                        _bluetoothState = AConnectState.STATE_CONNECTING
                    }
                }
            }
        })
    }

    override fun write(str: String) {

    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    override fun stop() {
        _bluetoothGatt?.disconnect()
        _bluetoothGatt?.close()
        _bluetoothGatt = null
    }

    ///////////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
        super.onDestroy(owner)
    }
}