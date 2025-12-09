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

class IShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "TShapeView"

    private val paint = Paint().apply {
       // color = "#9A9A9A".toColorInt()
        color = Color.BLACK
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
        style = Paint.Style.STROKE
    }

    private var dashLength: Float = 0f
    private var gapLength: Float = 0f

    init {
        // 实线段长度
        dashLength = 10f
        // dashLength = resources.getDimension(R.dimen._10dp)
        // 虚线段长度
        gapLength = 3f
        // gapLength = resources.getDimension(R.dimen._3dp)
        paint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
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

        // Log.d(TAG, "onDraw: ${width}-----${height}")

        // val minSize = min(verticalSize, horizontalSize)
        // val dashLength = minSize / 4f
        // val gapLength = dashLength / 2f
        // paint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)

        val verticalStartX = centerX - horizontalSize / 2  // 竖线起点X与横线起点对齐
        val verticalStartY = centerY - verticalSize / 2
        val verticalEndX = centerX - horizontalSize / 2
        val verticalEndY = centerY + verticalSize / 2
        canvas.drawLine(verticalStartX, verticalStartY, verticalEndX, verticalEndY, paint)
    }
}