package com.example.demo03.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import com.king.view.viewfinderview.ViewfinderView
import java.lang.reflect.Field

/**
 * 自定义扫描视图，增强网格扫描效果
 */
class CustomViewfinderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewfinderView(context, attrs, defStyleAttr) {

    private val customPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startTime = System.currentTimeMillis()

    // 动画配置：单位毫秒
    private val scanDuration = 2000L // 扫描时间
    private val pauseDuration = 1000L // 停顿时间
    private val totalDuration = scanDuration + pauseDuration

    // 反射父类私有变量
    private val frameField: Field? =
        ViewfinderView::class.java.getDeclaredField("frame").apply { isAccessible = true }
    private val scannerStartField: Field? =
        ViewfinderView::class.java.getDeclaredField("scannerStart").apply { isAccessible = true }
    private val laserColorField: Field? =
        ViewfinderView::class.java.getDeclaredField("laserColor").apply { isAccessible = true }
    private val frameLineStrokeWidthField: Field? =
        ViewfinderView::class.java.getDeclaredField("frameLineStrokeWidth")
            .apply { isAccessible = true }


    /**
     * 获取当前扫描框的 Rect 区域
     * 用于交给 CameraScan 设置识别范围
     */
    fun getFrameRect(): Rect? {
        val frameF = frameField?.get(this) as? RectF ?: return null
        return Rect(frameF.left.toInt(), frameF.top.toInt(), frameF.right.toInt(), frameF.bottom.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val frame = frameField?.get(this) as? RectF ?: return

        // 1. 计算时间进度
        val elapsed = (System.currentTimeMillis() - startTime) % totalDuration

        val newScannerStart: Float
        if (elapsed <= scanDuration) {
            // 扫描阶段：从 frame.top 移动到 frame.bottom
            val progress = elapsed.toFloat() / scanDuration
            newScannerStart = frame.top + (frame.height() * progress)
        } else {
            // 停顿阶段：固定在底部
            newScannerStart = frame.bottom
        }

        // 2. 将计算出的位置强制同步给父类的私有变量，这样父类的 drawGridScanner 就能画在正确位置
        scannerStartField?.set(this, newScannerStart)

        // 3. 执行父类绘制（绘制网格和原始线）
        super.onDraw(canvas)

        // 4. 绘制你要求的“加粗且无缝隙”的底部线条
        if (elapsed <= scanDuration) {
            drawBoldLastLine(canvas, frame, newScannerStart)
        }
    }

    private fun drawBoldLastLine(canvas: Canvas, frame: RectF, yPos: Float) {
        try {
            val laserColor = laserColorField?.get(this) as? Int ?: 0
            val frameLineStrokeWidth = frameLineStrokeWidthField?.get(this) as? Float ?: 0f

            // 切换为 FILL 模式，这样我们可以精确控制覆盖区域
            customPaint.style = Paint.Style.FILL
            customPaint.color = laserColor
            customPaint.shader = null // 确保不透明，消除空隙视觉感

            // 设置厚度
            val thickness =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics)
            val padding = frameLineStrokeWidth / 2f

            // 向上重叠 1 像素物理压住网格线末端，彻底消除缝隙
            val overlap = 1f
            canvas.drawRect(
                frame.left + padding,
                yPos - thickness + overlap,
                frame.right - padding,
                yPos + overlap,
                customPaint
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}