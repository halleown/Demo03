package com.example.demo03.spinner

import android.content.Context
import android.text.Editable
import android.text.InputFilter
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

    /**
     * 输入类型：0：字符串 1：数字
     */
    private var inputType: Int = 0

    /**
     * 输入类型为数字时，限定输入的最小值
     */
    private var minNum: Int = 0

    /**
     * 输入类型为数字时，限定输入的最大值
     */
    private var maxNum: Int = Int.MAX_VALUE
    private lateinit var etInput: EditText
    private lateinit var llView: LinearLayout
    private var onValueChangeListener: ((String) -> Unit)? = null

    init {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.CustomEditorText)
        inputType = typedArray.getIndex(R.styleable.CustomEditorText_mInputType)
        minNum = typedArray.getInt(R.styleable.CustomEditorText_mMinNum, minNum)
        maxNum = typedArray.getInt(R.styleable.CustomEditorText_mMaxNum, maxNum)
        typedArray.recycle()

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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val info = s.toString().trim()
                when (inputType) {
                    1 -> {// 数字类型
                        // todo 先判断输入的内容是否为纯数字
                        val num = 0
                        if (num < minNum && num > maxNum) {
                            // 删掉最后一个输入的字符
                            etInput.setText(info.substring(0, info.length - 1))
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
                onValueChangeListener?.invoke(s.toString())
            }
        })
    }

    fun setMaxLen(max: Int) {
        etInput.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(max)))
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