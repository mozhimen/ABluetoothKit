package com.mozhimen.bluetoothk.ble2.test.test

import com.mozhimen.basick.impls.MutableLiveDataStrict
import com.mozhimen.kotlin.elemk.androidx.lifecycle.bases.BaseViewModel

/**
 * @ClassName ClientViewModel
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/12
 * @Version 1.0
 */
class ClientViewModel : BaseViewModel() {
    val liveData = MutableLiveDataStrict("")
}