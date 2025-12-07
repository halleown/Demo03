package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.min

class TShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "TShapeView"

    private val paint = Paint().apply {
        color = 0xFF333333.toInt() // 深灰色
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        // 使用不同的比例来充分利用宽高
        // 竖线使用高度的100%
        val verticalSize = height * 1
        // 横线使用宽度的80%
        val horizontalSize = width * 0.8f

        Log.d(TAG, "onDraw: ${width}-----${height}")

        // 根据实际绘制尺寸动态设置虚线参数
        // 使用较小的尺寸来计算虚线，保持一致性
        val minSize = min(verticalSize, horizontalSize)
        val dashLength = minSize / 4f
        val gapLength = dashLength / 2f
        paint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)

        // 绘制竖线（中间，上下延伸）- 使用高度来填充
        val verticalStartX = centerX - horizontalSize / 2  // 竖线起点X与横线起点对齐
        val verticalStartY = centerY - verticalSize / 2
        val verticalEndX = centerX - horizontalSize / 2
        val verticalEndY = centerY + verticalSize / 2
        canvas.drawLine(verticalStartX, verticalStartY, verticalEndX, verticalEndY, paint)

        // 绘制横线（从竖线中间向右延伸）- 使用宽度来填充
        val horizontalStartX = centerX - horizontalSize / 2 + 10
        val horizontalStartY = centerY - 5
        val horizontalEndX = centerX + horizontalSize / 2
        val horizontalEndY = centerY - 5
        canvas.drawLine(horizontalStartX, horizontalStartY, horizontalEndX, horizontalEndY, paint)
    }
}