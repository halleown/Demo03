package com.example.demo03.form

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.demo03.R

/**
 * 封装自定义表单
 */
class CustomFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_form)
        initView()
    }

    private fun initView() {
        val spinner = findViewById<CustomSpinnerView>(R.id.mySpinner)
        val spinner1 = findViewById<CustomSpinnerView>(R.id.spinner1)
        val myEditor = findViewById<CustomEditorText>(R.id.myEditor)

        val errorState = FormStateStyle(
            FormState.Error,
            R.drawable.trim_spinner_view_error,
            R.color.white,
            false
        )
        myEditor.addStateStyle(errorState)
        myEditor.updateStateStyle(FormState.Disable)

        val data = listOf("北京", "上海", "广州", "深圳", "aaa", "bbb", "ccc")
        spinner.setDropList(data, defaultPos = 0)
        spinner.setShowDropItem(5)

        spinner1.setDropList(data, 3)


        spinner.setOnSpinnerItemSelectedListener(object : CustomSpinnerView.OnSpinnerItemSelectedListener {
            override fun onItemSelected(index: Int, text: String) {
                // 选择后自动恢复 normal 状态
                spinner.updateStateStyle(FormState.Normal)
                Log.d("Spinner", "Selected: $text")
            }
        })

        // 3. 模拟业务逻辑切换状态
        findViewById<Button>(R.id.btn1).setOnClickListener {
            spinner.updateStateStyle(FormState.Normal)
            myEditor.updateStateStyle(FormState.Normal)
        }

        findViewById<Button>(R.id.btn2).setOnClickListener {
            spinner.updateStateStyle(FormState.Disable)
            myEditor.updateStateStyle(FormState.Disable)
        }

        findViewById<Button>(R.id.btn3).setOnClickListener {
            spinner.updateStateStyle(FormState.Error)
            myEditor.updateStateStyle(FormState.Error)
        }
    }
}