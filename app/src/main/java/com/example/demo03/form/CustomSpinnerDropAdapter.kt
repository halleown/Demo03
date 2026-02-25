package com.example.demo03.form

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.example.demo03.R

/**
 * 下拉框的数据适配器
 */
open class CustomSpinnerDropAdapter(
    private val mContext: Context,
    private var data: List<String>? = null
) : BaseAdapter() {

    private var cachedHeight: Int = -1

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
        notifyDataSetChanged()
    }

    /**
     * 测量单个 Item 的高度
     */
    fun measureItemHeight(parent: ViewGroup): Int {
        if (cachedHeight > 0) return cachedHeight

        val view = LayoutInflater.from(mContext).inflate(getLayoutId(), parent, false)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)

        cachedHeight = view.measuredHeight
        return cachedHeight
    }

    open val singleRowColor = Color.parseColor("#e4e5e7")
    open val doubleRowColor = Color.parseColor("#eeeeee")

    @LayoutRes
    open fun getLayoutId(): Int = R.layout.item_spinner_drop

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(getLayoutId(), parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }
        holder.tvName.text = data?.get(position)
        holder.rlItem.setBackgroundColor(if (position % 2 == 0) singleRowColor else doubleRowColor)

        return view
    }

    private class ViewHolder(root: View) {
        val tvName: TextView = root.findViewById(R.id.tv_name)
        val rlItem: View = root.findViewById(R.id.rl_item)
    }
}