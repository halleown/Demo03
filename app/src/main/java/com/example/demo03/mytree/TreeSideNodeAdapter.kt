package com.example.demo03.mytree

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.demo03.R

private const val PAYLOAD_SELECTION = "payload_selection"


class TreeSideNodeAdapter(
    var datas: MutableList<TreeSideItems>,
    var mContext: Context,
    var customHorizontalScrollView: CustomHorizontalScrollView,
    var rlv_side_menu: RecyclerView,
    private var selectedNodeId: Long = 1L,
    private val onSelectedNodeChange: (Long) -> Unit = {}
) : RecyclerView.Adapter<TreeSideNodeAdapter.TestDemoHolder>() {

    // 子适配器引用，递归同步选中
    private val childAdapters = mutableMapOf<Int, TreeSideNodeAdapter>()

    private val TAG = "xialj"

    var globalList: ViewTreeObserver.OnGlobalLayoutListener? = null

    //    var treeSideNodeCheck = false // 当节点不存在子级节点时,是否带复选框 false表示不带复选框单选模式,true表示带多选框多选模式
    var listener: Listener? = null

    // var isShowing: Boolean = false

    /**
     * 记录当前这一列（同一级别）的虚线 phase，使 view_t / view_l / view_i 看起来像一条连续的虚线
     */
    private var currentColumnPhase: Float = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestDemoHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.item_tree_side, parent, false)
        return TestDemoHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    /** 只刷新旧/新两个节点，并向子适配器同步 */
    fun updateSelection(newId: Long) {
        if (newId == selectedNodeId) return
        val oldId = selectedNodeId
        selectedNodeId = newId

        val oldPos = datas.indexOfFirst { it.Node == oldId }
        val newPos = datas.indexOfFirst { it.Node == newId }
        if (oldPos >= 0) notifyItemChanged(oldPos, PAYLOAD_SELECTION)
        if (newPos >= 0) notifyItemChanged(newPos, PAYLOAD_SELECTION)

        // 递归同步
        childAdapters.values.forEach { it.updateSelection(newId) }
    }

    override fun onBindViewHolder(holder: TestDemoHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_SELECTION)) {
            val itemData = datas[position]
            val isSelected = itemData.Node == selectedNodeId && itemData.Enable
            holder.llText.isSelected = isSelected
            holder.tvName.isSelected = isSelected
            holder.tvWeak.isSelected = isSelected
            // Log.d(TAG, "onBindViewHolder: 上一个节点：${itemData.Node}--------${selectedNodeId}")
            return
        }
        super.onBindViewHolder(holder, position, payloads)
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
        // holder.ivExpand.visibility = if (itemData.ShowExpand) View.VISIBLE else View.GONE
        holder.ivExpand.visibility = if (itemData.childItems?.isNotEmpty() == true) View.VISIBLE else View.INVISIBLE
        holder.ivExpand.setImageResource(if (itemData.Expand) R.drawable.down_arrow_black else R.drawable.right_arrow_black)

        // if (isShow) holder.view_i.visibility = View.VISIBLE else holder.view_i.visibility = View.GONE


        // 根节点不显示线
        if (!getLevel(itemData.Node)) {
            if (position == datas.size - 1) {// 最后一个节点
                holder.view_l.visibility = View.VISIBLE
                holder.view_t.visibility = View.GONE
                holder.view_i.visibility = View.GONE
            } else {// 中间节点
                holder.view_l.visibility = View.GONE
                holder.view_t.visibility = View.VISIBLE
                holder.view_i.visibility = View.VISIBLE
                // isShowing = position < datas.size - 1
            }
        } else {
            holder.view_l.visibility = View.GONE
            holder.view_t.visibility = View.GONE
            holder.view_i.visibility = View.GONE
        }


        // ================== 虚线 phase 串联处理 ==================
        // 思路：同一级别的三种线（T / L / I）共享一个 phase，使相邻 item 的虚线可以“接上”
        // 使用 addOnLayoutChangeListener 确保在当前 item 完成布局后再更新 phase，
        // 这样下一个 item 在 onBind 时拿到的就是已经更新过的 phase。
        if (holder.view_t.visibility == View.VISIBLE) {
            holder.view_t.setDashPhase(currentColumnPhase)
            Log.d("xialj", "onBindViewHolder: ---${itemData.Name}---T_设置当前偏移----${currentColumnPhase}---")
            holder.view_t.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    holder.view_t.removeOnLayoutChangeListener(this)
                    // currentColumnPhase = holder.view_t.getNextViewPhase()
                    currentColumnPhase = holder.view_t.getLastDashGapLength()
                    Log.d("xialj", "onBindViewHolder: ---${itemData.Name}--T_获取下一次偏移----${currentColumnPhase}---")
                }
            })
        }

        if (holder.view_i.visibility == View.VISIBLE) {
            holder.view_i.setDashPhase(currentColumnPhase)
            Log.d("xialj", "onBindViewHolder: ---${itemData.Name}--I_设置当前偏移----${currentColumnPhase}---")
            holder.view_i.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    holder.view_i.removeOnLayoutChangeListener(this)
                    // currentColumnPhase = holder.view_i.getNextViewPhase()
                    currentColumnPhase = holder.view_i.getLastDashGapLength()
                    Log.d("xialj", "onBindViewHolder: ---${itemData.Name}--I_获取下一次偏移----${currentColumnPhase}---")
                }
            })
        }


        if (holder.view_l.visibility == View.VISIBLE) {
            holder.view_l.setDashPhase(currentColumnPhase)
            Log.d("xialj", "onBindViewHolder: --${itemData.Name}---L_设置当前偏移----${currentColumnPhase}---")
            holder.view_l.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    holder.view_l.removeOnLayoutChangeListener(this)
                    // currentColumnPhase = holder.view_l.getNextViewPhase()
                    // currentColumnPhase = holder.view_l.getLastDashGapLength()
                    Log.d("xialj", "onBindViewHolder: --${itemData.Name}---L_获取下一次偏移----${currentColumnPhase}---")
                }
            })
        }

        // 选中态
        val isSelected = itemData.Node == selectedNodeId && itemData.Enable
        holder.llText.isSelected = isSelected
        holder.tvName.isSelected = isSelected
        holder.tvWeak.isSelected = isSelected

        // 展开折叠
        holder.ivExpand.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                val first = (rlv_side_menu.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val offset = rlv_side_menu.getChildAt(0)?.top ?: 0
                val scrollX = customHorizontalScrollView.scrollX
                globalList = ViewTreeObserver.OnGlobalLayoutListener { customHorizontalScrollView.scrollTo(scrollX, 0) }
                customHorizontalScrollView.viewTreeObserver.addOnGlobalLayoutListener(globalList)

                holder.rlvChild.isNestedScrollingEnabled = false
                itemData.Expand = !itemData.Expand
                holder.ivExpand.setImageResource(if (itemData.Expand) R.drawable.down_arrow_black else R.drawable.right_arrow_black)
                holder.rlvChild.visibility = if (itemData.Expand) View.VISIBLE else View.GONE
                (holder.rlvChild.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

                holder.itemView.postDelayed({
                    customHorizontalScrollView.scrollTo(scrollX, 0)
                    if (globalList != null) customHorizontalScrollView.viewTreeObserver.removeOnGlobalLayoutListener(globalList)
                }, 400)
                (rlv_side_menu.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(first, offset)
                listener?.clickSendStd(itemData, -12, position)
            }
        })


        if (itemData.childItems.isNullOrEmpty()) {
            holder.rlvChild.visibility = View.GONE
            childAdapters.remove(position)
            holder.llText.setOnClickListener(object : OnClickListener {
                override fun onClick(p0: View?) {
//                    if (itemData.Enable && itemData.Node != selectedNodeId) {
//                        onSelectedNodeChange(itemData.Node)
//                        updateSelection(itemData.Node)
//                        listener?.clearAllSelected2(itemData)
//                    }
                    val isSelected = itemData.Node == selectedNodeId && itemData.Enable
                    // 已选中或者未启用，直接返回
                    if (isSelected || !itemData.Enable) return

//                    // 同步数据 Sel
//                    datas.find { it.Node == selectedNodeId }?.Sel = false
//                    itemData.Sel = true

                    Log.d(TAG, "onClick: 无子节点：点击了“${itemData.Name}")
                    // 通知外层
                    onSelectedNodeChange(itemData.Node)
                    // 局部刷新新旧item
                    updateSelection(itemData.Node)
                    listener?.clearAllSelected2(itemData)
                }
            })
        } else {
            val childAdapter = TreeSideNodeAdapter(
                itemData.childItems!!,
                mContext,
                customHorizontalScrollView,
                rlv_side_menu,
                selectedNodeId,
                onSelectedNodeChange
            )
            childAdapter.listener = listener
            holder.rlvChild.layoutManager = LinearLayoutManager(mContext)
            holder.rlvChild.adapter = childAdapter
            childAdapters[position] = childAdapter

            holder.llText.setOnClickListener(object : OnClickListener {
                override fun onClick(p0: View?) {
//                    if (itemData.Enable && itemData.Node != selectedNodeId) {
//                        onSelectedNodeChange(itemData.Node)
//                        updateSelection(itemData.Node)
//                        listener?.clearAllSelected(itemData, position)
//                    }
                    val isSelected = itemData.Node == selectedNodeId && itemData.Enable
                    // 已选中或者未启用，直接返回
                    if (isSelected || !itemData.Enable) return

//                    // 同步数据 Sel
//                    datas.find { it.Node == selectedNodeId }?.Sel = false
//                    itemData.Sel = true

                    Log.d(TAG, "onClick: 有子节点：点击了“${itemData.Name}")
                    onSelectedNodeChange(itemData.Node)
                    updateSelection(itemData.Node)
                    listener?.clearAllSelected2(itemData)
                }
            })
            holder.rlvChild.visibility = if (itemData.Expand) View.VISIBLE else View.GONE
        }
    }


    class TestDemoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivExpand: ImageView = itemView.findViewById(R.id.ivExpand)
        var llText: LinearLayout = itemView.findViewById(R.id.llText)
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvWeak: TextView = itemView.findViewById(R.id.tvWeak)
        var rlvChild: RecyclerView = itemView.findViewById(R.id.rlvChild)
        var view_t: TShapeView = itemView.findViewById(R.id.view_t)
        var view_l: LShapeView = itemView.findViewById(R.id.view_l)
        var view_i: IShapeView = itemView.findViewById(R.id.view_i)
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