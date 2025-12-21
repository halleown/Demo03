package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import com.example.demo03.R

class TShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseDashShapeView(context, attrs, defStyleAttr) {

    override val dashColor: Int
        get() = Color.BLUE

    private val horizontalDashPaint = Paint().apply {
        color = dashColor
        strokeWidth = paint.strokeWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
        pathEffect = DashPathEffect(
            floatArrayOf(
                resources.getDimension(R.dimen._10dp),
                resources.getDimension(R.dimen._4dp)
            ),
            0f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2f
        val dashWidth = paint.strokeWidth

        // 绘制竖线
        canvas.drawLine(dashWidth / 2f, 0f, dashWidth / 2f, height, paint)

        // 绘制横线
        canvas.drawLine(dashWidth, centerY, dashWidth + width, centerY, horizontalDashPaint)
    }
}