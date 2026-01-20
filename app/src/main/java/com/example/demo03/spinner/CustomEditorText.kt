package com.example.demo03.spinner

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.example.demo03.R

/**
 * 自定义输入框控件
 */
open class CustomEditorText @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseCustomFormView(context, attr, defStyleAttr) {

    private lateinit var etInput: EditText
    private lateinit var llView: LinearLayout
    private var onValueChangeListener: ((String) -> Unit)? = null

    init {
        onApplyConfig = { config ->
            llView.setBackgroundResource(config.backgroundRes)
            etInput.setTextColor(ContextCompat.getColor(context, config.textColorRes))
            etInput.isEnabled = config.isEnable
            // llView.alpha = if (config.isEnable) 1.0f else 0.5f
        }
    }

    @LayoutRes
    open fun getLayoutId(): Int = R.layout.ui_custom_editor_text

    override fun initView() {
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        etInput = findViewById(R.id.et_input)
        llView = findViewById(R.id.ll_view)

        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onValueChangeListener?.invoke(s.toString())
            }
        })
    }

    fun getText(): String = etInput.text.toString().trim()

    fun setText(text: String) {
        etInput.setText(text)
    }

    fun setHint(hint: String) {
        etInput.hint = hint
    }

    fun setOnValueChangeListener(listener: (String) -> Unit) {
        this.onValueChangeListener = listener
    }
}