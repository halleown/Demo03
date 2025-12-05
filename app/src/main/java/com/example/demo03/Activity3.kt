package com.example.demo03

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Activity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3)
//        start()
//        start2()
        test3()
    }

    private fun start() {
        GlobalScope.launch {
            val launchJob = launch {
                Log.i("aaalaunch", "启动一个协程")
            }

            Log.i("aaalaunchJob", "$launchJob")
            val asyncJob = async {
                Log.d("aaaasync", "启动一个协程")
                "我是async返回值"
            }
            Log.d("aaaasyncJob.await", ":${asyncJob.await()}")
            Log.d("aaaasyncJob", "$asyncJob")
        }
    }

    private fun start2() = runBlocking { // runBlocking 是一个协程构建器，用于启动顶层协程
        println("准备调用 fetchDataFromServer")
        // launch 也是一个协程构建器，它会启动一个“即发即忘”的协程
        launch {
            val data = fetchDataFromServer() // 合法调用！因为 launch 的代码块是一个 suspend lambda
            println("收到的数据: $data")
        }

        println("launch 后面的代码会立即执行，不会等待 fetchDataFromServer 完成")
    }

    // 1. 定义一个挂起函数
    suspend fun fetchDataFromServer(): String {
        println("开始获取数据...") // 运行在线程 A
        delay(1000) // 这是一个内置的挂起函数。它会暂停协程1秒，但不会阻塞线程 A。
        // 此时，线程 A 可以去干别的事。
        println("数据获取完成！") // 1秒后，协程可能在线程 A、B 或 C 上恢复执行
        return "这是服务器返回的数据"
    }

    suspend fun getToken(): String {
        delay(1)
        Log.d("aaa", "getToken 开始执行，时间:  ${System.currentTimeMillis()}")
        return "ask"
    }

    suspend fun getResponse(token: String): String {
        delay(2)
        Log.d("aaa", "getResponse 开始执行，时间:  ${System.currentTimeMillis()}")
        return "response"
    }

    fun setText(response: String) {
        Log.d("aaa", "setText 执行，时间:  ${System.currentTimeMillis()}")
    }
    // lambda
    fun test(a : Int , b : (num1 : Int , num2 : Int) -> Int) : Int{
        return a + b.invoke(3,5)
    }
    fun test3() {
        Log.i("aaa","结果：${test(10,{ num1: Int, num2: Int ->  num1 + num2 })}")
        Log.i("aaa","结果：${test(10,{ num1: Int, num2: Int ->  num1 * num2 })}")
        Log.d("aaa", "test2开始执行")
        GlobalScope.launch(Dispatchers.IO) {
            String()
            Log.d("aaa", "协程开始执行，时间：${System.currentTimeMillis()}")

            val token = getToken()
            val response = getResponse(token)

            setText(response)
        }

        for (i in 1..10) {
            Log.d("aaa", "主线程打印第$i 次，时间：${System.currentTimeMillis()}")
        }
    }
}

