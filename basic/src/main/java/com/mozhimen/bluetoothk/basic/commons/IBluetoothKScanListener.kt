package com.mozhimen.bluetoothk.basic.commons


/**
 * @ClassName IBluetoothKEventListener
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/2/18
 * @Version 1.0
 */
interface IBluetoothKScanListener<A,B,C> {
    fun onStart(){}
    /**
     * 连接成功或失败后调用
     * @param socket 获得的socket
     * @param device 本次连接的设备，可存下来方便下次自动重连，就不用每次都选择了。
     * @param e 错误
     */
    fun onFound(obj: A){}
    fun onBonding(obj: B){}
    fun onBonded(obj: C){}

    fun onStop(){}
}