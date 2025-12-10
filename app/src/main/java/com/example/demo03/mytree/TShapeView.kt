package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.toColorInt
import com.example.demo03.R
import kotlin.math.min

class TShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "TShapeView"

    private val paint = Paint().apply {
       // color = "#9A9A9A".toColorInt()
        color = Color.BLUE
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
    }

    init {
        // 避免硬件加速吞掉虚线
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        // 实线段长度
        val dash = resources.getDimension(R.dimen._10dp)
        // 虚线段长度
        val gap = resources.getDimension(R.dimen._3dp)
        paint.pathEffect = DashPathEffect(floatArrayOf(dash, gap), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        // 竖线使用高度的100%
        val verticalSize = height * 1
        // 横线使用宽度的80%
        val horizontalSize = width * 0.8f

        Log.d(TAG, "onDraw: ${width}-----${height}")

        // val minSize = min(verticalSize, horizontalSize)
        // val dashLength = minSize / 4f
        // val gapLength = dashLength / 2f
        // paint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)

        // 绘制竖线（中间，上下延伸）- 使用高度来填充
        val verticalStartX = centerX - horizontalSize / 2  // 竖线起点X与横线起点对齐
        val verticalStartY = centerY - verticalSize / 2
        val verticalEndX = centerX - horizontalSize / 2
        val verticalEndY = centerY + verticalSize / 2
        canvas.drawLine(verticalStartX, verticalStartY, verticalEndX, verticalEndY, paint)

        // 绘制横线（从竖线中间向右延伸）- 使用宽度来填充
        val horizontalStartX = centerX - horizontalSize / 2 + context.resources.getDimension(R.dimen._3dp)
        val horizontalStartY = centerY
        val horizontalEndX = centerX + horizontalSize / 2
        val horizontalEndY = centerY
        canvas.drawLine(horizontalStartX, horizontalStartY, horizontalEndX, horizontalEndY, paint)
    }
}