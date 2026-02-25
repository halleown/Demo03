package com.example.demo03.form

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.demo03.R

abstract class BaseCustomFormView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attr, defStyleAttr) {

    private val TAG = "BaseCustomFormView"

    protected abstract fun initView()

    /**
     * 目标View，用于设置表单内文本对齐方式
     */
    abstract fun getTargetAlignView(): View?

    /**
     * 表单内文本对齐方式：0 左对齐 1 居中对齐 2 右对齐
     */
    protected var mTextAlign: Int = 0

    private var styleMap: MutableMap<FormState, FormStateStyle> = mutableMapOf()

    /**
     * 应用样式，子类必须实现此方法以响应状态变化
     */
    protected abstract fun onApplyStateStyle(style: FormStateStyle)

    /**
     * 自定义控件是否启用
     */
    protected var mIsEnable = true

    init {
        attr?.let {
            val a = context.obtainStyledAttributes(attr, R.styleable.BaseCustomFormView)
            mTextAlign = a.getInt(R.styleable.BaseCustomFormView_mTextAlign, mTextAlign)
            a.recycle()
        }
        initView()
        initDefaultStyles()
        // 默认选中Normal状态
        updateStateStyle(FormState.Normal)

        setAlignMethod(mTextAlign)
    }

    /**
     * 设置表单内文本对齐方式
     */
    fun setAlignMethod(align: Int?) {
        val view = getTargetAlignView() as? android.widget.TextView ?: return
        align?.let {
            view.gravity = when (it) {
                1 -> Gravity.CENTER
                2 -> Gravity.END or Gravity.CENTER_VERTICAL
                else -> Gravity.START or Gravity.CENTER_VERTICAL
            }
            mTextAlign = it
        }
    }

    open fun initDefaultStyles() {
        val defaults = listOf(
            FormStateStyle(
                FormState.Normal,
                R.drawable.custom_form_view_normal_bg,
                R.color.black,
                true
            ),
            FormStateStyle(
                FormState.Disable,
                R.drawable.custom_form_view_disable_bg,
                R.color.light_gray,
                false
            )
        )
        addStateStyles(defaults)
    }

    fun setStateStyles(styles: List<FormStateStyle>, defaultStyle: FormState = FormState.Normal) {
        styleMap.clear()
        addStateStyles(styles)
        updateStateStyle(defaultStyle)
    }

    fun addStateStyles(styles: List<FormStateStyle>) {
        styleMap.putAll(styles.associateBy { it.backgroundState })
    }

    fun addStateStyle(style: FormStateStyle) {
        styleMap[style.backgroundState] = style
    }

    /**
     * 更新表单状态样式
     */
    fun updateStateStyle(state: FormState) {
        if (styleMap[state] == null) {
            Log.e(TAG, "状态 [$state] 不存在")
        }
        val style = styleMap[state] ?: styleMap.getValue(FormState.Normal)
        onApplyStateStyle(style)
    }

}

sealed class FormState(val tag: String) {
    object Normal : FormState("normal")
    object Disable : FormState("disable")
    object Error : FormState("error")

    override fun equals(other: Any?): Boolean = (other is FormState) && other.tag == tag
    override fun hashCode(): Int = tag.hashCode()
}

data class FormStateStyle(
    val backgroundState: FormState,  // 背景状态标识
    val backgroundRes: Int,         // 背景资源 ID (drawable)
    val textColorRes: Int,          // 文字颜色资源 ID
    val isEnable: Boolean = true,   // 当前组件是否启用，是否能够响应事件
)