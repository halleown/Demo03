package com.example.demo03.spinner

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.demo03.R

/**
 * 自定义下拉框
 */
class CustomSpinnerView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attr) {

    private val TAG = "CustomSpinnerView"
    private lateinit var tvSpinnerField: TextView
    private lateinit var ivDrop: ImageView
    private lateinit var rlView: RelativeLayout
    private lateinit var mDropPopupWindow: PopupWindow
    private lateinit var mDropAdapter: PopFormAdapter

    /**
     * 下拉框列表数据源
     */
    private var dropList: MutableList<String> = mutableListOf()
    private var dismissTime: Long = 0

    /**
     * 状态配置
     */
    private var statusMap: Map<String, ViewStatusConfig> = emptyMap()

    /**
     * 下拉框是否启用
     */
    private var isEnable = true

    /**
     * 记录当前选中索引
     */
    var selectedIndex: Int = -1
        private set

    private var mOnViewChangeListener: OnViewChangeListener? = null

    init {
        initView(context)
        initDropDialog()
        setupDefaultConfigs()
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.ui_custom_spinner_view, this, true)
        tvSpinnerField = findViewById(R.id.tv_spinner_field)
        ivDrop = findViewById(R.id.iv_drop)
        rlView = findViewById(R.id.rl_view)

        rlView.setOnClickListener {
            if (!isEnable) return@setOnClickListener
            // 防抖：如果刚刚关闭，300ms内不允许再次打开
            if (System.currentTimeMillis() - dismissTime > 300) {
                showPopDialog()
            }
        }
    }

    private fun setupDefaultConfigs() {
        val defaults = listOf(
            ViewStatusConfig(
                "normal",
                R.drawable.custom_spinner_view_normal_bg,
                R.color.black,
                true
            ),
            ViewStatusConfig(
                "disabled",
                R.drawable.custom_spinner_view_disable_bg,
                R.color.light_gray,
                false
            ),
        )
        setStatusConfigs(defaults)
    }

    private fun initDropDialog() {
        val popView = LayoutInflater.from(context).inflate(R.layout.pop_form_drop_view, null)
        val listView = popView.findViewById<ListView>(R.id.listview)

        mDropPopupWindow = PopupWindow(
            popView,
            LayoutParams.WRAP_CONTENT, // 宽度会在显示时动态覆盖
            LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            isOutsideTouchable = true
            setOnDismissListener {
                dismissTime = System.currentTimeMillis()
                setDropIcon(false)
            }
        }

        mDropAdapter = PopFormAdapter(context, dropList)
        listView.adapter = mDropAdapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectItem(position)
            mDropPopupWindow.dismiss()
        }
    }

    /**
     * 显示下拉框并自动计算方向
     */
    private fun showPopDialog() {
        setDropIcon(true)

        // 强制下拉框宽度与输入框一致
        mDropPopupWindow.width = rlView.width

        // 计算位置
        val location = IntArray(2)
        rlView.getLocationOnScreen(location)
        val screenHeight = resources.displayMetrics.heightPixels
        val viewBottom = location[1] + rlView.height

        // 测量内容高度
        mDropPopupWindow.contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val popHeight = mDropPopupWindow.contentView.measuredHeight

        if (screenHeight - viewBottom < popHeight) {
            // 空间不足，向上弹出 (偏移量为：-(popHeight + 控件本身高度))
            mDropPopupWindow.showAsDropDown(rlView, 0, -(popHeight + rlView.height))
        } else {
            mDropPopupWindow.showAsDropDown(rlView)
        }
    }

    private fun setDropIcon(isUp: Boolean) {
        val resId = if (isUp) R.drawable.ic_select_up_black else R.drawable.ic_select_drop_black
        ivDrop.setImageResource(resId)
    }

    fun setStatusConfigs(configs: List<ViewStatusConfig>) {
        statusMap = configs.associateBy { it.stateTag }
        // 默认选中normal模式
        updateState("normal")
    }

    fun updateState(tag: String) {
        statusMap[tag]?.let { config ->
            rlView.setBackgroundResource(config.backgroundRes)
            tvSpinnerField.setTextColor(ContextCompat.getColor(context, config.textColorRes))

            this.isEnable = config.isEnable
//            // 禁用逻辑处理
//            if (tag == "disabled") {
////                this.isEnabled = false
//                rlView.alpha = 0.5f
//            } else {
////                this.isEnabled = true
//                rlView.alpha = 1.0f
//            }
        }
    }

    fun setDropList(list: List<String>, defaultPos: Int = -1) {
        dropList.clear()
        dropList.addAll(list)
        mDropAdapter.setData(dropList)
        if (defaultPos in list.indices) {
            selectItem(defaultPos)
        }
    }

    fun selectItem(position: Int) {
        if (position in dropList.indices) {
            selectedIndex = position
            tvSpinnerField.text = dropList[position]
            mOnViewChangeListener?.onSelectChanged(position, dropList[position])
        }
    }

    fun setOnViewChangeListener(listener: OnViewChangeListener) {
        this.mOnViewChangeListener = listener
    }

    interface OnViewChangeListener {
        fun onSelectChanged(index: Int, text: String)
    }
}