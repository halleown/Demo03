package com.example.demo03

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.demo03.graph.GraphActivity
import com.example.demo03.scan.QRCodeScanActivity
import com.example.demo03.spinner.CustomEditorText
import com.example.demo03.spinner.CustomSpinnerView
import com.example.demo03.spinner.ViewStatusConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.jvm.java

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initview()
    }

    private fun initview() {
//        testConroutineContext()
        findViewById<Button>(R.id.btn1).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
//                testConroutineStart()
//                testUnDispatched()
//                testCoroutineScope()
//                testCoRoutineExptionHandler()
//                testException()
//                testException2()
//                testException3()
//                test()
//                test2()
//                test3()
//                test4()
                test5()
            }
        })

        findViewById<Button>(R.id.btn2).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity, Activity2::class.java))
            }
        })
        findViewById<Button>(R.id.btn3).setOnClickListener { startActivity(Intent(this@MainActivity, Activity3::class.java)) }
        findViewById<Button>(R.id.btn4).setOnClickListener {
            startActivity(Intent(this@MainActivity, Activity4::class.java))
        }
        findViewById<Button>(R.id.btn5).setOnClickListener {
            startActivity(Intent(this@MainActivity, QRCodeScanActivity::class.java))
        }
        findViewById<Button>(R.id.btn6).setOnClickListener {
            startActivity(Intent(this@MainActivity, GraphActivity::class.java))
        }


        val spinner = findViewById<CustomSpinnerView>(R.id.mySpinner)
        val myEditor = findViewById<CustomEditorText>(R.id.myEditor)
        val defaults = listOf(
            ViewStatusConfig("normal", R.drawable.custom_form_view_normal_bg, R.color.black),
            ViewStatusConfig(
                "disabled",
                R.drawable.custom_form_view_disable_bg,
                R.color.light_gray,
                false
            ),
            ViewStatusConfig("error", R.drawable.trim_spinner_view_error, R.color.white, false)
        )
        spinner.setStatusConfigs(defaults)
        myEditor.setStatusConfigs(defaults)

        val data = listOf("北京", "上海", "广州", "深圳", "aaa", "bbb", "ccc")
        spinner.setDropList(data, defaultPos = 0)

        // 2. 监听选择
        spinner.setOnViewChangeListener(object : CustomSpinnerView.OnViewChangeListener {
            override fun onSelectChanged(index: Int, text: String) {
                // 选择后自动恢复 normal 状态
                spinner.updateState("normal")
                Log.d("Spinner", "Selected: $text")
            }
        })

        // 3. 模拟业务逻辑切换状态
        findViewById<Button>(R.id.btn7).setOnClickListener {
            spinner.updateState("normal")
            myEditor.updateState("normal")

        }

        findViewById<Button>(R.id.btn8).setOnClickListener {
            spinner.updateState("disabled")
            myEditor.updateState("disabled")
        }

        findViewById<Button>(R.id.btn9).setOnClickListener {
            spinner.updateState("error")
            myEditor.updateState("error")
        }

       startActivity(Intent(this@MainActivity, GraphActivity::class.java))
    }

    private fun testConroutineContext() {
        val coroutineContext1 = Job() + CoroutineName("第一个上下文")
        Log.i("coroutineContext1", "$coroutineContext1")
        val coroutineContext2 = coroutineContext1 + Dispatchers.Default + CoroutineName("第二个上下文")
        Log.i("coroutineContext2", "$coroutineContext2")
        val coroutineContext3 = coroutineContext2 + Dispatchers.Main + CoroutineName("第三个上下文")
        Log.i("coroutineContext3", "$coroutineContext3")
    }

    private fun testConroutineStart() {
        val defultJob = GlobalScope.launch {
            Log.i("defaultJob", "CoroutineStart.DEFAULT")
        }
        defultJob.cancel()
        val lazyJob = GlobalScope.launch(start = CoroutineStart.LAZY) {
            Log.i("lazyJob", "CoroutineStart.LAZY")
        }
        val atomicJob = GlobalScope.launch(start = CoroutineStart.ATOMIC) {
            Log.i("atomicJob", "CoroutineStart.ATOMIC挂起前")
            delay(100)
            Log.i("atomicJob", "CoroutineStart.ATOMIC挂起后")
        }
        atomicJob.cancel()
        val undispatchedJob = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            Log.i("undispatchedJob", "CoroutineStart.UNDISPATCHED挂起前")
            delay(100)
            Log.i("undispatchedJob", "CoroutineStart.UNDISPATCHED挂起后")
        }
        undispatchedJob.cancel()
    }

    private fun testUnDispatched() {
        GlobalScope.launch(Dispatchers.Main) {
//            val job = launch(Dispatchers.IO) {
//            val job = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                Log.i("${Thread.currentThread().name}线程", "-> 挂起前")
                delay(100)
                Log.i("${Thread.currentThread().name}线程", "-> 挂起后")
            }
            Log.i("${Thread.currentThread().name}线程", "-> join前")
            job.join()
            Log.i("${Thread.currentThread().name}线程", "-> join后")
        }
    }

    private fun testCoroutineScope() {
        GlobalScope.launch(Dispatchers.Main) {
            Log.i("父协程上下文", "$coroutineContext")
            launch(CoroutineName("第一个子协程")) {
                Log.i("第一个子协程上下文", "$coroutineContext")
            }
            launch(Dispatchers.Unconfined) {
                Log.i("第二个子协程上下文", "$coroutineContext")
            }
        }

    }

    private fun testCoRoutineExptionHandler() {
        GlobalScope.launch {
            val job = GlobalScope.launch {
                Log.d("${Thread.currentThread().name}", " 抛出未捕获异常")
                throw NullPointerException("异常测试")
            }
            job.join()
            Log.d("${Thread.currentThread().name}", "end")

        }
    }

    private fun testException() {
        GlobalScope.launch {
            GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
                Log.d("${Thread.currentThread().name}", " 我要开始抛异常了")
                try {
                    throw NullPointerException("异常测试")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Log.d("${Thread.currentThread().name}", "end")
        }
    }

    private fun testException2() {
        var a: MutableList<Int> = mutableListOf(1, 2, 3)
        GlobalScope.launch {
            GlobalScope.launch {
                Log.d("${Thread.currentThread().name}", "我要开始抛异常了")
                try {
                    launch {
                        Log.d("${Thread.currentThread().name}", "${a[1]}")
                    }
                    a.clear()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun testException3() {
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.i("exceptionHandler", "${coroutineContext[CoroutineName]} ：$throwable")
        }
        GlobalScope.launch(coroutineExceptionHandler + CoroutineName("testException3")) {
            val job = launch {
                Log.d("${Thread.currentThread().name}", "我要开始抛异常了")
                throw NullPointerException("异常测试")
            }
            Log.d("${Thread.currentThread().name}", "end")
        }
    }

    private fun test() {
        var name: String? = null
        // 捕获并处理异常
        try {
            checkNotNull(name, { "变量为空" })
            name!!.count()
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    private fun test2() {
        var aaa = -1
        // 捕获并处理异常
        try {
            require(aaa > 0) { "aaa必须大于0" }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun test3() {
        var name: String? = null
        // 捕获并处理异常
        try {
            requireNotNull(name) { "requireNotNull返回:变量为空" }
            name!!.count()
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    private fun test4() {
        var aaa = -1
        // 捕获并处理异常
        try {
            assert(aaa > 0) { "aaa小于0" }
            Log.i("aaa", "aaa=$aaa")
        } catch (e: Error) {
            e.printStackTrace()

        }
    }

    private fun test5() {
        runBlocking {
            Log.d("runBlocking", "启动一个协程")
        }
    }
}
