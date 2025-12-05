package com.example.demo03

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class Activity2 : AppCompatActivity() {

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.i("exceptionHandler", "$throwable")
    }
    lateinit var btn: Button

    init {
        Log.i("aaa", "init")
        lifecycle
        lifecycleScope.launchWhenResumed {
            Log.i("aaa", "在类初始化位置启动协程")
        }
        Dispatchers.Main.immediate
        val scope = lifecycle.coroutineScope

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)
        btn = findViewById<Button>(R.id.btn)
//        lifecycleScope.launch {
//            delay(2000)
//            Toast.makeText(this@Activity2,"haha", Toast.LENGTH_SHORT).show()
//        }

        requestMain {
            delay(2000)
            Toast.makeText(this@Activity2, "haha", Toast.LENGTH_SHORT).show()
        }
        requestIO {
//            loadNetData()
        }
        delayMain(delayTime = 100) {
            Toast.makeText(this@Activity2, "haha", Toast.LENGTH_SHORT).show()
        }

        btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                start()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.i("aaa", "onResume")
    }

    fun testException() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.i("exceptionHandler", "${coroutineContext[CoroutineName].toString()} 处理异常 ：$throwable")
        }
        val superScope = CoroutineScope(SupervisorJob() + exceptionHandler)
        with(superScope) {
            this.launch {

            }
        }
    }

    var job: Job? = null
    fun start() {
        // 创建一个 CoroutineExceptionHandler
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e("MyCoroutine", "${coroutineContext[CoroutineName]} Caught $throwable")
        }
        job = GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
            supervisorScope {
                launch {
                    //网络请求1...
                    throw NullPointerException("空指针")
                }
                val result = withContext(Dispatchers.IO) {
                    //网络请求2...
//                requestData()
                    "请求结果"
                }
                Log.i("aaa", "thread:${Thread.currentThread().name}")
                btn.text = result
                launch {
                    //网络请求3...
                }
            }
        }
    }

    fun load() {
        lifecycleScope.launch(exceptionHandler) {
            //省略...
        }
        lifecycleScope.launch(exceptionHandler) {
            //省略...
        }
        lifecycleScope.launch(exceptionHandler) {
            //省略...
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}

inline fun AppCompatActivity.requestMain(
    errCode: Int = -1, errMsg: String = "", report: Boolean = false,
    noinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch(GlobalCoroutineExceptionHandler(errCode, errMsg, report)) {
        block.invoke(this)
    }
}

inline fun AppCompatActivity.requestIO(
    errCode: Int = -1, errMsg: String = "", report: Boolean = false,
    noinline block: suspend CoroutineScope.() -> Unit
): Job {
    return lifecycleScope.launch(Dispatchers.IO + GlobalCoroutineExceptionHandler(errCode, errMsg, report)) {
        block.invoke(this)
    }
}

inline fun AppCompatActivity.delayMain(
    errCode: Int = -1, errMsg: String = "", report: Boolean = false,
    delayTime: Long, noinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch(GlobalCoroutineExceptionHandler(errCode, errMsg, report)) {
        withContext(Dispatchers.IO) {
            delay(delayTime)
        }
        block.invoke(this)
    }
}
