package com.mozhimen.bluetoothk.ble.androidx

import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.GattCharacteristic
import androidx.bluetooth.GattClientScope
import androidx.bluetooth.GattService
import androidx.bluetooth.ScanResult
import com.mozhimen.kotlin.elemk.commons.IA_Listener
import com.mozhimen.kotlin.utilk.java.util.uUID2str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName BluetooKBleX
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/26 21:48
 * @Version 1.0
 */
class BluetoothKBleX {
    private var _bluetoothDevices: ConcurrentHashMap<String, ScanResult> = ConcurrentHashMap<String, ScanResult>()
    private var _connectJobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap<String, Job>()
    private var _gattClientScopes: ConcurrentHashMap<String, GattClientScope> = ConcurrentHashMap<String, GattClientScope>()
    private val _subscribeFlows: ConcurrentHashMap<String, Flow<ByteArray>?> = ConcurrentHashMap<String, Flow<ByteArray>?>()
    private val _subscribeListeners: ConcurrentHashMap<String, IA_Listener<ByteArray>?> = ConcurrentHashMap<String, IA_Listener<ByteArray>?>()

    //////////////////////////////////////////////////////////////////////////

    @OptIn(DelicateCoroutinesApi::class)
    fun getScope(): CoroutineScope =
        GlobalScope

    fun getScanResult(mac: String): ScanResult? =
        _bluetoothDevices[mac]

    fun getBluetoothDevice(mac: String): BluetoothDevice? =
        getScanResult(mac)?.device

    fun getConnectJob(mac: String): Job? =
        _connectJobs[mac]

    fun getGattClientScope(mac: String): GattClientScope? =
        _gattClientScopes[mac]

    fun getGattService(mac: String, characteristicsUUID: UUID): GattService? =
        getGattClientScope(mac)?.services?.find { service ->
            service.characteristics.find { characteristic ->
                characteristic.uuid.uUID2str() == characteristicsUUID.uUID2str()
            } != null
        }

    fun getGattCharacteristic(mac: String, characteristicsUUID: UUID): GattCharacteristic? =
        getGattService(mac, characteristicsUUID)?.getCharacteristic(characteristicsUUID)

    //////////////////////////////////////////////////////////////////////////

    suspend fun readGattCharacteristic(mac: String, uUID: UUID): ByteArray? =
        getGattCharacteristic(mac, uUID)?.let { characteristic ->
            val result = getGattClientScope(mac)?.readCharacteristic(characteristic)
            return if (result?.isSuccess == true) result.getOrNull() else null
        }

    suspend fun writeCharacteristic(mac: String, uUID: UUID, bytes: ByteArray): Boolean =
        getGattCharacteristic(mac, uUID)?.let { characteristic ->
            val result = getGattClientScope(mac)?.writeCharacteristic(characteristic, bytes)
            return result?.isSuccess == true
        } ?: false

    fun subscribeToCharacteristic(mac: String, uUID: UUID): Flow<ByteArray>? {
        var subscribeFlow = _subscribeFlows[mac]
        if (_subscribeFlows.contains(mac)) {
            return subscribeFlow//Prevent duplicate subscriptions
        } else {
            getGattCharacteristic(mac, uUID)?.let { characteristic ->
                subscribeFlow = getGattClientScope(mac)?.subscribeToCharacteristic(characteristic)
                _subscribeFlows[mac] = subscribeFlow
                getScope().launch {
                    subscribeFlow?.collect {
                        withContext(Dispatchers.Main) {
                            _subscribeListeners[mac]?.invoke(it)
                        }
                    }
                }
                return subscribeFlow
            } ?: return null
        }
    }

    //////////////////////////////////////////////////////////////////////////

    fun addSubscribeListener(mac: String, listener: IA_Listener<ByteArray>) {
        _subscribeListeners[mac] = listener
    }

    fun addScanResult(scanResult: ScanResult) {
        _bluetoothDevices[scanResult.deviceAddress.address] = scanResult
    }

    fun addConnectJob(mac: String, job: Job) {
        _connectJobs[mac] = job
    }

    fun addGattClientScope(mac: String, gattClientScope: GattClientScope) {
        _gattClientScopes[mac] = gattClientScope
    }

    fun removeSubscribeListener(mac: String): IA_Listener<ByteArray>? =
        _subscribeListeners.remove(mac)

    fun removeScanResult(mac: String): ScanResult? =
        _bluetoothDevices.remove(mac)

    fun removeConnectJob(mac: String): Job? =
        _connectJobs.remove(mac)

    fun removeClientScope(mac: String): GattClientScope? =
        _gattClientScopes.remove(mac)

    //////////////////////////////////////////////////////////////////////////

    fun disconnectAll() {
        _gattClientScopes.clear()
        for (i in _connectJobs) {
            i.value.cancel()
        }
        _connectJobs.clear()
    }

    //////////////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    private object INSTANCE {
        val holder = BluetoothKBleX()
    }
}