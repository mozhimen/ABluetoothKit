package com.mozhimen.bluetoothk.utils

import android.content.Context
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_COARSE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_FINE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_ADVERTISE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_CONNECT
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_SCAN
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.permissionk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsNavHostUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsRequestUtil

/**
 * @ClassName BluetoothKUtil
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/20
 * @Version 1.0
 */
object BluetoothKUtil {
    @JvmStatic
    @OptIn(OPermission_BLUETOOTH_SCAN::class, OPermission_BLUETOOTH_CONNECT::class, OPermission_BLUETOOTH_ADVERTISE::class, OPermission_ACCESS_COARSE_LOCATION::class, OPermission_ACCESS_FINE_LOCATION::class)
    fun requestBluetoothPermission(context: Context, onGranted: I_Listener) {
        if (UtilKBuildVersion.isAfterV_31_12_S()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter31(context)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after31(context, {
                    onGranted.invoke()
                }, {
                    XXPermissionsNavHostUtil.startPermission_BLUETOOTH(context)
                })
            }
        } else if (UtilKBuildVersion.isAfterV_23_6_M()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter23(context)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after23(context, {
                    onGranted.invoke()
                }, {
                    XXPermissionsNavHostUtil.startPermission_BLUETOOTH(context)
                })
            }
        } else {
            onGranted.invoke()
        }
    }
}