package com.example.demo03.spinner

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.demo03.R

class PopFormAdapter(
    private val mContext: Context,
    private var data: MutableList<String>?
) :
    BaseAdapter() {
    override fun getCount(): Int {
        return data?.size ?: 0
    }

    override fun getItem(position: Int): Any? {
        return data?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setData(data: MutableList<String>?) {
        this.data = data
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            convertView =
                LayoutInflater.from(mContext).inflate(R.layout.item_pop_input, parent, false)
            holder.tvName = convertView.findViewById(R.id.tv_name)
            holder.rlItem = convertView.findViewById(R.id.ll_settings_laguage_item)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.tvName!!.text = data?.get(position)
        if (position % 2 == 0) {
            holder.rlItem!!.setBackgroundColor(Color.parseColor("#e4e5e7"))
        } else {
            holder.rlItem!!.setBackgroundColor(Color.parseColor("#eeeeee"))
        }
        return convertView
    }

    internal class ViewHolder {
        var tvName: TextView? = null
        var rlItem: RelativeLayout? = null
    }
}