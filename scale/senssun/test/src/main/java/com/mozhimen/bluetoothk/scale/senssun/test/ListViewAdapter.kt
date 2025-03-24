package com.mozhimen.bluetoothk.scale.senssun.test

import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * @ClassName ListViewAdapter
 * @Description TODO
 * @Author mozhimen
 * @Date 2025/3/21
 * @Version 1.0
 */
/**
 * Created by wucaiyan on 17-11-7.
 */
class ListViewAdapter(private val mContext: Context, private val mlist: List<BluetoothDevice>) : BaseAdapter() {
    override fun getCount(): Int {
        return mlist.size
    }

    override fun getItem(position: Int): Any {
        return mlist[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.simple_expandable_list_item_1, null, false)
            viewHolder = ViewHolder()
            viewHolder.mTextView = convertView.findViewById<View>(R.id.text1) as TextView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        val device = mlist[position]
        viewHolder.mTextView!!.text = """
            ${device.name}
            ${device.address}
            """.trimIndent()
        return convertView
    }

    internal inner class ViewHolder {
        var mTextView: TextView? = null
    }
}