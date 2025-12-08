package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import androidx.core.graphics.toColorInt
import com.example.demo03.R

class LShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
//        color = "#9A9A9A".toColorInt()
        color = Color.BLACK
        // 虚线宽度
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2

        // L形只占高度的一半，绘制在上半部分
        val availableHeight = height / 2f  // 只使用上半部分的高度
//        val topY = height * 0.1f  // 从顶部10%的位置开始
        val topY = 0
        val bottomY = height * 0.5f  // 到高度50%的位置结束（下半部分空白）

        // 竖线使用可用高度的100%
        val verticalSize = availableHeight * 1f
        // 横线使用宽度的80%
        val horizontalSize = width * 0.8f

        // 根据实际绘制尺寸动态设置虚线参数
        val minSize = min(verticalSize, horizontalSize)
        val dashLength = minSize / 4f// 虚线长度
        val gapLength = dashLength / 2f// 虚线间隔
        paint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)

        // 绘制L字：竖线在左，横线在底部
        // 竖线（左侧，从上向下延伸，只在上半部分）
        val verticalStartX = centerX - horizontalSize / 2  // 竖线起点X与横线起点对齐
        val verticalStartY = topY
        val verticalEndX = centerX - horizontalSize / 2
        val verticalEndY = bottomY  // 竖线终点在高度一半的位置

        canvas.drawLine(verticalStartX, verticalStartY.toFloat(), verticalEndX, verticalEndY, paint)

        // 横线（在高度一半的位置，从竖线底部向右延伸）
        val horizontalStartX = centerX - horizontalSize / 2 + context.resources.getDimension(R.dimen._3dp)// +10是为了离竖线远一些
        val horizontalStartY = bottomY  // 横线在高度一半的位置
        val horizontalEndX = centerX + horizontalSize / 2
        val horizontalEndY = bottomY

        canvas.drawLine(horizontalStartX, horizontalStartY, horizontalEndX, horizontalEndY, paint)
    }
}