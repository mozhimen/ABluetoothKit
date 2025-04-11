package com.mozhimen.bluetoothk.ble.androidx.test

import com.mozhimen.basick.impls.MutableLiveDataStrict
import com.mozhimen.kotlin.elemk.androidx.lifecycle.bases.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @ClassName ClientViewModel
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/12
 * @Version 1.0
 */
class ClientViewModel : BaseViewModel() {
    val liveData = MutableLiveDataStrict<String>()
    val flowSubscribeData = MutableStateFlow<ByteArray>(ByteArray(0))
}