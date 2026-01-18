package com.example.demo03.scan

import android.util.Log
import com.example.demo03.R
import com.google.zxing.Result
import com.king.camera.scan.AnalyzeResult
import com.king.camera.scan.BaseCameraScanFragment
import com.king.camera.scan.analyze.Analyzer
import com.king.zxing.DecodeConfig
import com.king.zxing.DecodeFormatManager
import com.king.zxing.analyze.MultiFormatAnalyzer

class QRCodeScanFragment : BaseCameraScanFragment<Result>() {

    val TAG = "QRCodeScanFragment"

    override fun createAnalyzer(): Analyzer<Result?>? {
        //初始化解码配置
        val decodeConfig = DecodeConfig()
        decodeConfig.setHints(DecodeFormatManager.ALL_HINTS) //如果只有识别二维码的需求，这样设置效率会更高，不设置默认为DecodeFormatManager.DEFAULT_HINTS
            .setFullAreaScan(false) //设置是否全区域识别，默认false
            .setAreaRectRatio(0.35f) //设置识别区域比例，默认0.8，设置的比例最终会在预览区域裁剪基于此比例的一个矩形进行扫码识别
            .setAreaRectVerticalOffset(0) //设置识别区域垂直方向偏移量，默认为0，为0表示居中，可以为负数
            .setAreaRectHorizontalOffset(0) //设置识别区域水平方向偏移量，默认为0，为0表示居中，可以为负数

        // BarcodeCameraScanActivity默认使用的MultiFormatAnalyzer，这里也可以改为使用QRCodeAnalyzer
        return MultiFormatAnalyzer(decodeConfig)
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_qrcode_scan
    }

    override fun onScanResultCallback(result: AnalyzeResult<Result?>) {
        Log.e(TAG, "onScanResultCallback: ${result.result.text}")
    }
}