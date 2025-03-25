package com.mozhimen.bluetoothk.basic.utils

import android.app.Activity
import android.content.Intent
import com.mozhimen.bluetoothk.basic.cons.CBluetoothKBasic
import com.mozhimen.kotlin.elemk.commons.I_Listener
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_COARSE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_ACCESS_FINE_LOCATION
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_ADVERTISE
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_CONNECT
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_BLUETOOTH_SCAN
import com.mozhimen.kotlin.utilk.android.content.UtilKIntentGet
import com.mozhimen.kotlin.utilk.android.content.UtilKPackageManagerWrapper
import com.mozhimen.kotlin.utilk.android.os.UtilKBuildVersion
import com.mozhimen.navigatek.start.utils.startForResult
import com.mozhimen.permissionk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.permissionk.xxpermissions.XXPermissionsRequestUtil

/**
 * @ClassName BluetoothKUtil
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/20
 * @Version 1.0
 */
object UtilBluetooth {
    @JvmStatic
    @OptIn(
        OPermission_BLUETOOTH_SCAN::class,
        OPermission_BLUETOOTH_CONNECT::class,
        OPermission_BLUETOOTH_ADVERTISE::class,
        OPermission_ACCESS_COARSE_LOCATION::class,
        OPermission_ACCESS_FINE_LOCATION::class
    )
    fun requestBluetoothPermission(activity: Activity, onGranted: I_Listener, onDenied: I_Listener? = null) {
        if (!UtilKPackageManagerWrapper.hasSystemFeature_BLUETOOTH(activity)) {
            onDenied?.invoke()
            return
        }
        if (UtilKBuildVersion.isAfterV_31_12_S()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter31(activity)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after31(activity, onGranted, onDenied = {
                    startForResult_ACTION_REQUEST_ENABLE(activity, onGranted, onDenied)
                })
            }
        } else if (UtilKBuildVersion.isAfterV_23_6_M()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter23(activity)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after23(activity, onGranted, onDenied = {
                    startForResult_ACTION_REQUEST_ENABLE(activity, onGranted, onDenied)
                })
            }
        } else {
            onGranted.invoke()
        }
    }

    @JvmStatic
    @OptIn(
        OPermission_BLUETOOTH_SCAN::class,
        OPermission_BLUETOOTH_CONNECT::class,
        OPermission_BLUETOOTH_ADVERTISE::class,
        OPermission_ACCESS_COARSE_LOCATION::class,
        OPermission_ACCESS_FINE_LOCATION::class
    )
    fun requestBluetoothBlePermission(activity: Activity, onGranted: I_Listener, onDenied: I_Listener? = null) {
        if (!UtilKPackageManagerWrapper.hasSystemFeature_BLUETOOTH_LE(activity)) {
            onDenied?.invoke()
            return
        }
        if (UtilKBuildVersion.isAfterV_31_12_S()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter31(activity)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after31(activity, onGranted, onDenied = {
                    startForResult_ACTION_REQUEST_ENABLE(activity, onGranted, onDenied)
                })
            }
        } else if (UtilKBuildVersion.isAfterV_23_6_M()) {
            if (XXPermissionsCheckUtil.hasPermission_BLUETOOTH_aftter23(activity)) {
                onGranted.invoke()
            } else {
                XXPermissionsRequestUtil.requestPermission_BLUETOOTH_after23(activity, onGranted, onDenied = {
                    startForResult_ACTION_REQUEST_ENABLE(activity, onGranted, onDenied)
                })
            }
        } else {
            onGranted.invoke()
        }
    }

    @JvmStatic
    fun startForResult_ACTION_REQUEST_ENABLE(activity: Activity, onGranted: I_Listener, onDenied: I_Listener?) {
        activity.startForResult(UtilKIntentGet.getBluetoothAdapter_ACTION_REQUEST_ENABLE(), CBluetoothKBasic.REQUEST_CODE_ENABLE) { a: Boolean, b: Intent? ->
            if (a) {
                onGranted.invoke()
            } else {
                onDenied?.invoke()
            }
        }
    }

    @JvmStatic
    fun startForResult_ACTION_REQUEST_DISCOVERABLE(activity: Activity, duration: Int, onGranted: I_Listener, onDenied: I_Listener?) {
        activity.startForResult(UtilKIntentGet.getBluetoothAdapter_ACTION_REQUEST_DISCOVERABLE(duration), CBluetoothKBasic.REQUEST_CODE_DISCOVERABLE) { a: Boolean, b: Intent? ->
            if (a) {
                onGranted.invoke()
            } else {
                onDenied?.invoke()
            }
        }
    }
}