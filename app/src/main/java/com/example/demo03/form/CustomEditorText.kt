package com.example.demo03.form

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

    protected lateinit var etInput: EditText
    protected lateinit var llView: LinearLayout

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
    private var isCustomKeyBoard: Boolean = false

    private var listener: OnEditorActionListener? = null

    init {
        attr?.let {
            val a = context.obtainStyledAttributes(attr, R.styleable.CustomEditorText)
            inputType = a.getInt(R.styleable.CustomEditorText_mInputType, inputType)
            minNum = a.getInt(R.styleable.CustomEditorText_mMinNum, minNum)
            maxNum = a.getInt(R.styleable.CustomEditorText_mMaxNum, maxNum)
            a.recycle()
        }
    }

    override fun onApplyStateStyle(style: FormStateStyle) {
        llView.setBackgroundResource(style.backgroundRes)
        etInput.setTextColor(ContextCompat.getColor(context, style.textColorRes))
        etInput.isEnabled = style.isEnable
    }

    @LayoutRes
    open fun getLayoutId(): Int = R.layout.ui_custom_editor_text

    override fun getTargetAlignView(): View? = etInput

    override fun initView() {
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        etInput = findViewById(R.id.et_input)
        llView = findViewById(R.id.ll_view)

        etInput.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && inputType == 1) { // 失去焦点且是数字类型，检查最小值
                validateMinNum()
            }
        }

        etInput.setOnTouchListener { v: View, event: MotionEvent? ->
            if (event?.action == MotionEvent.ACTION_DOWN) {
                if (v is EditText && isCustomKeyBoard) {
                    listener?.onEditorTouched(etInput, true)
                }
            }
            false
        }

        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener?.onTextChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                if (inputType == 1 && s != null && s.isNotEmpty()) {
                    validateNumberInput(s)
                }
            }
        })
    }
    
    private fun validateMinNum() {
        val info = etInput.text.toString().trim()
        if (info.isNotEmpty()) {
            try {
                val num = info.toInt()
                if (num < minNum) {
                    etInput.setText(minNum.toString())
                }
            } catch (e: NumberFormatException) {
                etInput.setText(minNum.toString())
            }
        }
    }

    private fun validateNumberInput(s: Editable) {
        val info = s.toString().trim()
        if (info.isEmpty()) return

        // 如果不是纯数字（由于inputType=1通常配合xml inputType=number，这里做额外保障）
        if (!info.matches(Regex("-?\\d+"))) { // 支持负数的话用 -?
             // 这里简单处理，如果仅仅是检查数字范围
        }
        
        try {
            val num = info.toLong()
            if (num > maxNum) {
                val newText = maxNum.toString()
                if (info != newText) {
                    etInput.setText(newText)
                    etInput.setSelection(newText.length)
                }
            }
        } catch (e: NumberFormatException) {
             if (info.length > 1) {
                 // Revert to maxNum or safety
                  etInput.setText(maxNum.toString())
                  etInput.setSelection(etInput.length())
             }
        }
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


    fun setOnEditorActionListener(listener: OnEditorActionListener) {
        this.listener = listener
    }

    interface OnEditorActionListener {
        fun onTextChanged(text: String)

        fun onEditorTouched(editText: EditText, isCustomKeyboard: Boolean)
    }
}