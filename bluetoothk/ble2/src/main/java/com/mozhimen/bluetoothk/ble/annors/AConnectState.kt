package com.mozhimen.bluetoothk.ble.annors

import androidx.annotation.IntDef

/**
 * @ClassName AConnectState
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
@IntDef(
    AConnectState.STATE_DISCONNECTED,
    AConnectState.STATE_CONNECTING,
    AConnectState.STATE_CONNECTED
)
annotation class AConnectState {
    companion object {
        // 蓝牙连接已断开
        const val STATE_DISCONNECTED = 0
        // 蓝牙正在连接
        const val STATE_CONNECTING = 1
        // 蓝牙已连接
        const val STATE_CONNECTED = 2
        // 蓝牙连接失败
        const val STATE_CONNECT_FAIL = 3
    }
}
