package com.example.demo03.spinner

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.example.demo03.R

/**
 * 自定义下拉框
 */
open class CustomSpinnerView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseCustomFormView(context, attr, defStyleAttr) {

    private val TAG = "CustomSpinnerView"
    protected lateinit var tvSpinnerValue: TextView
    protected lateinit var ivDrop: ImageView
    protected lateinit var rlView: RelativeLayout
    private lateinit var mDropPopupWindow: PopupWindow
    private lateinit var mDropAdapter: CustomSpinnerDropAdapter

    /**
     * 下拉框列表数据源
     */
    private var dropList: MutableList<String> = mutableListOf()
    private var dismissTime: Long = 0

    /**
     * 记录当前选中索引
     */
    var selectedIndex: Int = -1
        private set

    private var mOnSpinnerItemSelectedListener: OnSpinnerItemSelectedListener? = null

    init {
        initDropDialog()
    }

    override fun onApplyStateStyle(style: FormStateStyle) {
        rlView.setBackgroundResource(style.backgroundRes)
        tvSpinnerValue.setTextColor(ContextCompat.getColor(context, style.textColorRes))
        this.mIsEnable = style.isEnable
    }

    @LayoutRes
    open fun getLayoutId(): Int = R.layout.ui_custom_spinner_view

    override fun getTargetAlignView(): View? = null

    override fun initView() {
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)
        tvSpinnerValue = findViewById(R.id.tv_spinner_value)
        tvSpinnerValue.gravity = Gravity.START
        ivDrop = findViewById(R.id.iv_drop)
        rlView = findViewById(R.id.rl_view)

        rlView.setOnClickListener {
            if (!mIsEnable) return@setOnClickListener
            // 防抖：如果刚刚关闭，300ms内不允许再次打开
            if (System.currentTimeMillis() - dismissTime > 300) {
                showPopDialog()
            }
        }
    }

    private fun initDropDialog() {
        val popView = LayoutInflater.from(context).inflate(R.layout.custom_spinner_drop_view, null)
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

        mDropAdapter = CustomSpinnerDropAdapter(context, dropList)
        listView.adapter = mDropAdapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            setSpinnerPos(position)
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

    /**
     * 设置下拉框选项数据
     */
    fun setDropList(list: List<String>, defaultPos: Int = -1) {
        dropList.clear()
        dropList.addAll(list)
        mDropAdapter.setData(dropList)
        setSpinnerPos(defaultPos)
    }

    /**
     * 设置下拉框选中项
     */
    fun setSpinnerPos(position: Int) {
        if (position in dropList.indices) {
            selectedIndex = position
            tvSpinnerValue.text = dropList[position]
            mOnSpinnerItemSelectedListener?.onItemSelected(position, dropList[position])
        }
    }

    fun setOnSpinnerItemSelectedListener(listener: OnSpinnerItemSelectedListener) {
        this.mOnSpinnerItemSelectedListener = listener
    }

    interface OnSpinnerItemSelectedListener {
        fun onItemSelected(pos: Int, text: String)
    }
}