package com.mozhimen.bluetoothk.ble.androidx

import android.app.Activity
import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.GattCharacteristic
import androidx.bluetooth.GattClientScope
import androidx.bluetooth.GattService
import androidx.bluetooth.ScanResult
import com.mozhimen.bluetoothk.basic.utils.UtilBluetooth
import com.mozhimen.bluetoothk.ble.androidx.commons.IBluetoothKXClientListener
import com.mozhimen.kotlin.elemk.commons.IExt_AListener
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.kotlin.utilk.java.util.uUID2str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @ClassName BluetooKBleX
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2025/3/26 21:48
 * @Version 1.0
 */
class BluetoothKBleX : IUtilK {
    private var _scanResults: ConcurrentHashMap<String, ScanResult> = ConcurrentHashMap<String, ScanResult>()
    private var _connectJobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap<String, Job>()
    private var _gattClientScopes: ConcurrentHashMap<String, GattClientScope> = ConcurrentHashMap<String, GattClientScope>()
    private val _subscribeFlows: ConcurrentHashMap<String, Flow<ByteArray>?> = ConcurrentHashMap<String, Flow<ByteArray>?>()
    private val _iBluetoothKXClientListeners: ConcurrentHashMap<String, LinkedList<IBluetoothKXClientListener>> =
        ConcurrentHashMap<String, LinkedList<IBluetoothKXClientListener>>()

    //////////////////////////////////////////////////////////////////////////

    @OptIn(DelicateCoroutinesApi::class)
    fun getScope(): CoroutineScope =
        GlobalScope

    fun getSubscribeFlow(mac: String, uUIDService: String, uUIDCharacteristics: String): Flow<ByteArray>? =
        _subscribeFlows[mac + uUIDService + uUIDCharacteristics]

    fun getIBluetoothKXClientListener(mac: String): LinkedList<IBluetoothKXClientListener>? =
        _iBluetoothKXClientListeners[mac]

    fun getScanResult(mac: String): ScanResult? =
        _scanResults[mac]

    fun getBluetoothDevice(mac: String): BluetoothDevice? =
        getScanResult(mac)?.device

    fun getConnectJob(mac: String): Job? =
        _connectJobs[mac]

    fun getGattClientScope(mac: String): GattClientScope? =
        _gattClientScopes[mac]

    fun getGattService_characteristics(mac: String, uUIDCharacteristics: UUID): GattService? =
        getGattClientScope(mac)?.services?.find { service ->
            service.characteristics.find { characteristic ->
                characteristic.uuid.uUID2str() == uUIDCharacteristics.uUID2str()
            } != null
        }

    fun getGattService_service(mac: String, uUIDService: UUID): GattService? =
        getGattClientScope(mac)?.getService(uUIDService)

    fun getGattCharacteristic(mac: String, uUIDService: UUID, uUIDCharacteristics: UUID): GattCharacteristic? =
        getGattService_service(mac, uUIDService)?.getCharacteristic(uUIDCharacteristics)

    fun getGattCharacteristic(mac: String, uUIDCharacteristics: UUID): GattCharacteristic? =
        getGattService_characteristics(mac, uUIDCharacteristics)?.getCharacteristic(uUIDCharacteristics)

    //////////////////////////////////////////////////////////////////////////

    fun addIBluetoothKXClientListener(mac: String, listener: IBluetoothKXClientListener) {
        var list = getIBluetoothKXClientListener(mac)
        if (list == null) {
            list = LinkedList()
            _iBluetoothKXClientListeners[mac] = list
            UtilKLogWrapper.d(TAG, "addIBluetoothKXClientListener: ")
        }
        if (!getIBluetoothKXClientListener(mac)!!.contains(listener)) {
            list.add(listener)
        }
    }

    fun addScanResult(scanResult: ScanResult) {
        _scanResults[scanResult.deviceAddress.address] = scanResult
    }

    fun addConnectJob(mac: String, job: Job) {
        _connectJobs[mac] = job
    }

    fun addGattClientScope(mac: String, gattClientScope: GattClientScope) {
        _gattClientScopes[mac] = gattClientScope
    }

    fun addSubscribeFlows(mac: String, uUIDService: String, uUIDCharacteristics: String, subscribeFlow: Flow<ByteArray>) {
        _subscribeFlows[mac + uUIDService + uUIDCharacteristics] = subscribeFlow
    }

    fun removeIBluetoothKXClientListeners(mac: String): LinkedList<IBluetoothKXClientListener>? =
        _iBluetoothKXClientListeners.remove(mac)

    fun removeIBluetoothKXClientListener(mac: String, listener: IBluetoothKXClientListener): Boolean? =
        getIBluetoothKXClientListener(mac)?.remove(listener)

    fun removeScanResult(mac: String): ScanResult? =
        _scanResults.remove(mac)

    fun removeConnectJob(mac: String): Job? =
        _connectJobs.remove(mac)

    fun removeGattClientScope(mac: String): GattClientScope? =
        _gattClientScopes.remove(mac)

    //////////////////////////////////////////////////////////////////////////

    fun <R> connect(mac: String, activity: Activity, block: IExt_AListener<GattClientScope, R>) {
        UtilBluetooth.requestBluetoothBlePermission(activity, onGranted = {
            if (mac.isNotEmpty() && getScanResult(mac) != null && getConnectJob(mac) == null) {
                getIBluetoothKXClientListener(mac)?.forEach { it.onConnecting() }
                getScope().launch {
                    BluetoothLe(activity.applicationContext).connectGatt(getBluetoothDevice(mac)!!) {
                        addGattClientScope(mac, this)
                        withContext(Dispatchers.Main) {
                            getIBluetoothKXClientListener(mac)?.forEach { it.onConnected() }
                            block()
                        }
                        awaitCancellation()
                    }
                }.also { addConnectJob(mac, it) }
            } else
                getIBluetoothKXClientListener(mac)?.forEach { it.onConnectFail() }
        }, {
            getIBluetoothKXClientListener(mac)?.forEach { it.onConnectFail() }
        })
    }

    fun disconnect(mac: String) {
        removeGattClientScope(mac)
        getConnectJob(mac)?.cancel()
        removeConnectJob(mac)
        getIBluetoothKXClientListener(mac)?.forEach { it.onDisConnected() }
        removeIBluetoothKXClientListeners(mac)
    }

    fun disconnectAll() {
        _gattClientScopes.clear()
        for (i in _connectJobs) {
            i.value.cancel()
        }
        _connectJobs.clear()
        for (j in _iBluetoothKXClientListeners) {
            j.value.forEach { it.onDisConnected() }
        }
        _iBluetoothKXClientListeners.clear()
    }

    //////////////////////////////////////////////////////////////////////////

    suspend fun readGattCharacteristic(mac: String, uUIDService: UUID, uUIDCharacteristics: UUID): ByteArray? =
        getGattCharacteristic(mac, uUIDService, uUIDCharacteristics)?.let { characteristic ->
            val result = getGattClientScope(mac)?.readCharacteristic(characteristic)
            return if (result?.isSuccess == true) result.getOrNull()?.also { bytes -> getIBluetoothKXClientListener(mac)?.forEach { it.onReadGattCharacteristic(bytes) } } else null
        }

    suspend fun writeCharacteristic(mac: String, uUIDService: UUID, uUIDCharacteristics: UUID, bytes: ByteArray): Boolean =
        getGattCharacteristic(mac, uUIDService, uUIDCharacteristics)?.let { characteristic ->
            val result = getGattClientScope(mac)?.writeCharacteristic(characteristic, bytes)
            return result?.isSuccess == true
        } ?: false

    private fun createSubscribeFlow(mac: String, uUIDService: UUID, uUIDCharacteristics: UUID): Flow<ByteArray>? {
        val subscribeFlow = getSubscribeFlow(mac, uUIDService.uUID2str(), uUIDCharacteristics.uUID2str())
        if (subscribeFlow != null) {
            return subscribeFlow//Prevent duplicate subscriptions
        } else {
            getGattCharacteristic(mac, uUIDService, uUIDCharacteristics)?.let { characteristic ->
                return getGattClientScope(mac)?.subscribeToCharacteristic(characteristic)?.also { addSubscribeFlows(mac, uUIDService.uUID2str(), uUIDCharacteristics.uUID2str(), it) } ?: return null
            } ?: return null
        }
    }

    fun subscribeToCharacteristic(mac: String, uUIDService: UUID, uUIDCharacteristics: UUID) {
        if (!_subscribeFlows.containsKey(mac + uUIDService.uUID2str() + uUIDCharacteristics.uUID2str())) {
            UtilKLogWrapper.d(TAG, "subscribeToCharacteristic: ")
            getScope().launch {
                createSubscribeFlow(mac, uUIDService, uUIDCharacteristics)?.collect { bytes ->
                    withContext(Dispatchers.Main) {
                        getIBluetoothKXClientListener(mac)?.forEach { it.onReadGattCharacteristic(bytes) }
                    }
                }
            }
        }
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