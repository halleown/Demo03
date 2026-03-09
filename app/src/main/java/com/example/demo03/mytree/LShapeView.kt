package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
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

        val radius = width * 0.5f // 圆弧半径
        // 绘制只占高度一半的竖线
        canvas.drawLine(dashWidth / 2f, 0f, dashWidth / 2f, centerY - radius, paint)

        // 绘制圆弧
        val rectF = RectF(
            dashWidth / 2f,             // left
            centerY - 2 * radius,       // top
            dashWidth / 2f + 2 * radius, // right
            centerY                     // bottom: 确保圆弧终点在 centerY
        )

        // 从 180度(左边) 开始顺时针画 90度，终点就会落在正方形底部的中间(即 centerY)
        // 如果想要平滑连接竖线，起始点应该是 180度
        canvas.drawArc(rectF, 180f, -90f, false, horizontalDashPaint)
    }
}