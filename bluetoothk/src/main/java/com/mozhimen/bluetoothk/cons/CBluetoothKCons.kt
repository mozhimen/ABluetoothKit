package com.mozhimen.bluetoothk.cons


/**
 * @ClassName CBluetoothKCons
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/17 10:50
 * @Version 1.0
 */
object CBluetoothKCons {
    const val INTENT_ACTION_BLUETOOTH_ADAPTER_CANCEL_DISCOVERY = "cc.liyongzhi.action.BLUETOOTH_ADAPTER_CANCEL_DISCOVERY"
    const val INTENT_BLUETOOTH_DISCONNECTED = "cc.liyongzhi.action.BLUETOOTH_DISCONNECTED"
    const val INTENT_BLUETOOTH_CONNECTED = "cc.liyongzhi.action.BLUETOOTH_CONNECTED"
    const val EXTRA_BLUETOOTH_MAC = "mac"
    const val EXTRA_CALLBACK_KEY = "callback_key"
    const val EXTRA_MAC_ADDRESS = "bluetooth_mac_address"
    const val REQUEST_CODE_OPEN_BT = 0x01
}