package com.example.demo03.spinner

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.example.demo03.R

abstract class BaseCustomFormView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attr, defStyleAttr) {

    private var statusMap: Map<String, ViewStatusConfig> = emptyMap()

    protected var onApplyConfig: ((ViewStatusConfig) -> Unit)? = null

    /**
     * 自定义控件是否启用
     */
    protected var isEnable = true


    abstract fun initView()

    init {
        initView()
        setupDefaultConfigs()
    }

    open fun setupDefaultConfigs() {
        val defaults = listOf(
            ViewStatusConfig(
                "normal",
                R.drawable.custom_form_view_normal_bg,
                R.color.black,
                true
            ),
            ViewStatusConfig(
                "disabled",
                R.drawable.custom_form_view_disable_bg,
                R.color.light_gray,
                false
            )
        )
        setStatusConfigs(defaults)
    }

    open fun setStatusConfigs(configs: List<ViewStatusConfig>) {
        statusMap = configs.associateBy { it.stateTag }
        updateState("normal")
    }

    /**
     * 更新视图状态
     */
    fun updateState(tag: String) {
        if (statusMap[tag] == null) {
            throw RuntimeException("状态不存在")
        }
        statusMap[tag]?.let { config ->
            onApplyConfig?.invoke(config)
        }
    }
}

data class ViewStatusConfig(
    val stateTag: String,           // 状态标识，例如 "normal", "error", "disabled"
    val backgroundRes: Int,         // 背景资源 ID (drawable)
    val textColorRes: Int,          // 文字颜色资源 ID
    val isEnable: Boolean = true,   // 该状态下组件是否响应交互
)