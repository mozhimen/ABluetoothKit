package com.mozhimen.bluetoothk.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.mozhimen.bluetoothk.cons.CBluetoothK
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_COARSE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_FINE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_ADVERTISE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_CONNECT
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_SCAN
import com.mozhimen.kotlin.utilk.android.content.UtilKIntentGet
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.navigatek.start.utils.startForResult
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
    @OptIn(
        OPermission_BLUETOOTH_SCAN::class,
        OPermission_BLUETOOTH_CONNECT::class,
        OPermission_BLUETOOTH_ADVERTISE::class,
        OPermission_ACCESS_COARSE_LOCATION::class,
        OPermission_ACCESS_FINE_LOCATION::class
    )
    fun requestBluetoothPermission(activity: Activity, onGranted: I_Listener) {
        if (UtilKBuildVersion.isAfterV_31_12_S()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter31(activity.applicationContext)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after31(activity.applicationContext, {
                    onGranted.invoke()
                }, {
                    activity.startForResult(UtilKIntentGet.getBluetoothAdapter_ACTION_REQUEST_ENABLE(), CBluetoothK.REQUEST_CODE_OPEN_BT) { a: Boolean, b: Intent? ->
                        if (a) {
                            onGranted.invoke()
                        }
                    }
                })
            }
        } else if (UtilKBuildVersion.isAfterV_23_6_M()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter23(activity.applicationContext)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after23(activity.applicationContext, {
                    onGranted.invoke()
                }, {
                    activity.startForResult(UtilKIntentGet.getBluetoothAdapter_ACTION_REQUEST_ENABLE(), CBluetoothK.REQUEST_CODE_OPEN_BT) { a: Boolean, b: Intent? ->
                        if (a) {
                            onGranted.invoke()
                        }
                    }
                })
            }
        } else {
            onGranted.invoke()
        }
    }
}