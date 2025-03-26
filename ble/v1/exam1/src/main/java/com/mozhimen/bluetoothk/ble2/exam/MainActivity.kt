package com.mozhimen.bluetoothk.ble2.exam

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.AdapterView
import com.mozhimen.bluetoothk.classic.BluetoothKClassic
import com.mozhimen.bluetoothk.ble.BluetoothKBle2ScanProxy
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.IDataAvailableListener
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.IServicesDiscoveredListener
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.LocalBinder
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiCall_BindViewLifecycle
import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy
import com.mozhimen.kotlin.lintk.optins.OApiInit_InApplication
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
import com.mozhimen.kotlin.utilk.android.widget.showToast
import com.mozhimen.kotlin.utilk.commons.IUtilK
import com.mozhimen.uik.databinding.bases.viewbinding.activity.BaseActivityVB

/**
 * @ClassName BleController
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
@SuppressLint("MissingPermission")
class MainActivity : BaseActivityVB<ActivityMain3Binding>(), IUtilK {
    private var _bluetoothLeService: BluetoothLeService? = null
    private var _ble2DeviceAdapter: Ble2DeviceAdapter = Ble2DeviceAdapter(emptyList())
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var _bluetoothDevices: MutableList<BluetoothDevice> = ArrayList()

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    private val _bluetoothKBle2ScanProxy by lazy { BluetoothKBle2ScanProxy() }

    /**
     * 保存0307 查询的用户信息
     */
    private val userHexString: MutableList<String> = ArrayList()
    private var count = 0

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            _bluetoothLeService = (service as LocalBinder).service
            if (!_bluetoothLeService!!.initialize()) {
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            _bluetoothLeService = null
        }
    }
    private var userInfo: ByteArray? = null
    private var baseWeight: String? = null
    private var isRespond = false
    private var isStartSetUser = false

    private val _iServicesDiscoveredListener: IServicesDiscoveredListener = object : IServicesDiscoveredListener {
        override fun onServicesDiscovered(bluetoothGatt: BluetoothGatt?) {
            for (gattService in _bluetoothLeService?.getSupportedGattServices() ?: return) {
                UtilKLogWrapper.d(TAG, "displayGattServices: getService ====" + gattService.uuid + ",," + gattService.type)
                for (gattCharacteristic in gattService.characteristics) {
                    UtilKLogWrapper.d(TAG, "displayGattServices: gattCharacteristic uuid ==" + gattCharacteristic.uuid + ",," + gattCharacteristic.value + ",," + gattCharacteristic.properties)
                    if (gattCharacteristic.uuid.toString() == CBle2.UUID_UNLOCK_DATA_NOTIFY) {
                        CBle2.gattCharacteristic_notify = gattCharacteristic
                        _bluetoothLeService!!.setCharacteristicNotification(gattCharacteristic, true)
                    }
                    if (gattCharacteristic.uuid.toString() == CBle2.UUID_UNLOCK_DATA_WRITE) {
                        CBle2.gattCharacteristic_write = gattCharacteristic
                    }
                }
            }
        }
    }

    private val _iDataAvailableListener = object : IDataAvailableListener {
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            var stringBuilder: StringBuilder? = null
            val data = characteristic?.value
            if (data != null && data.size > 0) {
                stringBuilder = StringBuilder(data.size)
                for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
            }
            if (gatt?.getService(characteristic?.uuid) != null) {
                val service = gatt.getService(characteristic!!.uuid).uuid
                val chars = gatt.getService(characteristic.uuid).characteristics

                for (chara in chars) {
                    UtilKLogWrapper.d(TAG, "onCharacteristicRead: service==" + service + ",chara==UUID==" + chara.uuid + ",de= " + chara.permissions + ",dede==" + chara.properties)
                }
            }
            UtilKLogWrapper.d(TAG, "onCharacteristicRead: get read ==${characteristic!!.uuid},,${String(data!!)} ${stringBuilder.toString()}")
            UtilKLogWrapper.d(TAG, "onCharacteristicRead: readvalue ===" + String(characteristic.value))
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            var stringBuilder: StringBuilder? = null
            val data = characteristic?.value
            if (data != null && data.size > 0) {
                stringBuilder = StringBuilder(data.size)
                for (byteChar in data) stringBuilder!!.append(String.format("%02X ", byteChar))
            }
            if (stringBuilder != null) {
                UtilKLogWrapper.d(TAG, "onCharacteristicRead: get write ==" + characteristic?.uuid + ",," + stringBuilder.toString())
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (characteristic?.uuid.toString() == CBle2.UUID_UNLOCK_DATA_NOTIFY) {
                if (characteristic?.value != null) {
                    val tempData = Utils.bytesToHex(characteristic.value, characteristic.value.size)
                    //Log.d(TAG,"临时数据一直在变=="+Integer.parseInt(tempData.substring(15,17),16)*10+"."+Integer.parseInt(tempData.substring(17,19),16));
                    if (tempData.substring(24, 26) == "AA") {
                        UtilKLogWrapper.d(TAG, "onCharacteristicChanged: result 不变了 datd==" + Utils.bytesToHexString(characteristic.value))
                        baseWeight = tempData.substring(14, 18)
                    }
                    doneCharacteristicChangedData(gatt, characteristic)
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    @OptIn(OApiInit_InApplication::class)
    override fun initData(savedInstanceState: Bundle?) {
        if (BluetoothKClassic.instance.getBluetoothAdapter() == null) {
            "没有找到蓝牙适配器".showToast()
            onBackPressed()
            return
        }
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
        super.initData(savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {
        vb.listView.adapter = _ble2DeviceAdapter
        vb.listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val device = parent.getItemAtPosition(position) as BluetoothDevice
            if (_bluetoothLeService != null) {
                _bluetoothLeService!!.connect(device.address)
                _bluetoothLeService!!.setServicesDiscoveredListener(_iServicesDiscoveredListener)
                _bluetoothLeService!!.setDataAvailableListener(_iDataAvailableListener)
            }
        }
    }

    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
    override fun initObserver() {
        _bluetoothKBle2ScanProxy.startScan(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        _bluetoothLeService = null
    }

    //////////////////////////////////////////////////////////////////////////

    private fun doneCharacteristicChangedData(gatt: BluetoothGatt?, character: BluetoothGattCharacteristic) {
        var optionCMMD: String? = null
        var dataOutHead: String? = null
        if (character.uuid.toString() == CBle2.UUID_UNLOCK_DATA_NOTIFY) {
            if (character.value == null) {
                UtilKLogWrapper.d(TAG, "character.getUuid().toString() not respond data")
            } else {
                val dataHex = Utils.bytesToHex(character.value, character.value.size)
                UtilKLogWrapper.d(TAG, "doneCharacteristicChangedData: doneCharacteristicChangedData=$dataHex")

                optionCMMD = dataHex.substring(10, 14)
                if (isInvalidForResult(optionCMMD, dataHex)) {
                    return
                }
                isStartSetUser = true
                when (optionCMMD) {
                    Utils.CMMD_GET_TEMP_DATA -> {
                        dataOutHead = dataHex.substring(14, 24) //weight
                        if (!isRespond && isStartSetUser && count < 3) {
                            var temp: ByteArray
//        var userInfoO: UserInfo? = null
//        if (userInfos.size != 0) {
//            userInfoO = userInfos[1]
//        }

                            //连接以后设置用户的信息
                            if (baseWeight == null) {
                                baseWeight = "0000"
                            } else {
                                UtilKLogWrapper.d(TAG, "writeUserInfoToScale: baseWeight ==" + baseWeight.toString())
                            }
                            userInfo = byteArrayOf(0x10, 0x00, 0x00, 0xc5.toByte(), 0x13, 0x03, 0x01, 0x00, 0x06, 0x90.toByte(), 0x81.toByte(), 0x01, 0x9e.toByte(), 0x1a, 0x01, 0x00, 0x00, 0x3D, 0x00)
//        if (userInfoO != null) {
//            userInfo!![8] = userInfoO.getSerialNum()!!.toByte()
//            temp = Utils.hexString2Bytes(userInfoO!!.getPinId())
//            if (temp.size > 1) {
//                userInfo!![9] = temp[0]
//                userInfo!![10] = temp[1]
//            }
//            temp = Utils.hexString2Bytes(userInfoO.getSexId())
//            if (temp.size > 0) {
//                userInfo!![11] = temp[0]
//            }
//            temp = Utils.hexString2Bytes(userInfoO.getHeight())
//            if (temp.size > 0) {
//                userInfo!![12] = temp[0]
//            }
//            temp = Utils.hexString2Bytes(userInfoO.getAge())
//            if (temp.size > 0) {
//                userInfo!![13] = temp[0]
//            }
//            temp = Utils.hexString2Bytes(userInfoO.getPhys())
//            if (temp.size > 0) {
//                userInfo!![14] = temp[0]
//            }
//            temp = Utils.hexString2Bytes(userInfoO.getWeight())
//            if (temp.size > 1) {
//                userInfo!![16] = temp[0]
//                userInfo!![17] = temp[1]
//            }
//        }
                            temp = Utils.constructionCheckCode(Utils.getHexString(userInfo))
                            if (temp.size > 0) {
                                userInfo!![18] = temp[0]
                            }

                            if (userInfo != null) {
                                CBle2.gattCharacteristic_write?.setValue(userInfo)
                            }
                            _bluetoothLeService!!.writeCharacteristic(CBle2.gattCharacteristic_write)
                        }
                    }

                    Utils.CMMD_SET_USER_RESPOND -> {
                        count = count + 1
                        UtilKLogWrapper.d(TAG, "doneCharacteristicChangedData: 已经得到回复第" + count + "次")
                        dataOutHead = dataHex.substring(14, 20) //设置用户返回标志和pin
                    }

                    Utils.CMMD_USERINFOS_RESPOND -> {
                        isStartSetUser = false
                        dataOutHead = dataHex.substring(14, 34)
                        userHexString.add(dataOutHead.substring(16))
//                        val userInfo = UserInfo(
//                            dataHex.substring(16, 18), dataHex.substring(18, 22), dataHex.substring(22, 24),
//                            dataHex.substring(24, 26), dataHex.substring(26, 28), dataHex.substring(28, 30), dataHex.substring(30, 34)
//                        )

//                        if (!userInfos.contains(userInfo)) {
//                            userInfos.add(userInfo)
//                        }

                        if (character.value[7].toInt() == 8) {
                        }
//                        Log.d(TAG, "userinfos==" + userInfo.toString() + "all num =" + character.value[7] + ",size=" + userInfos.size)
                    }

                    Utils.CMMD_GET_RESULT_DATA -> {
                        //返回所测试的所有结果，可能在1416中的字段可看出是实时记录还是历史记录，如果是FF便是发送完毕和无记录
                        dataOutHead = dataHex.substring(14, 56)
                        isRespond = true
                        count = 0
                    }

                    else -> UtilKLogWrapper.d(TAG, "doneCharacteristicChangedData: ot same data")
                }
            }
        }
    }

    private fun isInvalidForResult(optionCMMD: String, dataOutHead: String): Boolean {
        val lenght = dataOutHead.length
        UtilKLogWrapper.d(TAG, "isInvalidForResult: option=$optionCMMD ,dataOutHead=$dataOutHead,len =$lenght")
        return if (optionCMMD == Utils.CMMD_GET_TEMP_DATA && lenght < 28) {
            true
        } else if (optionCMMD == Utils.CMMD_USERINFOS_RESPOND && lenght < 36) {
            true
        } else if (optionCMMD == Utils.CMMD_SET_USER_RESPOND && lenght < 22) {
            true
        } else if (optionCMMD == Utils.CMMD_GET_RESULT_DATA && lenght < 58) {
            true
        } else {
            false
        }
    }
}