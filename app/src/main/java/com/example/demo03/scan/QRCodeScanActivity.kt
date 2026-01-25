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
    private var analyzer: Analyzer<Result?>? = null

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
                myView.post {
                    updateScanArea()
                }
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

    /**
     * 解决rk3568等设备上字体缩放异常的问题
     *
     * 问题原因：
     * 1. rk3568平板等设备系统字体缩放（fontScale）可能设置较大（>1.0）
     * 2. zxing-lite库的BarcodeCameraScanActivity可能修改了Configuration但没有正确处理fontScale
     * 3. 导致使用sp单位的文字（如@dimen/_24sp）被异常放大显示
     * 4. Pixel手机等设备可能fontScale为1.0，所以显示正常
     *
     * 安全性说明：
     * ✅ 此修复只影响UI显示，完全不影响扫码功能：
     *    - TextView只是显示"扫描二维码"标题，纯UI元素
     *    - ViewfinderView的labelTextSize只用于绘制提示文本，不影响扫码识别逻辑
     *    - 扫码核心功能（CameraScan、Analyzer、图像识别）完全独立，不受字体大小影响
     *    - 扫描框位置、识别区域等关键参数都不受影响
     *
     * 解决方案：
     * 在onResume中手动修复字体大小，使用applicationContext的Resources
     * 来获取不受Activity Configuration影响的尺寸值
     */
    override fun onResume() {
        super.onResume()
        // 使用applicationContext的Resources获取正确的尺寸（不受Activity fontScale影响）
        val appResources = applicationContext.resources
        val correctTextSizePx = appResources.getDimension(R.dimen._24sp)

        // 修复TextView的字体大小（顶部标题栏）
        tvScanQR.setTextSize(TypedValue.COMPLEX_UNIT_PX, correctTextSizePx)

        // 修复ViewfinderView的提示文本字体大小（扫描框下方的提示文字）
        // 注意：这只是UI显示，不影响扫码识别功能
        // 如果担心影响，可以注释掉下面这行，只修复TextView
        myView.setLabelTextSize(correctTextSizePx)
    }

    /**
     * 替代方案：通过override getResources()来重置fontScale
     *
     * 如果上面的onResume方案不够用，可以取消注释此方法
     * 注意：此方法会重置整个Configuration，可能影响其他功能
     *
     * 在Android 11 (API 30)上，updateConfiguration虽然已标记为废弃，但仍可使用
     * 在Android 13+ (API 33+)上，此方法可能无效，建议使用方案1
     */
    // override fun getResources(): Resources {
    //     val res = super.getResources()
    //     val config = Configuration()
    //     config.setToDefaults() // 恢复默认配置（包括fontScale = 1.0）
    //     val appMetrics = applicationContext.resources.displayMetrics
    //     res.updateConfiguration(config, appMetrics)
    //     return res
    // }


    override fun createAnalyzer(): Analyzer<Result?>? {
        // 延迟创建Analyzer，等ViewfinderView的frame确定后再创建
        // 如果analyzer已创建，直接返回
        if (analyzer != null) {
            return analyzer
        }

        // 初始化解码配置（使用临时值，后续会在updateScanArea中更新）
        val decodeConfig = DecodeConfig()
        // 支持默认的条码格式(包括二维码)
        decodeConfig.setHints(DecodeFormatManager.ALL_HINTS)
            .setFullAreaScan(false) // 设置是否全区域识别，默认false
            .setAreaRectRatio(0.625f) // 临时值，会在updateScanArea中更新为精确值
            .setAreaRectVerticalOffset(0) // 临时值，会在updateScanArea中更新为精确值
            .setAreaRectHorizontalOffset(0) // 临时值，会在updateScanArea中更新为精确值
        // BarcodeCameraScanActivity默认使用的MultiFormatAnalyzer
        analyzer = MultiFormatAnalyzer(decodeConfig)
        return analyzer
    }

    /**
     * 更新扫码识别区域，使其精确匹配ViewfinderView的扫描框
     * 
     * 原理：
     * 1. 获取ViewfinderView的扫描框位置（frame RectF，相对于ViewfinderView的坐标）
     * 2. 将ViewfinderView的frame坐标转换为PreviewView坐标系
     * 3. 计算frame相对于PreviewView的比例和偏移
     * 4. 根据比例和偏移量设置DecodeConfig的识别区域
     * 
     * 注意：此方法需要在ViewfinderView和PreviewView都完成布局（onLayout）后调用
     */
    private fun updateScanArea() {
        try {
            // 获取ViewfinderView的扫描框位置（相对于ViewfinderView的坐标）
            val frameRect = myView.getFrameRect() ?: run {
                Log.w("QRCodeScan", "ViewfinderView frame未初始化，延迟更新识别区域")
                // 如果frame还未初始化，延迟执行
                myView.post { updateScanArea() }
                return
            }

            // 获取ViewfinderView和PreviewView的尺寸和位置
            val viewfinderWidth = myView.width.toFloat()
            val viewfinderHeight = myView.height.toFloat()
            val previewWidth = previewView.width.toFloat()
            val previewHeight = previewView.height.toFloat()

            if (viewfinderWidth <= 0 || viewfinderHeight <= 0 || previewWidth <= 0 || previewHeight <= 0) {
                Log.w("QRCodeScan", "View尺寸无效，延迟更新识别区域")
                previewView.post { updateScanArea() }
                return
            }

            // 获取ViewfinderView在屏幕上的位置（相对于父容器）
            val viewfinderLocation = IntArray(2)
            myView.getLocationInWindow(viewfinderLocation)
            val viewfinderLeft = viewfinderLocation[0].toFloat()
            val viewfinderTop = viewfinderLocation[1].toFloat()

            // 获取PreviewView在屏幕上的位置（相对于父容器）
            val previewLocation = IntArray(2)
            previewView.getLocationInWindow(previewLocation)
            val previewLeft = previewLocation[0].toFloat()
            val previewTop = previewLocation[1].toFloat()

            // 将ViewfinderView的frame坐标转换为PreviewView坐标系
            val frameLeftInPreview = frameRect.left + viewfinderLeft - previewLeft
            val frameTopInPreview = frameRect.top + viewfinderTop - previewTop
            val frameWidth = frameRect.width()
            val frameHeight = frameRect.height()

            // 计算比例（相对于PreviewView的宽度和高度）
            val ratioWidth = frameWidth / previewWidth
            val ratioHeight = frameHeight / previewHeight

            Log.d("QRCodeScan", "比例计算: ratioWidth=$ratioWidth, ratioHeight=$ratioHeight")

            // zxing-lite的setAreaRectRatio是相对于预览区域的比例
            // 使用maxOf确保识别区域足够覆盖整个扫描框（避免因比例问题导致裁剪）
            var ratio = maxOf(ratioWidth, ratioHeight)
            
            // 如果比例太小（小于0.1），可能是计算错误，使用默认值
            if (ratio < 0.1f) {
                Log.w("QRCodeScan", "识别区域比例过小($ratio)，可能存在计算错误，使用默认值0.625")
                return
            }
            
            // 计算偏移量
            val frameCenterX = frameLeftInPreview + frameWidth / 2f
            val frameCenterY = frameTopInPreview + frameHeight / 2f
            val previewCenterX = previewWidth / 2f
            val previewCenterY = previewHeight / 2f

            val horizontalOffset = (frameCenterX - previewCenterX) / previewWidth
            val verticalOffset = (frameCenterY - previewCenterY) / previewHeight

            // 重新创建Analyzer with精确的识别区域
            val decodeConfig = DecodeConfig()
            decodeConfig.setHints(DecodeFormatManager.ALL_HINTS)
                .setFullAreaScan(false)
                .setAreaRectRatio(ratio) 
            
            val hOffsetInt = horizontalOffset.toInt()
            val vOffsetInt = verticalOffset.toInt()
            decodeConfig.setAreaRectHorizontalOffset(hOffsetInt)
            decodeConfig.setAreaRectVerticalOffset(vOffsetInt)
            
            analyzer = MultiFormatAnalyzer(decodeConfig)
            
            // 更新CameraScan的Analyzer
            getCameraScan().setAnalyzer(analyzer)
            
            Log.d("QRCodeScan", "识别区域已更新，精确匹配扫描框（${frameWidth.toInt()}x${frameHeight.toInt()}）")
        } catch (e: Exception) {
            Log.e("QRCodeScan", "更新识别区域失败", e)
        }
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
//        getCameraScan().setAnalyzeImage(false)

        Log.d("xialj", "二维码类型: " + result.getResult().getBarcodeFormat())
        if (result.result != null) {
            Log.d("xialj", "onScanResultCallback: " + result.result.text)
        }

        val intent = Intent()
        intent.putExtra(CameraScan.SCAN_RESULT, result.getResult().getText())
        setResult(RESULT_OK, intent)
//        finish()
    }
}