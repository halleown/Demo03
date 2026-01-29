package com.example.demo03.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import com.king.view.viewfinderview.ViewfinderView

class CustomViewfinderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewfinderView(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val SCAN_PERIOD = 2000

    private fun <T> getPrivateField(name: String): T {
        val field = ViewfinderView::class.java.getDeclaredField(name)
        field.isAccessible = true
        return field.get(this) as T
    }

    private fun setPrivateField(name: String, value: Any?) {
        val field = ViewfinderView::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(this, value)
    }

    override fun onDraw(canvas: Canvas) {
        // 1. 获取所有原始私有属性
        val paint = getPrivateField<Paint>("paint")
        val frame = getPrivateField<RectF>("frame")
        var scannerStart = getPrivateField<Float>("scannerStart")
        val scannerEnd = getPrivateField<Float>("scannerEnd")
        val laserStyle = getPrivateField<Any>("laserStyle").toString()
        val laserColor = getPrivateField<Int>("laserColor")

        // 2. 严格同步补丁位置计算逻辑
        val step = frame.height() / (SCAN_PERIOD / 16.0f)
        if (scannerStart < scannerEnd) {
            scannerStart += step
        } else {
            scannerStart = frame.top
        }
        setPrivateField("scannerStart", scannerStart)

        // 3. 执行基类绘制（绘制背景阴影、四个角等）
        super.onDraw(canvas)

        // 4. 只有 GRID 模式下覆盖补丁逻辑
        if (laserStyle == "GRID") {
            val backgroundTop = frame.top

            // --- A. 修复背景渐变：确保 Shader 坐标与当前 scannerStart 实时对齐 ---
            // 补丁逻辑：从顶部到扫描线位置
            backgroundPaint.shader = LinearGradient(
                0f, backgroundTop,
                0f, scannerStart,
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#22FFFFFF")),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(frame.left, backgroundTop, frame.right, scannerStart, backgroundPaint)

            // --- B. 绘制网格线 ---
            val lineGradient = LinearGradient(
                0f, backgroundTop,
                0f, scannerStart,
                intArrayOf(shadeColor(laserColor), laserColor),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = lineGradient
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f // 强制极细网格

            val laserGridColumn = getPrivateField<Int>("laserGridColumn")
            val frameLineStrokeWidth = getPrivateField<Float>("frameLineStrokeWidth")
            val gridItemSize = frame.width() / laserGridColumn
            val laserGridHeight = gridItemSize * 4

            val gridPath = Path()
            val padding = frameLineStrokeWidth / 2f

            // 纵向线
            var x = frame.left + padding
            while (x <= frame.right - padding) {
                gridPath.moveTo(x, backgroundTop)
                gridPath.lineTo(x, scannerStart)
                x += gridItemSize
            }

            // 横向线
            val horizontalLineCount = (laserGridHeight / gridItemSize).toInt()
            for (i in 0..horizontalLineCount) {
                val y = scannerStart - i * gridItemSize
                if (y < backgroundTop) continue
                gridPath.moveTo(frame.left + padding, y)
                gridPath.lineTo(frame.right - padding, y)
            }
            canvas.drawPath(gridPath, paint)

            // --- C. 修复线太粗：重置 Paint 关键属性 ---
            paint.shader = null
            paint.color = Color.parseColor("#FF6D00")
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 0f // FILL 模式下 strokeWidth 必须为 0 否则在某些设备会加粗

            // 精确绘制 3px 高度的线矩形
            canvas.drawRect(
                frame.left + padding,
                scannerStart,
                frame.right - padding,
                scannerStart + 3f,
                paint
            )
        }
    }

    private fun shadeColor(color: Int): Int {
        val alpha = color ushr 24
        val remainder = color and 0x00FFFFFF
        return (alpha * 0.1).toInt() shl 24 or remainder
    }
}