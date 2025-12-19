package com.example.demo03.dash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.demo03.R

class DashedLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
    }
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    var dashOnLength = context.resources.getDimension(R.dimen._10dp)

    var gapLength = context.resources.getDimension(R.dimen._4dp)

    private val dashCycle: Float
        get() = dashOnLength + gapLength

    // 偏移量：决定从周期的哪个点开始画
    var phaseOffset: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 这里的 phaseOffset 如果是 10，意味着从周期的第 10 像素开始画
        paint.pathEffect = DashPathEffect(floatArrayOf(dashOnLength, gapLength), phaseOffset)

        val x = width / 2f
        canvas.drawLine(x, 0f, x, height.toFloat(), paint)
    }

    /**
     * 计算下一个 View 应该使用的偏移量
     */
    fun getNextOffset(viewHeight: Int): Float {
        // (当前已有的偏移 + 当前消耗的高度) 对 周期 取模
        // 这代表了当前 View 结束时，正处于一个周期的哪个位置
        return (phaseOffset + viewHeight) % dashCycle
    }
}