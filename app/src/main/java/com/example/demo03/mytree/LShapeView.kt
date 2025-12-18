package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
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
        invalidate()
    }

    // /**
    //  * 计算"给下一个 View 使用的 phase"，LShape 的竖线只占高度的一半
    //  */
    // fun getNextViewPhase(): Float {
    //     if (dashGapCycle <= 0f) return 0f
    //     val lineLength = (height * 0.5f)
    //     if (lineLength <= 0f) return 0f
    //     // 考虑当前 dashPhase，计算画完 lineLength 后在周期中的位置
    //     val positionInCycle = (dashPhase + lineLength) % dashGapCycle
    //     // 如果位置在 dash 部分（< dashLength），说明最后一个 dash 没画完，返回这个位置让下一个 view 补齐
    //     // 如果位置在 gap 部分（>= dashLength），说明最后一个 dash 画完了，返回 0 让下一个 view 跳过 gap
    //     return if (positionInCycle < dashLength) {
    //         positionInCycle
    //     } else {
    //         0f
    //     }
    // }

    // /**
    //  * 计算在指定长度下，最后一个虚线段（dash + gap）的长度
    //  * @param lineLength 线的总长度
    //  * @return 最后一个完整周期（dash + gap）的长度，如果线长度不足以包含一个完整周期则返回实际剩余长度
    //  */
    // fun getLastDashGapLength(): Float {
    //     val lineLength = height.toFloat()
    //     if (lineLength <= 0f || dashGapCycle <= 0f) return 0f
    //
    //     // 计算能容纳多少个完整周期
    //     val fullCycles = (lineLength / dashGapCycle).toInt()
    //     // 剩余长度
    //     val remainder = lineLength % dashGapCycle
    //
    //     // 如果剩余长度 >= dash，说明最后一个周期是完整的（dash + gap）
    //     // 如果剩余长度 < dash，说明最后一个周期不完整，只有部分 dash
    //     return if (remainder >= dashLength) {
    //         dashGapCycle // 最后一个完整周期
    //     } else if (remainder > 0f) {
    //         remainder // 只有部分 dash，没有 gap
    //     } else {
    //         dashGapCycle // 正好是完整周期
    //     }
    // }

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