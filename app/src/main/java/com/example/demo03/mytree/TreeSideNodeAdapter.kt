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
import androidx.core.view.isVisible
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
    private var columnEndPhase: Float = 0f

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

                // 显不显示I，看当前节点还有没有子节点 且 还有同级的节点
                if (!itemData.childItems.isNullOrEmpty()) {
                    // 需要添加约束条件，才显示i
                    holder.view_i.visibility = View.VISIBLE
                }

                // isShowing = position < datas.size - 1
            }
        } else {
            holder.view_l.visibility = View.GONE
            holder.view_t.visibility = View.GONE
            holder.view_i.visibility = View.GONE
        }


        // ================== 虚线 phase 串联处理 ==================
        // 规则：
        // 1. view_t 根据上一条竖线的 columnEndPhase 偏移（如果是第一个，则从 0 开始）
        // 2. view_i 根据 view_t 的最后一个虚线周期进行偏移
        // 3. view_l 根据 view_i（或上一节点）的最后一个虚线周期进行偏移
        // 4. 每个 view 画完后，更新 columnEndPhase 传给下一个 view

        when {
            holder.view_t.isVisible -> {
                // 情况 1：有 view_t，先画 T，然后根据情况画 I 或 L
                setupDashPhase(holder.view_t, columnEndPhase) { phaseAfterT ->
                    when {
                        holder.view_i.isVisible -> {
                            // T -> I：I 接 T 的尾巴
                            setupDashPhase(holder.view_i, phaseAfterT) { phaseAfterI ->
                                columnEndPhase = phaseAfterI
                            }
                        }
                        holder.view_l.isVisible -> {
                            // T -> L：L 接 T 的尾巴（没有 I 的情况）
                            setupDashPhase(holder.view_l, phaseAfterT) { phaseAfterL ->
                                columnEndPhase = phaseAfterL
                            }
                        }
                        else -> {
                            // 只有 T，没有 I/L，把 phase 传给下一条竖线
                            columnEndPhase = phaseAfterT
                        }
                    }
                }
            }
            holder.view_i.isVisible -> {
                // 情况 2：没有 T，只有 I，用上一条竖线的 columnEndPhase
                setupDashPhase(holder.view_i, columnEndPhase) { phaseAfterI ->
//                    when {
//                        holder.view_t.isVisible -> {
//                            // I -> T: T接I的尾巴
//                            setupDashPhase(holder.view_t, phaseAfterI) { phaseAfterT ->
//                                columnEndPhase = phaseAfterT
//                            }
//                        } else -> {
                            columnEndPhase = phaseAfterI
//                        }
//                    }
                }
            }
            holder.view_l.isVisible -> {
                // 情况 3：没有 T/I，只有 L，用上一条竖线的 columnEndPhase
                setupDashPhase(holder.view_l, columnEndPhase) { phaseAfterL ->
                    columnEndPhase = phaseAfterL
                }
            }
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

    /**
     * 设置虚线 View 的 phase，并在布局完成后计算下一个 phase
     * @param view 要设置的虚线 View（TShapeView / IShapeView / LShapeView）
     * @param phase 当前 phase 值
     * @param onPhaseCalculated 布局完成后回调，参数是下一个 View 应该使用的 phase
     */
    private fun setupDashPhase(
        view: BaseDashShapeView,
        phase: Float,
        onPhaseCalculated: (Float) -> Unit
    ) {
        view.setDashPhase(phase)
        view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
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
                view.removeOnLayoutChangeListener(this)
                val nextPhase = view.getNextViewPhase()
                onPhaseCalculated(nextPhase)
            }
        })
    }
}