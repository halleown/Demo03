package com.example.demo03

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.demo03.coroutine.Activity1
import com.example.demo03.coroutine.Activity2
import com.example.demo03.coroutine.Activity3
import com.example.demo03.form.CustomFormActivity
import com.example.demo03.graph.GraphActivity
import com.example.demo03.scan.QRCodeScanActivity
import com.example.demo03.tree.Activity4
import kotlin.jvm.java

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        findViewById<Button>(R.id.btn1).setOnClickListener {
            startActivity(Intent(this@MainActivity, Activity1::class.java))
        }
        findViewById<Button>(R.id.btn2).setOnClickListener {
            startActivity(Intent(this@MainActivity, Activity2::class.java))
        }
        findViewById<Button>(R.id.btn3).setOnClickListener {
            startActivity(Intent(this@MainActivity, Activity3::class.java))
        }
        findViewById<Button>(R.id.btn4).setOnClickListener {
            startActivity(Intent(this@MainActivity, Activity4::class.java))
        }
        findViewById<Button>(R.id.btn5).setOnClickListener {
            startActivity(Intent(this@MainActivity, QRCodeScanActivity::class.java))
        }
        findViewById<Button>(R.id.btn6).setOnClickListener {
            startActivity(Intent(this@MainActivity, GraphActivity::class.java))
        }
        findViewById<Button>(R.id.btn7).setOnClickListener {
            startActivity(Intent(this@MainActivity, CustomFormActivity::class.java))
        }

    }
}
