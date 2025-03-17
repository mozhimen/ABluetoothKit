package com.mozhimen.bluetoothk.ble.mos

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcel
import android.os.Parcelable
import com.mozhimen.kotlin.utilk.android.os.applyReadParcelable
import java.io.Serializable

/**
 * @ClassName BluetoothDeviceBle
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/14
 * @Version 1.0
 */
data class BluetoothDeviceBle(
    val bluetoothDevice: BluetoothDevice?,
    val scanResult: ScanResult?,
) : Parcelable, Serializable {

    constructor(parcel: Parcel) : this(
        parcel.applyReadParcelable<BluetoothDevice>(),
        parcel.applyReadParcelable<ScanResult>()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(bluetoothDevice, flags)
        parcel.writeParcelable(scanResult, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BluetoothDeviceBle> {
        override fun createFromParcel(parcel: Parcel): BluetoothDeviceBle {
            return BluetoothDeviceBle(parcel)
        }

        override fun newArray(size: Int): Array<BluetoothDeviceBle?> {
            return arrayOfNulls(size)
        }
    }
}
