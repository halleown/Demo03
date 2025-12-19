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

class TShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
//        color = Color.BLACK
        color = Color.BLUE
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
        Log.d("xialj", "T___setDashPhase: ${phase}")
        updatePathEffect()
        invalidate()
    }

    /**
     * 计算"给下一个 View 使用的 phase"，根据当前竖线的长度和已应用的 dashPhase 进行推算
     */
    fun getNextViewPhase(): Float {
        if (dashGapCycle <= 0f) return 0f
        val lineLength = height.toFloat()
        if (lineLength <= 0f) return 0f
        // 考虑当前 dashPhase，计算画完 lineLength 后在周期中的位置
        val positionInCycle = (dashPhase + lineLength) % dashGapCycle
        // 如果位置在 dash 部分（< dashLength），说明最后一个 dash 没画完，返回这个位置让下一个 view 补齐
        // 如果位置在 gap 部分（>= dashLength），说明最后一个 dash 画完了，返回 0 让下一个 view 跳过 gap
        // return if (positionInCycle < dashLength) {
        //     positionInCycle
        // } else {
        //     0f
        // }
        val result = (dashPhase + height) % dashGapCycle
        Log.d("xialj", "T___getNextViewPhase: ${result}")
        return result
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

        canvas.drawLine(paint.strokeWidth  *0.5f, 0f, paint.strokeWidth * 0.5f, height, paint)

        // 绘制横线（从竖线中间向右延伸）- 使用宽度来填充
        val horizontalStartX = context.resources.getDimension(R.dimen._3dp)
        canvas.drawLine(horizontalStartX, centerY, horizontalStartX + width, centerY, paint)
    }
}