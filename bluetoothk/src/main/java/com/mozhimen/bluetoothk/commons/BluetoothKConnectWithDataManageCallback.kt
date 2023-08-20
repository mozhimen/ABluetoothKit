package com.mozhimen.bluetoothk.commons


/**
 * @ClassName BluetoothKConnectWithDataManageCallback
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/17 11:19
 * @Version 1.0
 */

abstract class BluetoothKConnectWithDataManageCallback : BluetoothKConnectCallback() {
    //    /**
    //     * 处理读取到的数据的函数，后台会有一个循环读取，若没有错误产生且有数据，则每次读取会有一个buffer，以及buffer的大小bytes。
    //     * @param bytes 本次读取到的字节数
    //     * @param buffer 字节存到这个数组中
    //     * @param e 错误
    //     */
    //    public abstract void dataMange(int bytes, byte[] buffer, Exception e);
    //
    //    public void internalDataMange(final int bytes, final byte[] buffer, final Exception e) {
    //        this.dataMange(bytes, buffer, e);
    //    }
    abstract fun getMac(BtName: String?, mac: String?)
}
