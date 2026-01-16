package com.example.demo03.spinner

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.postDelayed
import com.example.demo03.R


/**
 * 自定义下拉框
 */
class CustomSpinnerView @JvmOverloads constructor(context: Context, attr: AttributeSet): LinearLayout(context, attr) {

    private val TAG = "CustomSpinnerView"
    private lateinit var tvSpinnerFelid: TextView
    private lateinit var ivDrop: ImageView
    private lateinit var mDropPopupWindow: PopupWindow
    private lateinit var mDropAdapter: PopFormAdapter
    private lateinit var rlView: RelativeLayout

    /**
     * 下拉框列表数据源
     */
    private var dropList: MutableList<String> = mutableListOf()

    /**
     * 下拉框选中值
     */
    private var mSpinnerField = ""
    private var dismissTime: Long = 0
    private var navigationBarHeight: Int = 0
    private var actionClickTime: Long = 0
    private var mOnViewChangeListener: OnViewChangeListener? = null

    private var statusConfigs: List<InputStatusConfig> = listOf(
        InputStatusConfig("normal", R.drawable.custom_spinner_view_normal_bg, R.color.black),
        InputStatusConfig("disabled", R.drawable.custom_spinner_view_disable_bg, R.color.light_gray),
    )

    init {
        initView(context)
        initDropDialog()
        updateState("normal")
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.ui_custom_spinner_view, this, true)
        tvSpinnerFelid = findViewById(R.id.tv_spinner_field)
        ivDrop = findViewById(R.id.iv_drop)
        rlView = findViewById(R.id.rl_view)
    }

    private fun initDropDialog() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.pop_form_drop_view, null)
        val mDropListView = view.findViewById<ListView>(R.id.listview)
        mDropPopupWindow = PopupWindow(
            view,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        mDropPopupWindow.isOutsideTouchable = true
        mDropPopupWindow.setBackgroundDrawable(ColorDrawable())
        mDropAdapter = PopFormAdapter(context, dropList)
        mDropListView.setAdapter(mDropAdapter)

        rlView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rlView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                rlView.postDelayed(50) {
                    val editWidth = rlView.measuredWidth
                    if (editWidth > 0) {
                        mDropPopupWindow.width = editWidth
                    }
                    Log.d(TAG, "onGlobalLayout: rlView: ${editWidth}")
                }
            }
        })

        rlView.setOnClickListener {
            if (!isEnabled) return@setOnClickListener
            actionClickTime = System.currentTimeMillis()
            if (actionClickTime - dismissTime > 300) {
                showPopDialog()
            } else {
                setDropImage(false)
            }
        }

        mDropListView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, view: View, position: Int, id: Long
            ) {
                if (position >= dropList.size) return
                Log.d(TAG, "onItemClick: ${dropList[position]}")
                mOnViewChangeListener?.onSelectChanged(
                    position,
                    dropList[position]
                )
                setSpinnerField(dropList[position])
                mDropPopupWindow.dismiss()
            }
        }
        mDropPopupWindow.setOnDismissListener(object : PopupWindow.OnDismissListener {
            override fun onDismiss() {
                dismissTime = System.currentTimeMillis()
                setDropImage(false)
            }
        })
    }

    fun dismissPop() {
        if (this::mDropPopupWindow.isInitialized) {
            mDropPopupWindow.dismiss()
        }
    }

    private fun showPopDialog() {
        // val isShow = mOnViewChangeListener?.onDropDownShowing()
        // if (isShow == false) {
        //     return
        // }
        setDropImage(true)

        mDropPopupWindow.contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val popupHeight = mDropPopupWindow.contentView.measuredHeight

        val location = IntArray(2)
        rlView.postDelayed(100) {
            rlView.getLocationOnScreen(location)
            // edittext的y坐标
            val anchorY = location[1]

            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            Log.d(
                TAG,
                "showPopDialog: anchorY: ${anchorY} rlViewHeight: ${rlView.height} popHeight: ${popupHeight} screenHeight: ${screenHeight} navigationBarHeight: ${navigationBarHeight}"
            )
            if (anchorY + rlView.height + popupHeight > screenHeight - navigationBarHeight) {
                // 向下显示不全，向上弹出
                mDropPopupWindow.showAsDropDown(rlView, 0, -rlView.height - popupHeight)
            } else {
                // 向下弹出
                mDropPopupWindow.showAsDropDown(rlView)
            }
        }
    }

    private fun setDropImage(isFold: Boolean) {
        var srcDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_select_up_black, context.theme)
        if (!isFold) {
            srcDrawable = ResourcesCompat.getDrawable(
                resources, R.drawable.ic_select_drop_black, context.theme
            )
        }
        ivDrop.setImageDrawable(srcDrawable)
    }

    private fun setSpinnerField(name: String?) {
        name?.let {
            mSpinnerField = name
            tvSpinnerFelid.text = name
        }
    }

    fun setDropList(dropList: MutableList<String>) {
        this.dropList.clear()
        this.dropList.addAll(dropList)
        mDropAdapter.setData(dropList)
        mDropAdapter.notifyDataSetChanged()
    }



    fun setStatusConfigs(configs: List<InputStatusConfig>) {
        this.statusConfigs = configs
    }

    fun updateState(tag: String) {
        val config = statusConfigs.find { it.stateTag == tag }
        config?.let {
            setBackgroundResource(it.backgroundRes)
            tvSpinnerFelid.setTextColor(ContextCompat.getColor(context, it.textColorRes))
        }
    }

    // fun getNavigationBarHeight(): Int {
    //     val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    //     return context.resources.getDimensionPixelSize(resourceId)
    // }

    fun setOnInputChangeListener(onViewChangeListener: OnViewChangeListener) {
        this.mOnViewChangeListener = onViewChangeListener
    }

    interface OnViewChangeListener {
        fun onSelectChanged(afterPos: Int, text: String)
    }

}

data class InputStatusConfig(
    val stateTag: String,       // 状态标识，例如 "normal", "error", "disabled"
    val backgroundRes: Int,     // 背景资源 ID (drawable)
    val textColorRes: Int       // 文字颜色资源 ID
)