package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.demo03.R

class LShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
//        color = Color.BLACK
        color = Color.RED
        // 虚线宽度
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
    }

    // dash / gap / 周期长度
    private val dashLength: Float
    private val gapLength: Float
    private val dashGapCycle: Float
    private val intervals: FloatArray
    // 当前虚线相位，用于和上一条线连接
    private var dashPhase: Float = 0f

    init {
        // 避免硬件加速吞掉虚线
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        // 实线段长度
        dashLength = resources.getDimension(R.dimen._10dp)
        // 虚线段长度
        gapLength = resources.getDimension(R.dimen._4dp)
        dashGapCycle = dashLength + gapLength
        intervals = floatArrayOf(dashLength, gapLength)
        updatePathEffect()
    }

    private fun updatePathEffect() {
        paint.pathEffect = DashPathEffect(intervals, dashPhase)
    }

    /**
     * 设置当前 View 的虚线相位，使它能和上一条线对齐
     */
    fun setDashPhase(phase: Float) {
        dashPhase = phase
        updatePathEffect()
        Log.d("xialj", "L___setDashPhase: ${phase}")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        canvas.drawLine(paint.strokeWidth  * 0.5f, 0f, paint.strokeWidth * 0.5f, height * 0.5f, paint)

        // 横线（在高度一半的位置，从竖线底部向右延伸）
        val horizontalStartX = context.resources.getDimension(R.dimen._3dp)// +10是为了离竖线远一些
        canvas.drawLine(horizontalStartX, height * 0.5f, width, height * 0.5f, paint)
    }
}