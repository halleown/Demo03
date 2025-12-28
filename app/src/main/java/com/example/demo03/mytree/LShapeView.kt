package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import com.example.demo03.R
import kotlin.div


class LShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseDashShapeView(context, attrs, defStyleAttr) {

//    override val dashColor: Int
//        get() = Color.RED

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
        val dashWidth = paint.strokeWidth
        val centerY = height / 2f + context.resources.getDimension(R.dimen._4dp) - dashWidth / 2f

        // 绘制只占高度一半的竖线
        canvas.drawLine(dashWidth / 2f, 0f, dashWidth / 2f, centerY, paint)

        // 绘制横线
        canvas.drawLine(0f, centerY, width, centerY, horizontalDashPaint)
    }
}