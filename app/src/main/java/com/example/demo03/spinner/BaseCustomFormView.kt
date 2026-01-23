package com.example.demo03.spinner

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.demo03.R

abstract class BaseCustomFormView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attr, defStyleAttr) {

    /**
     * 目标View，用于设置文本对齐方式
     */
    abstract fun getTargetAlignView(): View?

    /**
     * 文本对齐方式：0 左对齐 1 居中对齐 2 右对齐
     */
    protected var mTextAlign: Int = 0

    private var statusMap: MutableMap<FormState, ViewStatusConfig> = mutableMapOf()
    protected var onApplyConfig: ((ViewStatusConfig) -> Unit)? = null

    /**
     * 自定义控件是否启用
     */
    protected var isViewEnable = true

    abstract fun initView()


    init {
        attr?.let {
            val a = context.obtainStyledAttributes(attr, R.styleable.BaseCustomFormView)
            mTextAlign = a.getInt(R.styleable.BaseCustomFormView_mTextAlign, mTextAlign)
            a.recycle()
        }
        initView()
        initDefaultConfigs()

        setAlignMethod(mTextAlign)
    }

    /**
     * 设置文本对齐方式
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

    open fun initDefaultConfigs() {
        val defaults = listOf(
            ViewStatusConfig(
                FormState.Normal,
                R.drawable.custom_form_view_normal_bg,
                R.color.black,
                true
            ),
            ViewStatusConfig(
                FormState.Disable,
                R.drawable.custom_form_view_disable_bg,
                R.color.light_gray,
                false
            )
        )
        statusMap.putAll(defaults.associateBy { it.backgroundState })
    }

    fun setStatusConfigs(configs: List<ViewStatusConfig>, posState: Int = 0) {
        statusMap.clear()
        statusMap.putAll(configs.associateBy { it.backgroundState })
        updateState(posState)
    }

    fun addStatusConfigs(configs: List<ViewStatusConfig>) {
        statusMap.putAll(configs.associateBy { it.backgroundState })
    }

    fun addStatusConfig(config: ViewStatusConfig) {
        statusMap[config.backgroundState] = config
    }

    /**
     * 更新视图状态
     */
    fun updateState(state: FormState) {
        val config = statusMap[state] ?: throw RuntimeException("状态 [$state] 不存在")
        onApplyConfig?.invoke(config)
    }

    fun updateState(statePos: Int) {
        if (statePos < 0 || statePos >= statusMap.size) {
            throw RuntimeException("索引 [$statePos] 超出状态范围")
        }
        val stateTag = statusMap.keys.elementAt(statePos)
        updateState(stateTag)
    }

}

sealed class FormState(val tag: String) {
    object Normal : FormState("normal")
    object Disable : FormState("disable")
    object Error : FormState("error")

    override fun equals(other: Any?): Boolean = (other is FormState) && other.tag == tag
    override fun hashCode(): Int = tag.hashCode()
}

data class ViewStatusConfig(
    val backgroundState: FormState,  // 背景状态标识
    val backgroundRes: Int,         // 背景资源 ID (drawable)
    val textColorRes: Int,          // 文字颜色资源 ID
    val isEnable: Boolean = true,   // 当前组件是否启用
)