package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet

class IShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseDashShapeView(context, attrs, defStyleAttr) {

    override val dashColor: Int
        get() = Color.GREEN

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val height = height.toFloat()
        val dashWidth = paint.strokeWidth

        canvas.drawLine(dashWidth / 2f, 0f, dashWidth / 2f, height, paint)
    }
}