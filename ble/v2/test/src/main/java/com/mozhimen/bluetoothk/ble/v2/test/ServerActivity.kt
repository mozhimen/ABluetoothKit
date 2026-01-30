package com.mozhimen.bluetoothk.ble.v2.test

//class ServerActivity : BaseActivityVDBVM<ActivityServerBinding, ServerViewModel>() {
//    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
//    private val _bluetoothKServerProxy: BluetoothKBle2ServerProxy by lazy { BluetoothKBleServerProxy() }
//
//    ///////////////////////////////////////////////////////////////////////////////////
//
//    @OptIn(OApiInit_ByLazy::class, OApiCall_BindLifecycle::class, OApiCall_BindViewLifecycle::class)
//    override fun initView(savedInstanceState: Bundle?) {
//        _bluetoothKServerProxy.apply {
//            bindLifecycle(this@ServerActivity)
//            setListener {
//                vm.liveData.postValue(it)
//            }
//            start(this@ServerActivity)
//        }
//        vdb.serverBtnWrite.setOnClickListener {
//            val data = vdb.serverEditData.text.trim().toString()
//            if (data.isNotEmpty()) {
//                UtilKLogWrapper.d(TAG, "initView: data $data")
//                _bluetoothKServerProxy.write(data)
//            }
//        }
//    }
//
//    override fun bindViewVM(vdb: ActivityServerBinding) {
//        vdb.vm = vm
//    }
//}