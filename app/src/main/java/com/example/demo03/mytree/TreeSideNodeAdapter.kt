package com.example.demo03.mytree

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.demo03.R

class TreeSideNodeAdapter(
    var datas: MutableList<TreeSideItems>,
    var mContext: Context,
    // var customHorizontalScrollView: CustomHorizontalScrollView,
    var rlv_side_menu: RecyclerView
) :
    RecyclerView.Adapter<TreeSideNodeAdapter.TestDemoHolder>() {
    // var globalList: ViewTreeObserver.OnGlobalLayoutListener? = null

    //    var treeSideNodeCheck = false // 当节点不存在子级节点时,是否带复选框 false表示不带复选框单选模式,true表示带多选框多选模式
    var listener: Listener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestDemoHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.item_tree_side, null)
        return TestDemoHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: TestDemoHolder, position: Int) {
        val itemData = datas[position]

        holder.tvName.text = itemData.Name
        if (itemData.Weak.isEmpty()) {
            holder.tvWeak.visibility = View.GONE
        } else {
            holder.tvWeak.text = itemData.Weak
            holder.tvWeak.visibility = View.VISIBLE
        }
        val layoutParams = holder.llText.layoutParams
        layoutParams.width = (getScreenWidth(mContext) * 0.4).toInt() - dpToPx(24)
        holder.llText.layoutParams = layoutParams
        // 是否带复选框
        holder.ivExpand.visibility = if (itemData.ShowExpand) View.VISIBLE else View.INVISIBLE
        // if (itemData.ShowExpand) {
        if (itemData.Expand) {
            holder.ivExpand.setImageResource(R.drawable.down_arrow_black)
        } else {
            holder.ivExpand.setImageResource(R.drawable.right_arrow_black)
        }

        // 根节点不显示线

        if (position == datas.size - 1) {// 最后一个节点
            holder.ivLine.setImageResource(R.drawable.line)
        } else {
            holder.ivLine.setImageResource(R.drawable.line2)
        }


        // 展开折叠
        holder.ivExpand.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                // if (BaseShDisplay.isFastClick) {
                //     return
                // }

                // 保存滚动状态
                val position = (rlv_side_menu.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val offset = rlv_side_menu.getChildAt(0).top

                // 保存当前滚动位置
                // val scrollX = customHorizontalScrollView.scrollX
                // globalList = ViewTreeObserver.OnGlobalLayoutListener { customHorizontalScrollView.scrollTo(scrollX, 0) }
                // customHorizontalScrollView.viewTreeObserver.addOnGlobalLayoutListener(globalList)

                // 禁用滑动
                holder.rlvChild.isNestedScrollingEnabled = false
                itemData.Expand = !itemData.Expand
                if (itemData.Expand) {
                    holder.ivExpand.setImageResource(R.drawable.down_arrow_black)
                } else {
                    holder.ivExpand.setImageResource(R.drawable.right_arrow_black)
                }
                holder.rlvChild.visibility = if (itemData.Expand) View.VISIBLE else View.GONE

                // 禁用默认动画
                (holder.rlvChild.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
                // 还原滚动位置
                holder.itemView.postDelayed({
                    // 还原滚动位置
                    // customHorizontalScrollView.scrollTo(scrollX, 0)
                    // if (globalList != null) {
                    //     customHorizontalScrollView.viewTreeObserver.removeOnGlobalLayoutListener(globalList)
                    // }
                }, 400)
                // 恢复滚动状态
                (rlv_side_menu.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
                // 禁用嵌套滚动
                listener?.clickSendStd(itemData, -12, position)
            }
        })
        // }
        if (itemData.Enable) {
            holder.llText.isSelected = itemData.Sel
            holder.tvName.isSelected = itemData.Sel
            holder.tvWeak.isSelected = itemData.Sel
        } else {
            holder.llText.isSelected = false
            holder.tvName.isSelected = false
            holder.tvWeak.isSelected = false
        }


        if (itemData.childItems.isNullOrEmpty()) {// 没有子节点
            holder.rlvChild.visibility = View.GONE
            if (!itemData.Enable) {
//                holder.tvName.setTextColor(mContext.resources.getColor(R.color.deep_gray))
            }
            holder.llText.setOnClickListener(object : OnClickListener {
                override fun onClick(p0: View?) {
                    // if (BaseShDisplay.isFastClick) {
                    //     return
                    // }
                    if (itemData.Enable) {
                        if (!itemData.Sel) {
                            itemData.Sel = true
                            holder.llText.isSelected = itemData.Sel
                            // 清空选中项
                            listener?.clearAllSelected2(itemData)
                        }
                    }
                }
            })
        } else {
            // 有子节点
            val childAdapter = TreeSideNodeAdapter(itemData.childItems!!, mContext, rlv_side_menu)
            childAdapter.listener = listener
            holder.rlvChild.layoutManager = LinearLayoutManager(mContext)
            holder.rlvChild.adapter = childAdapter
//            holder.ivExpand.visibility = View.VISIBLE
            if (itemData.Enable) {
                holder.tvName.isSelected = itemData.Sel
            } else {
                holder.tvName.isSelected = false
            }


            holder.llText.setOnClickListener(object : OnClickListener {
                override fun onClick(p0: View?) {
                    // if (BaseShDisplay.isFastClick) {
                    //     return
                    // }
                    if (itemData.Enable) {
                        if (!itemData.Sel) {
                            itemData.Sel = true
                            holder.llText.isSelected = itemData.Sel
                            // 清空选中项
                            listener?.clearAllSelected(itemData, position)
                        }
                    }
                }
            })
            holder.rlvChild.visibility = if (itemData.Expand) View.VISIBLE else View.GONE
        }
    }


    class TestDemoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivExpand: ImageView
        var ivLine: ImageView

        //        var ivPlacehold: ImageView
        var llText: LinearLayout
        var tvName: TextView
        var tvWeak: TextView
        var rlvChild: RecyclerView

        init {
            ivExpand = itemView.findViewById(R.id.ivExpand)
//            ivPlacehold = itemView.findViewById(R.id.ivPlacehold)
            llText = itemView.findViewById(R.id.llText)
            tvName = itemView.findViewById(R.id.tvName)
            tvWeak = itemView.findViewById(R.id.tvWeak)
            rlvChild = itemView.findViewById(R.id.rlvChild)
            ivLine = itemView.findViewById(R.id.iv_line)

        }
    }

    abstract class Listener {
        // 不带复选框,有子节点点击回调
        abstract fun clearAllSelected(selectSideitem: TreeSideItems, position: Int)

        // 不带复选框 没有子节点点击回调
        abstract fun clearAllSelected2(selectSideitem: TreeSideItems)

        // 展开收起回调
        abstract fun clickSendStd(sideitem: TreeSideItems, key: Int, position: Int)

        // 带复选框没有子节点的点击回调
        abstract fun clickCheckItemClick(sideitem: TreeSideItems)

        // 带复选框有子节点的点击回调
        abstract fun clickCheckItemsClick(sideitem: TreeSideItems)


    }





    fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun dpToPx(dpValue: Int): Int {
        var DENSITY = Resources.getSystem()
            .getDisplayMetrics().density
        return (dpValue * DENSITY + 0.5f).toInt()
    }
}