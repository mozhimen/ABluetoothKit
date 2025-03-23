package com.mozhimen.bluetoothk.scale.senssun

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.bluetoothk.scale.senssun.BluetoothLeService.IDataAvailableListener
import com.mozhimen.bluetoothk.scale.senssun.BluetoothLeService.IServicesDiscoveredListener
import com.mozhimen.bluetoothk.scale.senssun.BluetoothLeService.LocalBinder
//import com.mozhimen.bluetoothk.scale.senssun.userentities.UserInfo

/**
 * @ClassName BleController
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
class BleController : AppCompatActivity(), View.OnClickListener, IServicesDiscoveredListener, IDataAvailableListener, AdapterView.OnItemClickListener {
    private var context: Context? = null

    private var openBt: Button? = null
    private var dataView: TextView? = null
    private var offBt: Button? = null
    private var scanBt: Button? = null
    private var deviceList: ListView? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var isSupportLe = false
    private var isBluetoothEnabled = false
    private val devices: MutableList<BluetoothDevice> = ArrayList()
    private var mBluetoothLeService: BluetoothLeService? = null
    private var mAdapter: ListViewAdapter? = null
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var readBt: Button? = null
    private var mDeviceAddress: String? = null

    /**
     * 保存0307 查询的用户信息
     */
//    private val userInfos: MutableList<UserInfo> = ArrayList()
    private val userHexString: MutableList<String> = ArrayList()


    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
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
    private var write: Button? = null
    private var userInfo: ByteArray? = null
    private var baseWeight: String? = null
    private var isRespond = false
    private var isStartSetUser = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        mHandler = Handler()
        context = applicationContext
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
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (mBluetoothLeService != null) {
            mBluetoothLeService!!.setServicesDiscoveredListener(this)
            mBluetoothLeService!!.setDataAvailableListener(this)
        }
        if (isBluetoothEnabled) {
            startScan(mBluetoothAdapter, true)
        }
    }

    private fun initBluetoothService(mBluetoothAdapter: BluetoothAdapter?) {
        if (mBluetoothAdapter == null) {
            return
        }
        isSupportLe = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
        isBluetoothEnabled = mBluetoothAdapter.isEnabled
    }

    private fun initView() {
        openBt = findViewById<View>(R.id.open_button) as Button
        offBt = findViewById<View>(R.id.off_button) as Button
        scanBt = findViewById<View>(R.id.scan_button) as Button
        dataView = findViewById<View>(R.id.textView) as TextView
        deviceList = findViewById<View>(R.id.listView) as ListView
        readBt = findViewById<View>(R.id.read) as Button
        write = findViewById<View>(R.id.write) as Button
        openBt!!.setOnClickListener(this)
        offBt!!.setOnClickListener(this)
        scanBt!!.setOnClickListener(this)
        readBt!!.setOnClickListener(this)
        write!!.setOnClickListener(this)
        initListView(devices)
    }


    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.open_button) {
            if (!isBluetoothEnabled) {
                mBluetoothAdapter!!.enable()
            }
        } else if (id == R.id.scan_button) {
            startScan(mBluetoothAdapter, false)
        } else if (id == R.id.read) {
        } else if (id == R.id.write) {
        }
    }

    private fun initListView(list: List<BluetoothDevice>) {
        Log.d("wcy", "init")
        if (list.size == 0) {
            return
        }
        if (mAdapter == null) {
            mAdapter = ListViewAdapter(this, list)
            deviceList!!.adapter = mAdapter
        } else {
            mAdapter!!.notifyDataSetChanged()
        }

        deviceList!!.onItemClickListener = this
    }


    private var isStartScan = false
    private val mlLeScanCallback: BluetoothAdapter.LeScanCallback = object : BluetoothAdapter.LeScanCallback {
        @SuppressLint("LongLogTag")
        override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
            //
            //Log.d(TAG,"device name =" +"evice.toString()");
            var descripter: String? = null
            if (scanRecord != null) {
                descripter = Utils.getHexString(scanRecord)
                Log.d(TAG, "descripter 开始连接 ==$descripter")

                if (scanRecord[0].toInt() == 0xFA && scanRecord[1].toInt() == 0xFB) {
                    //Log.d(TAG, "descripter 开始连接 ==" + descripter);
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
                                initListView(devices)
                            }
                            //devices.add(device);
                            Log.d(TAG, "device name =" + device.toString() + "," + device.uuids + "," + device.name + "," + device.bondState + "," + device.javaClass)
                        }
                    }
                }
            }
        } /* if (device != null && !devices.contains(device)) {
                if ((device.getName() != null) && device.getName().equals("SENSSUN CLOUD")){

                    if (scanRecord !=null) {
                        String descripter = Utils.bytesToHex(scanRecord,scanRecord.length);
                        Log.d(TAG,"descripter 开始连接 =="+descripter);
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
                    Log.d(TAG,"device name =" +device.toString()+","+device.getUuids()+","+device.getName()+","+device.getBondState()+","+device.getClass());
                }

            }

        }*/
    }

    fun startScan(bluetoothAdapter: BluetoothAdapter?, enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                isStartScan = false
                mBluetoothAdapter!!.stopLeScan(mlLeScanCallback)
                Log.d(TAG, "停止扫描")
            }, (60 * 1000).toLong())

            isStartScan = true
            Log.d(TAG, "开始扫描")
            mBluetoothAdapter!!.startLeScan(mlLeScanCallback)
        } else {
            isStartScan = false
            Log.d(TAG, "停止扫描")
            mBluetoothAdapter!!.stopLeScan(mlLeScanCallback)
        }
    }

    fun stopScan(bluetoothAdapter: BluetoothAdapter) {
        bluetoothAdapter.startLeScan(mlLeScanCallback)
    }


    override fun onServicesDiscovered(bluetoothGatt: BluetoothGatt?) {
//        userInfos.clear()
        displayGattServices(mBluetoothLeService!!.getSupportedGattServices())
    }


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
                Log.d(TAG, "service==" + service + ",chara==UUID==" + chara.uuid + ",de= " + chara.permissions + ",dede==" + chara.properties)
            }
        }

        Log.d(
            TAG, """
     get read ==${characteristic!!.uuid},,${String(data!!)}
     ${stringBuilder.toString()}
     """.trimIndent()
        )

        Log.d(TAG, "readvalue ===" + String(characteristic.value))
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        var stringBuilder: StringBuilder? = null
        val data = characteristic?.value
        if (data != null && data.size > 0) {
            stringBuilder = StringBuilder(data.size)
            for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
        }
        if (stringBuilder != null) {
            Log.d(TAG, "get write ==" + characteristic?.uuid + ",," + stringBuilder.toString())
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (characteristic?.uuid.toString() == BleDataManager.UUID_UNLOCK_DATA_NOTIFY) {
            if (characteristic?.value != null) {
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

    fun doneCharacteristicChangedData(gatt: BluetoothGatt?, character: BluetoothGattCharacteristic) {
        var optionCMMD: String? = null
        var dataOutHead: String? = null
        if (character.uuid.toString() == BleDataManager.UUID_UNLOCK_DATA_NOTIFY) {
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
                        if (!isRespond && isStartSetUser && count < 3) {
                            val jjj = byteArrayOf(0x00, 0x00)
                            writeUserInfoToScale(0x00.toByte(), jjj)
                        }
                    }

                    Utils.CMMD_SET_USER_RESPOND -> {
                        count = count + 1
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

                    else -> Log.d(TAG, "not same data")
                }
            }
        }
    }

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


    override fun onItemClick(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val device = parent.getItemAtPosition(position) as BluetoothDevice
        if (mBluetoothLeService != null) mBluetoothLeService!!.connect(device.address)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO request success
                Log.d(TAG, "wcy request success")
            }
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
            Log.d(TAG, "getService ====" + gattService.uuid + ",," + gattService.type)
            val gattCharacteristics = gattService.characteristics
            for (gattCharacteristic in gattCharacteristics) {
                Log.d(TAG, "gattCharacteristic uuid ==" + gattCharacteristic.uuid + ",," + gattCharacteristic.value + ",," + gattCharacteristic.properties)
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

    fun setCharacteristicType(bluetoothGattCharacteristic: BluetoothGattCharacteristic?) {
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

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }


    /**
     * option  0x00 /0x01 /0x02  新增／修改／删除///定义19个字节字符的数组
     */
    fun writeUserInfoToScale(option: Byte, userInfoByte: ByteArray?) {
        var temp: ByteArray
//        var userInfoO: UserInfo? = null
//        if (userInfos.size != 0) {
//            userInfoO = userInfos[1]
//        }

        //连接以后设置用户的信息
        if (baseWeight == null) {
            baseWeight = "0000"
        } else {
            Log.d(TAG, "baseWeight ==" + baseWeight.toString())
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
    fun delectedUserInfo(pinHex: ByteArray, optionTag: Byte) {
        val bytes = byteArrayOf(0x10, 0x00, 0x00, 0xc5.toByte(), 0x0b, 0x03, 0x02, 0x00, 0x00, 0x00, 0x00)
        if (pinHex.size < 2) {
            Log.d(TAG, "pin error defauled 0000")
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


    companion object {
        private const val TAG = "BluetoothMainActivity"
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 1

        private var count = 0
    }
}