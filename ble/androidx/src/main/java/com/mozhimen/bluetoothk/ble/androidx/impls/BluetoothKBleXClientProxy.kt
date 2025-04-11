package com.mozhimen.bluetoothk.ble.androidx.impls

import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.bluetoothk.basic.commons.IBluetoothKProxy
import com.mozhimen.bluetoothk.ble.androidx.BluetoothKBleX
import com.mozhimen.bluetoothk.ble.androidx.commons.IBluetoothKXClientListener
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.utilk.kotlin.strHex2bytes
import java.util.UUID

/**
 * @ClassName BluetoothKBleXClientProxy
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/26 20:23
 * @Version 1.0
 */
@OApiInit_ByLazy
@OApiCall_BindLifecycle
@OApiCall_BindViewLifecycle
class BluetoothKBleXClientProxy : BaseWakeBefDestroyLifecycleObserver(), IBluetoothKProxy<IBluetoothKXClientListener> {
    private var _mac: String = ""
    private var _listener: IBluetoothKXClientListener? = null

    ///////////////////////////////////////////////////////////////////////////////

    fun setMac(mac: String) {
        _mac = mac
    }

    fun getMac(): String =
        _mac

    override fun setListener(listener: IBluetoothKXClientListener) {
        if (_mac.isNotEmpty()) {
            if (_listener!=null)
                stop()
            BluetoothKBleX.instance.addIBluetoothKXClientListener(_mac, listener.also { _listener = it })
        }
    }

    override fun stop() {
        if (_mac.isNotEmpty()&&_listener!=null) {
            BluetoothKBleX.instance.removeIBluetoothKXClientListener(_mac, _listener!!)
            _listener = null
        }
    }

    suspend fun write(str: String, uUIDService: UUID, uUIDCharacteristic: UUID): Boolean =
        if (_mac.isNotEmpty()) {
            BluetoothKBleX.instance.writeCharacteristic(_mac, uUIDService, uUIDCharacteristic, str.strHex2bytes())
        } else false

    suspend fun read(uUIDService: UUID, uUIDCharacteristic: UUID): ByteArray? =
        if (_mac.isNotEmpty()) {
            BluetoothKBleX.instance.readGattCharacteristic(_mac, uUIDService, uUIDCharacteristic)
        } else null

    fun subscribeToCharacteristic(uUIDService: UUID, uUIDCharacteristics: UUID) {
        if (_mac.isNotEmpty()) {
            BluetoothKBleX.instance.subscribeToCharacteristic(_mac, uUIDService, uUIDCharacteristics)
        }
    }

    fun disconnect() {
        if (_mac.isNotEmpty()) {
            BluetoothKBleX.instance.disconnect(_mac)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
        super.onDestroy(owner)
    }
}