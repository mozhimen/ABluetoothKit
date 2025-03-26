package com.mozhimen.bluetoothk.ble2.exam

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.IConnectedListener
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.IDataAvailableListener
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.IServicesDiscoveredListener
//import com.mozhimen.bluetoothk.scale.senssun.userentities.UserInfo
import java.util.UUID

/**
 * @ClassName BleDataManager
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
/**
 * Created by wucaiyan on 2017/11/14.
 */
/**
 * Created by wucaiyan on 2017/11/14.
 */
class BleDataManager(context: Context?, bluetoothAdapter: BluetoothAdapter?) : IDataAvailableListener, IServicesDiscoveredListener, IConnectedListener {
    private val mBluetoothLeService = BluetoothLeService()

    private val mContext: Context? = null

    /**
     * 保存0307 查询的用户信息
     */
    private val userHexString: MutableList<String> = ArrayList()
    private val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val devices: MutableList<BluetoothDevice> = ArrayList()
    private var mDeviceAddress: String? = null
    private val mHandler: Handler
    private var userInfo: ByteArray? = null
    private var baseWeight: String? = null
    private var isRespond = false
    private var isStartSetUser = false
    // 标志是否开始扫描，最多1分钟
    var isStartScan: Boolean = false

    override fun onServicesDiscovered(bluetoothGatt: BluetoothGatt?) {
        Log.d(TAG, "onServicesDiscovered")
        displayGattServices(mBluetoothLeService.getSupportedGattServices())
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Log.d(TAG, "onCharacteristicRead")
        if (characteristic?.value != null) {
            Log.d(
                TAG, """
     onCharacteristicRead UUID =${characteristic.uuid}
     ,value =${Utils.bytesToHexString(characteristic.value)}
     """.trimIndent()
            )
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        Log.d(TAG, "onCharacteristicWrite")
        if (characteristic?.value != null) {
            Log.d(
                TAG, """
     onCharacteristicWrite UUID =${characteristic.uuid}
     ,value =${Utils.bytesToHexString(characteristic.value)}
     """.trimIndent()
            )
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        Log.d(TAG, "onCharacteristicChanged")
        if (characteristic?.uuid.toString() == UUID_UNLOCK_DATA_NOTIFY) {
            if (characteristic!!.value != null) {
                val tempData = Utils.bytesToHex(characteristic.value, characteristic.value.size)
                //Log.d(TAG,"临时数据一直在变=="+Integer.parseInt(tempData.substring(15,17),16)*10+"."+Integer.parseInt(tempData.substring(17,19),16));
                if (tempData.substring(24, 26) == "AA") {
                    Log.d(TAG, "result 不变了 datd==" + Utils.bytesToHexString(characteristic.value))
                    baseWeight = tempData.substring(14, 18)
                }
                doneCharacteristicChangedData(gatt, characteristic)
            }
        }
    }

    override fun onConnect(bluetoothGatt: BluetoothGatt?, status: Int, newStatus: Int) {
        Log.d(TAG, "onConnect")
        val intent = Intent(ACTION_BLE_CONNECT_STATE)
        if (newStatus == BluetoothProfile.STATE_CONNECTED) {
            intent.putExtra(ACTION_BLE_CONNECT_STATE, newStatus)
        } else if (newStatus == BluetoothProfile.STATE_DISCONNECTED) {
            intent.putExtra(ACTION_BLE_CONNECT_STATE, newStatus)
        }
        sendBroadcastForResult(intent)
    }

    /**
     * 发送结果广播
     */
    fun sendBroadcastForResult(intent: Intent?) {
        mContext!!.sendBroadcast(intent)
    }

//    val infoFromEleScales: List<UserInfo>
//        /**
//         * 获取当前所有电子秤中所有的用户信息
//         */
//        get() {
//            if (userInfos == null) {
//                userInfos = ArrayList()
//            }
//
//
//            return userInfos as MutableList<UserInfo>
//        }


    /**
     * get the supported characteristics , maybe need to change
     *
     * @param gattServices gattServices
     */
    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) {
            return
        }
        for (gattService in gattServices) {
            Log.d(TAG, "getService ====" + gattService.uuid + ",," + gattService.type)
            val gattCharacteristics = gattService.characteristics
            for (gattCharacteristic in gattCharacteristics) {
                Log.d(TAG, "gattCharacteristic uuid ==" + gattCharacteristic.uuid + ",," + gattCharacteristic.value + ",," + gattCharacteristic.properties)
                if (gattCharacteristic.uuid.toString() == UUID_UNLOCK_DATA_NOTIFY) {
                    gattCharacteristic_notify = gattCharacteristic
                    mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true)
                }
                if (gattCharacteristic.uuid.toString() == UUID_UNLOCK_DATA_WRITE) {
                    gattCharacteristic_write = gattCharacteristic
                }
            }
        }
    }


    /**
     * 同步用户信息
     * option  0x00 /0x01 /0x02  新增／修改／删除///定义19个字节字符的数组
     */
    fun writeUserInfoToScale(option: Byte, userInfoByte: ByteArray?) {
        var temp: ByteArray
//        var userInfoO: UserInfo? = null
//        if (userInfos!!.size != 0) {
//            userInfoO = userInfos!![1]
//        }

        userInfo = byteArrayOf(0x10, 0x00, 0x00, 0xc5.toByte(), 0x13, 0x03, 0x01, 0x00, 0x06, 0x90.toByte(), 0x81.toByte(), 0x01, 0x9e.toByte(), 0x1a, 0x01, 0x00, 0x00, 0x3D, 0x00)
//        if (userInfoO != null) {
//            userInfo!![8] = userInfoO.getSerialNum()!!.toByte()
//            temp = Utils.hexString2Bytes(userInfoO.getPinId())
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
        /*byte[] temp ;
        //连接以后设置用户的信息
        if (baseWeight == null) {
            baseWeight = "0000";
        } else {
            Log.d(TAG,"baseWeight =="+baseWeight.toString());
        }
        userInfo = new byte[]{0x10, 0x00, 0x00, (byte) 0xc5, 0x13, 0x03, 0x01, 0x00, 0x06, (byte) 0x90, (byte) 0x81, 0x01, (byte) 0x9e, 0x1a, 0x01, 0x00,0x00,0x00,0x00};
        temp = Utils.getRandomNum(4);
        if (temp.length>1) {
            userInfo[9] = temp[0];
            userInfo[10] = temp[1];
        }
        temp = Utils.hexString2Bytes(baseWeight);
        if (temp.length>1) {
            userInfo[16] = temp[0];
            userInfo[17] = temp[1];
        }*/
        temp = Utils.constructionCheckCode(Utils.getHexString(userInfo))
        if (temp.size > 0) {
            userInfo!![18] = temp[0]
        }

        if (userInfo != null) {
            gattCharacteristic_write!!.setValue(userInfo)
        }
        mBluetoothLeService.writeCharacteristic(gattCharacteristic_write)
    }

    /**
     * 接收临时数据
     */
    fun doneCharacteristicChangedData(gatt: BluetoothGatt?, character: BluetoothGattCharacteristic) {
        var optionCMMD: String? = null
        var dataOutHead: String? = null
        val intent: Intent
        if (character.uuid.toString() == UUID_UNLOCK_DATA_NOTIFY) {
            if (character.value == null) {
                Log.d(TAG, "character.getUuid().toString() not respond data")
            } else {
                val dataHex = Utils.bytesToHex(character.value, character.value.size)
                Log.d(TAG, "doneCharacteristicChangedData=$dataHex")

                optionCMMD = dataHex.substring(10, 14)
                if (isInvalidForResult(optionCMMD, dataHex)) {
                    return
                }
                isStartSetUser = true
                when (optionCMMD) {
                    Utils.CMMD_GET_TEMP_DATA -> {
                        dataOutHead = dataHex.substring(14, 24) //weight
                        if (!isRespond && isStartSetUser && count < 2 && baseWeight != null) {
                            val jjj = byteArrayOf(0x00, 0x00)
                            writeUserInfoToScale(0x00.toByte(), jjj)
                        }
                        intent = Intent(ACTION_BLE_GET_DATA)
                        intent.putExtra(TYPE_DATA_TEMP_WEIGHT, dataOutHead)
                        sendBroadcastForResult(intent)
                    }

                    Utils.CMMD_SET_USER_RESPOND -> {
                        count = count++
                        Log.d(TAG, "已经得到回复第" + count + "次")
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

//                        if (!userInfos!!.contains(userInfo)) {
//                            userInfos!!.add(userInfo)
//                        }
                        if (character.value[7].toInt() == 8) {
                            isStartSetUser = true
                            intent = Intent(TYPE_DATA_ALL_USERINFO)
                            sendBroadcastForResult(intent)
                        }
//                        Log.d(TAG, "userinfos==" + userInfo.toString() + "all num =" + character.value[7] + ",size =" + userInfos!!.size)
                    }

                    Utils.CMMD_GET_RESULT_DATA -> {
                        //返回所测试的所有结果，可能在1416中的字段可看出是实时记录还是历史记录，如果是FF便是发送完毕和无记录
                        dataOutHead = dataHex.substring(14, 56)
                        isRespond = true
                        count = 0
                        intent = Intent(ACTION_BLE_GET_DATA)
                        intent.putExtra(TYPE_DATA_FAT_RATIO, dataOutHead)
                        sendBroadcastForResult(intent)
                    }

                    else -> Log.d(TAG, "not same data")
                }
            }
        }
    }

    /**
     * 判断接收到的数据是否有效
     */
    private fun isInvalidForResult(optionCMMD: String, dataOutHead: String): Boolean {
        val lenght = dataOutHead.length
        Log.d(TAG, "isInvalidForResult option=$optionCMMD ,dataOutHead=$dataOutHead,len =$lenght")
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

    /**
     * 开始扫描
     */
    @SuppressLint("MissingPermission")
    fun startScan(bluetoothAdapter: BluetoothAdapter?, enable: Boolean, iCallBackResult: ICallBackResult?) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed({
                isStartScan = false
                mBluetoothAdapter.stopLeScan(mlLeScanCallback)
                Log.d(TAG, "停止扫描")
            }, (60 * 1000).toLong())

            isStartScan = true
            Log.d(TAG, "开始扫描")
            mBluetoothAdapter.startLeScan(mlLeScanCallback)
        } else {
            isStartScan = false
            Log.d(TAG, "停止扫描")
            mBluetoothAdapter.stopLeScan(mlLeScanCallback)
        }
    }

    fun connect(address: String?) {
        if (address == null) {
            Log.d(TAG, "connect address null,please scan")
        }
        mHandler.postDelayed(mConnTimeOutRunnable, (90 * 1000).toLong())
        mHandler.postDelayed({ //真正开始连接设备
            mBluetoothLeService.connect(address)
            val intent = Intent(ACTION_BLE_CONNECT_STATE)
            intent.putExtra(ACTION_BLE_CONNECT_STATE, BluetoothProfile.STATE_CONNECTING)
            sendBroadcastForResult(intent)
        }, 100)
    }

    /**
     * 连接超时，回调
     */
    private val mConnTimeOutRunnable = Runnable { //资源释放
        mBluetoothLeService.disconnect()
        mBluetoothLeService.close()
    }


//    fun getUserListFromHar(iCallBack: ICallBackResult) {
//        if (userInfos != null) {
//            iCallBack.onSuccessed(userInfos)
//        } else {
//            iCallBack.onFailed()
//        }
//    }


    interface ICallBackResult {
        fun onSuccessed(`object`: Any?)
        fun onFailed()
    }


    private val mlLeScanCallback: BluetoothAdapter.LeScanCallback = object : BluetoothAdapter.LeScanCallback {
        @SuppressLint("LongLogTag", "MissingPermission")
        override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
            if (device != null && !devices.contains(device)) {
                if ((device.name != null) && device.name == "SENSSUN CLOUD") {
                    if (scanRecord != null) {
                        val descripter = Utils.bytesToHex(scanRecord, scanRecord.size)
                        Log.d(TAG, "descripter 开始连接 ==$descripter")
                        val intent = Intent(ACTION_BLE_MANUFACTIRE_DATA)
                        intent.putExtra(ACTION_BLE_MANUFACTIRE_DATA, descripter)
                        sendBroadcastForResult(intent)
                    }
                    mDeviceAddress = device.address
                    connect(mDeviceAddress)
                    mBluetoothAdapter.stopLeScan(this)
                    devices.add(device)
                    Log.d(TAG, "device name =" + device.toString() + "," + device.uuids + "," + device.name + "," + device.bondState + "," + device.javaClass)
                }
            }
        }
    }


    init {
        mBluetoothLeService.initialize()
        mHandler = Handler()
        mBluetoothLeService.setDataAvailableListener(this)
        mBluetoothLeService.setServicesDiscoveredListener(this)
        mBluetoothLeService.setStateConnected(this)
    }


    companion object {
        private const val TAG = "BleDataManager"
        var gattCharacteristic_write: BluetoothGattCharacteristic? = null
        var gattCharacteristic_notify: BluetoothGattCharacteristic? = null

        //体重秤与体脂秤数据传输协议  03xx
        const val FUCTION_NUM: Int = 0x03

        //智能终端至硬件设备的数据请求／应答  data 00（正常接收） /0a(中断)
        const val REQUEST_APP_TO_HARDWARE: Int = 0x00

        //硬件设备至智能终端的应答／请求
        const val REPONSE_HARDWARE_TO_APP: Int = 0x80

        val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID: UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb")
        var UUID_UNLOCK_DATA_SERVICE: String = "0000fff0-0000-1000-8000-00805f9b34fb"
        var UUID_UNLOCK_DATA_NOTIFY: String = "0000fff1-0000-1000-8000-00805f9b34fb"
        var UUID_UNLOCK_DATA_WRITE: String = "0000fff2-0000-1000-8000-00805f9b34fb"
        var ACTION_BLE_GET_DATA: String = "com.senssunhealth.ACTION_BLE_GET_DATA"
        var ACTION_BLE_MANUFACTIRE_DATA: String = "com.senssunhealth.ACTION_BLE_MANUFACTIRE_DATA"
        var ACTION_BLE_CONNECT_STATE: String = "com.senssunhealth.ACTION_BLE_CONNECT_STATE"

        var TYPE_DATA_ALL_USERINFO: String = "com.senssunhealth.TYPE_DATA_USERINFOS"
        var TYPE_DATA_FAT_RATIO: String = "com.senssunhealth.TYPE_DATA_USERINFOS"
        var TYPE_DATA_TEMP_WEIGHT: String = "com.senssunhealth.TYPE_DATA_TEMP_WEIGHT"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2

        const val OPTION_SYNC_DATA_DEFAULED: Byte = 0x00
        const val OPTION_DELETE_USER_DATA: Byte = 0xaa.toByte()


        private var count = 0
    }
}