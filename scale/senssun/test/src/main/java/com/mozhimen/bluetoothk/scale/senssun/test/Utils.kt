package com.mozhimen.bluetoothk.scale.senssun.test

import android.util.Log
import com.mozhimen.kotlin.utilk.commons.IUtilK
import java.util.Random

/**
 * @ClassName Utils
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
/**
 * Created by wucaiyan on 17-11-9.
 */
object Utils : IUtilK {
    private const val DEBUG = false

    //厂商的ID：占两个字节 协议ID：0x01 产品ModeID：0x0115
    const val COUNT_MANUFACTURERS_DATA: Int = 11

    val hexArray: CharArray = "0123456789ABCDEF".toCharArray()

    val CMMD_QUERY_USER: ByteArray = byteArrayOf(0x10, 0x00, 0x00, 0xC5.toByte(), 0x08, 0x03, 0x07, 0x12)
    const val CMMD_SET_USER_REQUEST: String = "0301"
    const val CMMD_USERINFOS_RESPOND: String = "0387"
    const val CMMD_SET_USER_RESPOND: String = "0381"
    const val CMMD_GET_TEMP_DATA: String = "0380"
    const val CMMD_GET_RESULT_DATA: String = "0382"

    fun bytesToHex(bytes: ByteArray, count: Int): String {
        var bytes = bytes
        if (bytes.size > count) {
            bytes = subBytes(bytes, 0, count)
        }
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * 将字节转换成16进制字符串
     */
    fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.size <= 0) {
            return null
        }
        for (i in src.indices) {
            if ((src[i].toString() + "").length > 3) {
                val hv = (src[i].toString() + "").substring(2)

                if (hv.length < 2) {
                    stringBuilder.append(0)
                }
                stringBuilder.append(hv)
            }
        }
        return stringBuilder.toString()
    }

    fun getHexString(data: ByteArray?): String {
        var stringBuffer: StringBuffer? = null
        if (data != null && data.size > 0) {
            stringBuffer = StringBuffer(data.size)
            for (byteChar in data) {
                stringBuffer.append(String.format("%02X", byteChar))
            }
        }
        return stringBuffer.toString()
    }

    //截取字节数组
    fun subBytes(src: ByteArray?, begin: Int, count: Int): ByteArray {
        val bs = ByteArray(count)
        System.arraycopy(src, begin, bs, 0, count)
        return bs
    }

    /**
     * 从十六进制字符串到字节数组转换
     * hexstr 0x00
     */
    fun hexString2Bytes(hexstr: String): ByteArray {
        val length = hexstr.length
        if (length % 2 != 0) {
            throw RuntimeException("Hex  bit string length must be even")
        }
        val b = ByteArray(hexstr.length / 2)
        var j = 0
        for (i in b.indices) {
            val c0 = hexstr[j++]
            val c1 = hexstr[j++]
            Log.d(TAG, "getchar c0==$c0,c1==$c1")
            b[i] = ((parse(c0) shl 4) or parse(c1)).toByte()
            if (DEBUG) {
                Log.d(TAG, "b[+" + i + "]" + "parsec0=" + (parse(c0) shl 4) + "cparsec1=" + parse(c1) + "," + b[i])
            }
        }
        return b
    }

    private fun parse(c: Char): Int {
        if (c >= 'a') return (c.code - 'a'.code + 10) and 0x0f
        if (c >= 'A') return (c.code - 'A'.code + 10) and 0x0f
        return (c.code - '0'.code) and 0x0f
    }

    /**
     * 构造验证码
     */
    fun constructionCheckCode(data: String?): ByteArray {
        if (data == null || data == "") {
            return hexString2Bytes(0.toString() + "")
        }
        var len = data.length
        var num = 8
        if (len < 8) {
            throw RuntimeException("Character length must be greater than 8")
        }
        Log.d(TAG, "constructionCheckCode==len==$len")
        var total = 0
        while (num < len) {
            total += data.substring(num, num + 2).toInt(16)
            if (true) {
                Log.d(TAG, "data.substring(num,num+2)= " + data.substring(num, num + 2) + "Integer=" + data.substring(num, num + 2).toInt(16))
            }
            num = num + 2
        }
        var hex = Integer.toHexString(total)
        len = hex.length
        // 如果不够校验位的长度，补0,这里用的是两位校验
        hex = if (len < 1) {
            "00"
        } else if (len < 2) {
            "0$hex"
        } else {
            hex.substring(len - 2)
        }
        Log.d(TAG, "constructionCheckCode result ==" + hexString2Bytes(hex))
        return hexString2Bytes(hex)
    }

    fun getRandomNum(count: Int): ByteArray {
        val sb = StringBuffer()
        var str = "0123456789"
        val r = Random()
        for (i in 0 until count) {
            val num = r.nextInt(str.length)
            sb.append(str[num])
            str = str.replace((str[num].toString() + ""), "")
        }
        return hexString2Bytes(sb.toString())
    }
}