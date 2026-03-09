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
import kotlin.text.toFloat

class TShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseDashShapeView(context, attrs, defStyleAttr) {

//    override val dashColor: Int
//        get() = Color.BLUE

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

        // 节点位置的纵坐标
        val centerY = height / 2f + context.resources.getDimension(R.dimen._4dp) - dashWidth / 2f

        // 1. 绘制贯穿全文的竖线 (保持不变)
        canvas.drawLine(dashWidth / 2f, 0f, dashWidth / 2f, height, paint)

        // 2. 绘制圆弧横线 (调整 RectF 以创造间距)

        // 半径
        val radius = width * 0.5f

        val arcRect = RectF(
            dashWidth / 2f,         // left: 竖线中心点
            centerY - radius * 2f,      // top: 保持不变
            dashWidth / 2f + radius * 2f, // right: 相应地也向右偏移 gap
            centerY                     // bottom: 确保终点仍在 centerY 且水平向右
        )

        // 从 180度(左侧) 开始逆时针向上划 90度
        canvas.drawArc(arcRect, 180f, -90f, false, horizontalDashPaint)
    }
}