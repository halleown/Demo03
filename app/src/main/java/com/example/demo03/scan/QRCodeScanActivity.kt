package com.example.demo03.scan

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.camera.view.PreviewView
import com.example.demo03.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.king.camera.scan.AnalyzeResult
import com.king.camera.scan.CameraScan
import com.king.camera.scan.analyze.Analyzer
import com.king.zxing.BarcodeCameraScanActivity
import com.king.zxing.DecodeConfig
import com.king.zxing.DecodeFormatManager
import com.king.zxing.analyze.MultiFormatAnalyzer

class QRCodeScanActivity : BarcodeCameraScanActivity() {

    private lateinit var myView: ViewfinderView
    private lateinit var tvScanQR: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myView = findViewById(R.id.myView)

        val previewView = findViewById<PreviewView>(R.id.previewView)
        val loading = findViewById<ProgressBar>(R.id.pbCameraLoading)

        findViewById<View>(R.id.iv_exit).setOnClickListener {
//            it.isEnabled = false            // 简单防抖，避免多次点击
//            onBackPressedDispatcher.onBackPressed()
             finish()
        }

        // 初始：隐藏扫码 UI，只显示转圈
        previewView.visibility = View.INVISIBLE
        myView.visibility = View.INVISIBLE
        loading.visibility = View.VISIBLE

        // 相机预览开始输出帧（STREAMING）后再显示扫码页面
        previewView.previewStreamState.observe(this) { state ->
            if (state == PreviewView.StreamState.STREAMING) {
                loading.visibility = View.GONE
                previewView.visibility = View.VISIBLE
                myView.visibility = View.VISIBLE
            }
        }

        tvScanQR = findViewById<TextView>(R.id.tv_scan_qr)
    }

    // override fun getResources(): Resources {
    //     val res = super.getResources()
    //     val config = Configuration()
    //     config.setToDefaults() // 恢复默认配置
    //     val appMetrics = applicationContext.resources.displayMetrics
    //     res.updateConfiguration(config, appMetrics)
    //     return res
    // }

    // override fun onResume() {
    //     super.onResume()
    //     val normalPx = applicationContext.resources.getDimension(R.dimen._24sp)
    //     tvScanQR.setTextSize(TypedValue.COMPLEX_UNIT_PX, normalPx)
    //     Log.d("TextSizeLog1", "从全局Context获取的原始px: $normalPx")
    //     Log.d("TextSizeLog1", "DataManager 24px 对应此页面计算出的 px: $normalPx")
    //     Log.d("TextSizeLog1", "修复后 qrscan textSize px: ${tvScanQR.textSize}")
    // }

    override fun initCameraScan(cameraScan: CameraScan<Result?>) {
        super.initCameraScan(cameraScan)
        // 根据需要设置CameraScan相关配置
        cameraScan.setPlayBeep(true)
    }

    override fun createAnalyzer(): Analyzer<Result?>? {
        // 初始化解码配置
        val decodeConfig = DecodeConfig()
        decodeConfig.setHints(DecodeFormatManager.ALL_HINTS) // 如果只有识别二维码的需求，这样设置效率会更高，不设置默认为DecodeFormatManager.DEFAULT_HINTS
            .setFullAreaScan(false) // 设置是否全区域识别，默认false
            .setAreaRectRatio(0.35f) // 设置识别区域比例，默认0.8，设置的比例最终会在预览区域裁剪基于此比例的一个矩形进行扫码识别
            .setAreaRectVerticalOffset(0) // 设置识别区域垂直方向偏移量，默认为0，为0表示居中，可以为负数
            .setAreaRectHorizontalOffset(0) // 设置识别区域水平方向偏移量，默认为0，为0表示居中，可以为负数
        // BarcodeCameraScanActivity默认使用的MultiFormatAnalyzer，这里也可以改为使用QRCodeAnalyzer
        return MultiFormatAnalyzer(decodeConfig)
    }

    /**
     * 布局ID；通过覆写此方法可以自定义布局
     *
     * @return 布局ID
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_qrcode_scan
    }

    override fun onScanResultCallback(result: AnalyzeResult<Result?>) {
        // 停止分析
        getCameraScan().setAnalyzeImage(false)

        Log.d("xialj", "二维码类型: " + result.getResult().getBarcodeFormat())
        if (result.getResult().getBarcodeFormat() != BarcodeFormat.DATA_MATRIX) {
            Log.d("xialj", "二维码不匹配")
        } else {
            Log.d("xialj", "onScanResultCallback: " + result.getResult().getText())
        }

        val intent = Intent()
        intent.putExtra(CameraScan.SCAN_RESULT, result.getResult().getText())
        setResult(RESULT_OK, intent)
        finish()
    }
}