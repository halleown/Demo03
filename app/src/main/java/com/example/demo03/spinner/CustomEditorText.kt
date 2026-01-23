package com.example.demo03.spinner

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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

    /**
     * 显示自定义键盘
     */
    var isCustom: Boolean = false

    private lateinit var etInput: EditText
    private lateinit var llView: LinearLayout
    // private var onValueChangeListener: ((String) -> Unit)? = null
    private var listener: onViewChangeListener? = null

    init {
        attr?.let {
            val a = context.obtainStyledAttributes(attr, R.styleable.CustomEditorText)
            inputType = a.getInt(R.styleable.CustomEditorText_mInputType, inputType)
            minNum = a.getInt(R.styleable.CustomEditorText_mMinNum, minNum)
            maxNum = a.getInt(R.styleable.CustomEditorText_mMaxNum, maxNum)
            a.recycle()
        }

        onApplyConfig = { config ->
            llView.setBackgroundResource(config.backgroundRes)
            etInput.setTextColor(ContextCompat.getColor(context, config.textColorRes))
            etInput.isEnabled = config.isEnable
            // llView.alpha = if (config.isEnable) 1.0f else 0.5f
        }
    }

    @LayoutRes
    open fun getLayoutId(): Int = R.layout.ui_custom_editor_text

    override fun getTargetAlignView(): View? = etInput

    override fun initView() {
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        etInput = findViewById(R.id.et_input)
        llView = findViewById(R.id.ll_view)


        etInput.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && inputType == 1) { // 失去焦点且是数字类型
                val info = etInput.text.toString().trim()
                if (info.isNotEmpty()) {
                    try {
                        val num = info.toInt()
                        if (num < minNum) {
                            etInput.setText(minNum.toString())
                        }
                    } catch (e: Exception) {
                        etInput.setText(minNum.toString())
                    }
                }
            }
        }

        etInput.setOnTouchListener { v: View, event: MotionEvent? ->
            if (event?.action == MotionEvent.ACTION_DOWN) {
                if (v is EditText && isCustom) {
                    listener?.onTouchEditor(etInput, true)
                }
            }
            false
        }

        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val info = s.toString().trim()
                if (inputType == 1) {
                    if (info.isEmpty()) return

                    if (!info.matches("\\d+".toRegex())) {
                        return
                    }
                    var oldText = info

                    try {
                        val num = info.toInt()
                        if (num > maxNum) {
                            oldText = info.substring(0, info.length - 1)
                            etInput.setText(oldText)
                            etInput.setSelection(oldText.length)
                        }
                    } catch (e: Exception) {
                        oldText = info.substring(0, info.length - 1)
                        etInput.setText(oldText)
                        etInput.setSelection(oldText.length)
                    }
                    listener?.onValueChange(oldText)
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    /**
     * 设置最多输入多少字符
     */
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

    /**
     * 当输入类型为数字类型时，数字的最大值
     */
    fun setMaxNum(max: Int) {
        this.maxNum = max
    }

    /**
     * 当输入类型为数字类型时，数字的最小值
     */
    fun setMinNum(min: Int) {
        this.minNum = min
    }


    fun setOnValueChangeListener(listener: onViewChangeListener) {
        this.listener = listener
    }

    interface onViewChangeListener {
        fun onValueChange(s: String)

        fun onTouchEditor(editText: EditText, isCustom: Boolean)
    }
}