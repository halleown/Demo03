package com.example.demo03.spinner

/**
 * 通用组件状态配置项
 * 用于统一管理输入框、下拉框等组件在不同业务状态（正常、错误、禁用）下的视觉表现
 */
data class ViewStatusConfig(
    val stateTag: String,           // 状态标识，例如 "normal", "error", "disabled"
    val backgroundRes: Int,         // 背景资源 ID (drawable)
    val textColorRes: Int,          // 文字颜色资源 ID
    val isEnable: Boolean = true,   // 该状态下组件是否响应交互
)