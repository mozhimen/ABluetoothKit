package com.mozhimen.bluetoothk.scale.senssun.test

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import java.util.UUID

/**
 * @ClassName BluetoothLeService
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
/**
 * Created by wucaiyan on 17-11-6.
 */
@SuppressLint("MissingPermission")
class BluetoothLeService : Service() {
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mConnectionState = STATE_DISCONNECTED

    private var mIServicesDiscoveredListener: IServicesDiscoveredListener? = null
    private var mIDataAvailableListener: IDataAvailableListener? = null
    private var mIConnectedListener: IConnectedListener? = null
    private val mIDisconnectedListener: IDisconnectedListener? = null

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            UtilKLogWrapper.d(TAG, "newState ==" + newState + "已经链接成功！！！！！！！！！！！！！！")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt!!.discoverServices().also {
                    mIConnectedListener?.onConnect(mBluetoothGatt, status, newState)
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mIDisconnectedListener?.onDisconnect(mBluetoothGatt)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UtilKLogWrapper.d(TAG, "onServicesDiscovered status =$status")
                mIServicesDiscoveredListener?.onServicesDiscovered(gatt)
            }
        }


        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic.value != null) {
                UtilKLogWrapper.d(TAG, "onCharacteristicChanged value=${characteristic.value} UUID=${characteristic.uuid}")
            }
            if (characteristic.uuid.toString() == BleDataManager.UUID_UNLOCK_DATA_NOTIFY) {
                UtilKLogWrapper.d(TAG, "onCharacteristicChanged value=${characteristic.value}")
            }
            mIDataAvailableListener?.onCharacteristicChanged(gatt, characteristic)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mIDataAvailableListener?.onCharacteristicRead(gatt, characteristic, status)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            mIDataAvailableListener?.onCharacteristicWrite(gatt, characteristic)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            BleDataManager.gattCharacteristic_write!!.setValue(Utils.CMMD_QUERY_USER)
            writeCharacteristic(BleDataManager.gattCharacteristic_write)
        }
    }

    fun setServicesDiscoveredListener(iServicesDiscoveredListener: IServicesDiscoveredListener?) {
        mIServicesDiscoveredListener = iServicesDiscoveredListener
    }

    fun setDataAvailableListener(iDataAvailableListener: IDataAvailableListener?) {
        mIDataAvailableListener = iDataAvailableListener
    }

    fun setStateConnected(iConnectedListener: IConnectedListener?) {
        mIConnectedListener = iConnectedListener
    }

    inner class LocalBinder : Binder() {
        val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    private val mBinder: IBinder = LocalBinder()


    interface IServicesDiscoveredListener {
        fun onServicesDiscovered(bluetoothGatt: BluetoothGatt?)
    }


    interface IDataAvailableListener {
        fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?, status: Int,
        )

        fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
        )

        fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
        )
    }


    interface IConnectedListener {
        fun onConnect(bluetoothGatt: BluetoothGatt?, status: Int, newStatus: Int)
    }

    interface IDisconnectedListener {
        fun onDisconnect(bluetoothGatt: BluetoothGatt?)
    }

    fun getSupportedGattServices(): List<BluetoothGattService>? {
        if (mBluetoothGatt == null) {
            return null
        }
        return mBluetoothGatt!!.services
    }

    fun getSupportedGattServiceByUUID(uuid: UUID?): BluetoothGattService? {
        if (mBluetoothGatt == null) {
            return null
        }
        return mBluetoothGatt!!.getService(uuid)
    }

    fun setCharacteristicNotification(gattCharacterisitic: BluetoothGattCharacteristic?, enable: Boolean) {
        if (mBluetoothGatt == null || gattCharacterisitic == null) {
            UtilKLogWrapper.d(TAG, "gattCharacterisitic == null")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(gattCharacterisitic, enable)
        val descriptor = gattCharacterisitic.getDescriptor(BleDataManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID)
        if (descriptor != null) {
            UtilKLogWrapper.d("wcy", descriptor.uuid.toString() + ",," + descriptor.value + "," + descriptor.permissions)
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager

            if (mBluetoothManager == null) {
                UtilKLogWrapper.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            UtilKLogWrapper.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * connect a device
     *
     * @param address bleDevice.assress
     * @return if connect successful return true ,else return false
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            UtilKLogWrapper.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        /*if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            UtilKLogWrapper.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()){
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }*/
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            UtilKLogWrapper.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
        UtilKLogWrapper.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * disconnect
     */
    fun disconnect() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    /**
     * close the BluetoothGatt
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * read
     *
     * @param characteristic characteristic
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (mBluetoothGatt == null) {
            return
        }
        val isRead = mBluetoothGatt!!.readCharacteristic(characteristic)
        UtilKLogWrapper.d(TAG, "已经读到内容了 isRead =$isRead")
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (mBluetoothGatt == null) {
            return
        }
        if (mBluetoothGatt!!.writeCharacteristic(characteristic)) {
            UtilKLogWrapper.d(TAG, "writeCharacteristic done !")
        }
    }


    companion object {
        private const val TAG = "BluetoothLeService"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2

        const val ACTION_GATT_CONNECTED: String = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED: String = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

        private var mBluetoothGatt: BluetoothGatt? = null
    }
}