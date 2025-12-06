package com.example.demo03.tree

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo03.R

/**
 * @author: Luuuzi
 * @Date: 2021-01-29
 * @description: 带帮助信息菜单
 */
class TreeAdapter(var mContext: Context) :
    RecyclerView.Adapter<TreeAdapter.ViewHolder>() {

    var mLastSelectedPosition: Int = -1
    private var isSplitEnable: Boolean = false //显示2列还是1列
    private val data = mutableListOf<Component>()
    private var searchKeywords: String? = null
    fun addDataList(list: List<Component>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    fun setSplitEnable(splitEnable: Boolean) {
        isSplitEnable = splitEnable
    }

    //添加关键字
    fun putSearchkeywords(keywords: String?) {
        searchKeywords = keywords
    }

    public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item, null)
        return ViewHolder(view)
    }

    public override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val component = data[position]
        holder.name.text = component.name
        holder.line.visibility = if (component.isRootNode) View.VISIBLE else View.GONE
        if (position == data.size - 1) {// 节点的最后一个
            holder.line.setImageResource(R.drawable.line)
        } else {
            holder.line.setImageResource(R.drawable.line2)
        }
        val isChild = component is Composite
        holder.ivExpand.visibility = if (isChild) View.VISIBLE else View.INVISIBLE

        if (isChild) {//子recycleveiw
            holder.rlvChild.layoutManager = LinearLayoutManager(mContext)
            val childAdapter = TreeAdapter(mContext)
            holder.rlvChild.adapter = childAdapter
            childAdapter.addDataList((component as Composite).children)
            holder.ivExpand.setImageResource(if (holder.rlvChild.isVisible) R.drawable.down_arrow_black else R.drawable.right_arrow_black)
        } else {

        }

        holder.ivExpand.setOnClickListener {
            holder.rlvChild.visibility = if (holder.rlvChild.isVisible) View.GONE else View.VISIBLE
            holder.ivExpand.setImageResource(if (holder.rlvChild.isVisible) R.drawable.down_arrow_black else R.drawable.right_arrow_black)

        }
    }

    public override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var line: ImageView
        var ivExpand: ImageView
        var name: TextView

        var rlvChild: RecyclerView


        init {
            line = itemView.findViewById(R.id.line)
            ivExpand = itemView.findViewById(R.id.ivExpand)
            name = itemView.findViewById(R.id.tvName)
            rlvChild = itemView.findViewById(R.id.rlvChild)
        }
    }


}