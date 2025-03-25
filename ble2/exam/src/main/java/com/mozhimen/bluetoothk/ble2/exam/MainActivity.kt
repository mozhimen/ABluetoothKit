package com.mozhimen.bluetoothk.ble2.exam

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.IDataAvailableListener
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.IServicesDiscoveredListener
import com.mozhimen.bluetoothk.ble2.exam.BluetoothLeService.LocalBinder
import com.mozhimen.bluetoothk.ble2.exam.databinding.ActivityMain3Binding
import com.mozhimen.kotlin.utilk.android.util.UtilKLogWrapper
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
    companion object {
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }

    //////////////////////////////////////////////////////////////////////////

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var isSupportLe = false
    private var isBluetoothEnabled = false
    private val devices: MutableList<BluetoothDevice> = ArrayList()
    private var mBluetoothLeService: BluetoothLeService? = null
    private var mAdapter: Ble2DeviceAdapter? = null
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var mDeviceAddress: String? = null

    /**
     * 保存0307 查询的用户信息
     */
    private val userHexString: MutableList<String> = ArrayList()
    private var count = 0

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }
    private var mHandler: Handler? = null
    private var userInfo: ByteArray? = null
    private var baseWeight: String? = null
    private var isRespond = false
    private var isStartSetUser = false
    private var isStartScan = false

    private val mlLeScanCallback: BluetoothAdapter.LeScanCallback = object : BluetoothAdapter.LeScanCallback {
        @SuppressLint("LongLogTag", "MissingPermission")
        override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
            var descripter: String? = null
            if (scanRecord != null) {
                descripter = Utils.getHexString(scanRecord)
                UtilKLogWrapper.d(TAG, "onLeScan: descripter 开始连接 ==$descripter")

                if (scanRecord[0].toInt() == 0xFA && scanRecord[1].toInt() == 0xFB) {
                    //send MANUFACTIRE_DATA
                    val intent = Intent(BleDataManager.ACTION_BLE_MANUFACTIRE_DATA)
                    intent.putExtra(BleDataManager.ACTION_BLE_MANUFACTIRE_DATA, descripter)

                    if (descripter != null && device != null && !devices.contains(device)) {
                        if (device.name != null) {
                            mDeviceAddress = device.address
                            mBluetoothLeService!!.connect(mDeviceAddress)
                            mBluetoothAdapter!!.stopLeScan(this)
                            runOnUiThread {
                                devices.add(device)
                                mAdapter?.notifyDataSetChanged()
                            }
                            //devices.add(device);
                            UtilKLogWrapper.d(TAG, "onLeScan: device name =" + device.toString() + "," + device.uuids + "," + device.name + "," + device.bondState + "," + device.javaClass)
                        }
                    }
                }
            }
        } /* if (device != null && !devices.contains(device)) {
                if ((device.getName() != null) && device.getName().equals("SENSSUN CLOUD")){

                    if (scanRecord !=null) {
                        String descripter = Utils.bytesToHex(scanRecord,scanRecord.length);
                    }
                    mDeviceAddress = device.getAddress();
                    mBluetoothLeService.connect(mDeviceAddress);
                    mBluetoothAdapter.stopLeScan(this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devices.add(device);
                            initListView(devices);
                        }
                    });
                }
            }
        }*/
    }

    private val _iServicesDiscoveredListener: IServicesDiscoveredListener = object : IServicesDiscoveredListener {
        override fun onServicesDiscovered(bluetoothGatt: BluetoothGatt?) {
//        userInfos.clear()
            displayGattServices(mBluetoothLeService!!.getSupportedGattServices())
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
                val chars = gatt.getService(characteristic?.uuid).characteristics

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
            if (characteristic?.uuid.toString() == BleDataManager.UUID_UNLOCK_DATA_NOTIFY) {
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

    private val _onItemClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            val device = parent.getItemAtPosition(position) as BluetoothDevice
            if (mBluetoothLeService != null) mBluetoothLeService!!.connect(device.address)
        }
    }

    //////////////////////////////////////////////////////////////////////////

    override fun initData(savedInstanceState: Bundle?) {
        mHandler = Handler()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
            }
        }
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        initBluetoothService(mBluetoothAdapter)
        super.initData(savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {
        vb.openButton.setOnClickListener {
            if (!isBluetoothEnabled) {
                mBluetoothAdapter!!.enable()
            }
        }
        vb.scanButton.setOnClickListener {
            startScan(mBluetoothAdapter, false)
        }
        mAdapter = Ble2DeviceAdapter(emptyList())
        vb.listView.adapter = mAdapter
        vb.listView.onItemClickListener = _onItemClickListener
    }

    override fun onResume() {
        super.onResume()
        if (mBluetoothLeService != null) {
            mBluetoothLeService!!.setServicesDiscoveredListener(_iServicesDiscoveredListener)
            mBluetoothLeService!!.setDataAvailableListener(_iDataAvailableListener)
        }
        if (isBluetoothEnabled) {
            startScan(mBluetoothAdapter, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO request success
                Log.d(TAG, "wcy request success")
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private fun initBluetoothService(mBluetoothAdapter: BluetoothAdapter?) {
        if (mBluetoothAdapter == null) {
            return
        }
        isSupportLe = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
        isBluetoothEnabled = mBluetoothAdapter.isEnabled
    }

    private fun startScan(bluetoothAdapter: BluetoothAdapter?, enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                isStartScan = false
                mBluetoothAdapter!!.stopLeScan(mlLeScanCallback)
                UtilKLogWrapper.d(TAG, "startScan: 停止扫描")
            }, (60 * 1000).toLong())

            isStartScan = true
            UtilKLogWrapper.d(TAG, "startScan: 开始扫描")
            mBluetoothAdapter!!.startLeScan(mlLeScanCallback)
        } else {
            isStartScan = false
            UtilKLogWrapper.d(TAG, "startScan: 停止扫描")
            mBluetoothAdapter!!.stopLeScan(mlLeScanCallback)
        }
    }

    private fun stopScan(bluetoothAdapter: BluetoothAdapter) {
        bluetoothAdapter.startLeScan(mlLeScanCallback)
    }

    private fun doneCharacteristicChangedData(gatt: BluetoothGatt?, character: BluetoothGattCharacteristic) {
        var optionCMMD: String? = null
        var dataOutHead: String? = null
        if (character.uuid.toString() == BleDataManager.UUID_UNLOCK_DATA_NOTIFY) {
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
                            val jjj = byteArrayOf(0x00, 0x00)
                            writeUserInfoToScale(0x00.toByte(), jjj)
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
            UtilKLogWrapper.d(TAG, "displayGattServices: getService ====" + gattService.uuid + ",," + gattService.type)
            val gattCharacteristics = gattService.characteristics
            for (gattCharacteristic in gattCharacteristics) {
                UtilKLogWrapper.d(TAG, "displayGattServices: gattCharacteristic uuid ==" + gattCharacteristic.uuid + ",," + gattCharacteristic.value + ",," + gattCharacteristic.properties)
                if (gattCharacteristic.uuid.toString() == BleDataManager.UUID_UNLOCK_DATA_NOTIFY) {
                    BleDataManager.gattCharacteristic_notify = gattCharacteristic
                    mBluetoothLeService!!.setCharacteristicNotification(gattCharacteristic, true)
                }
                if (gattCharacteristic.uuid.toString() == BleDataManager.UUID_UNLOCK_DATA_WRITE) {
                    BleDataManager.gattCharacteristic_write = gattCharacteristic
                }
            }
        }
    }

    private fun setCharacteristicType(bluetoothGattCharacteristic: BluetoothGattCharacteristic?) {
        if (bluetoothGattCharacteristic != null) {
            val charaProp = bluetoothGattCharacteristic.properties
            if ((charaProp or BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService!!.setCharacteristicNotification(
                        mNotifyCharacteristic, false
                    )
                    mNotifyCharacteristic = null
                }
                //mBluetoothLeService.readCharacteristic(bluetoothGattCharacteristic);
            }
            if ((charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = bluetoothGattCharacteristic
                mBluetoothLeService!!.setCharacteristicNotification(
                    bluetoothGattCharacteristic, true
                )
            }
        }
    }

    /**
     * option  0x00 /0x01 /0x02  新增／修改／删除///定义19个字节字符的数组
     */
    private fun writeUserInfoToScale(option: Byte, userInfoByte: ByteArray?) {
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
            BleDataManager.gattCharacteristic_write?.setValue(userInfo)
        }
        mBluetoothLeService!!.writeCharacteristic(BleDataManager.gattCharacteristic_write)
    }

    /**
     * 手机请求同步
     * pinHex userPin 2byte 7,8
     * optionTag : 00-update new data
     * 1-50 recent 1-50 datas
     * 0xaa  delete all data
     */
    private fun delectedUserInfo(pinHex: ByteArray, optionTag: Byte) {
        val bytes = byteArrayOf(0x10, 0x00, 0x00, 0xc5.toByte(), 0x0b, 0x03, 0x02, 0x00, 0x00, 0x00, 0x00)
        if (pinHex.size < 2) {
            UtilKLogWrapper.d(TAG, "delectedUserInfo: pin error defauled 0000")
        } else {
            bytes[7] = pinHex[0]
            bytes[8] = pinHex[1]
        }
        if (optionTag.toInt() != 0) {
            bytes[9] = optionTag
        } else {
            bytes[9] = BleDataManager.OPTION_SYNC_DATA_DEFAULED
        }
    }
}