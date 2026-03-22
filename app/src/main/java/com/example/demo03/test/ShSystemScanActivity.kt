package com.example.demo03.test

import android.widget.Toast
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.concurrent.thread

/**
 * 假设这个是系统扫描组件
 */
class ShSystemScanActivity {


    /*
 收到这样的消息，且SaveData值为true，则生成文件夹及里面的json文件
{
	"MsgType": 3,
	"ChildType": 4,
	"SaveData": true
}
     */
    fun saveToJson() {
        var str = displayHandle.string
        var bean = Gson().toJson(str, DiagReportListBean::class)
        val manager = DiagManager(this)

        thread {
            val createTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            bean.vinName = "VIN${(1000..9999).random()}"
            bean.createTime = createTime
//            bean.systemItems = listOf(SysItem("系统正常", "P0000"))

            // 调用保存，并传入随机生成的时间戳
            manager.saveBean(bean, randomTimeMillis)

            runOnUiThread {
                Toast.makeText(this, "保存json文件成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

}