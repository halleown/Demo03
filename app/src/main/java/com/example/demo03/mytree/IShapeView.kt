package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.demo03.R

class IShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint = Paint().apply {
//        color = Color.BLACK
        color = Color.GREEN
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
    }

    // 虚线点长度
    private val dashLength: Float
    // 间隔长度
    private val gapLength: Float
    // 一个完整周期（dashLength + gapLength）的长度
    private val dashGapCycle: Float

    // 保存当前虚线起点偏移（phase），用于和上一条虚线“接上”
    private var dashPhase: Float = 0f

    private val intervals: FloatArray

    init {
        // 避免硬件加速吞掉虚线
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        dashLength = resources.getDimension(R.dimen._10dp)
        gapLength = resources.getDimension(R.dimen._4dp)
        dashGapCycle = dashLength + gapLength
        intervals = floatArrayOf(dashLength, gapLength)
        updatePathEffect()
    }

    private fun updatePathEffect() {
        mPaint.pathEffect = DashPathEffect(intervals, dashPhase)
    }

    /**
     * 由外部设置 phase，使当前虚线从指定偏移位置开始绘制
     */
    fun setDashPhase(phase: Float) {
        dashPhase = phase
        updatePathEffect()
        // 重新触发绘制，确保 phase 生效
        invalidate()
    }

    // /**
    //  * 计算在指定长度下，最后一个虚线段（dash + gap）的长度
    //  * @param lineLength 线的总长度
    //  * @return 最后一个完整周期（dash + gap）的长度，如果线长度不足以包含一个完整周期则返回实际剩余长度
    //  */
    // private fun getLastDashGapLength(lineLength: Float): Float {
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

    // /**
    //  * 获取最后一个完整周期 剩余部分长度
    //  */
    // fun getRemainLength(): Float {
    //     return dashGapCycle - getLastDashGapLength(height.toFloat())
    // }

    /**
     * 计算"下一个 View"应该使用的 phase。
     *
     * 目标：让"上一个 View 的最后一个虚线周期"和"下一个 View 的第一个虚线周期"
     * 合并成一个完整周期。
     *
     * 考虑当前 view 已经应用的 dashPhase，计算画完 lineLength 后在周期中的位置。
     * - 如果最后一个 dash 没画完（remainder < dashLength），返回 remainder，让下一个 view 补齐 dash
     * - 如果最后一个 dash 画完了（remainder >= dashLength），返回 0，让下一个 view 跳过 gap，开始新的 dash
     */
    fun getNextViewPhase(): Float {
        if (dashGapCycle <= 0f) return 0f
        val lineLength = height.toFloat()
        if (lineLength <= 0f) return 0f
        // 考虑当前 dashPhase，计算画完 lineLength 后在周期中的位置
        val positionInCycle = (dashPhase + lineLength) % dashGapCycle
        // 如果位置在 dash 部分（< dashLength），说明最后一个 dash 没画完，返回这个位置让下一个 view 补齐
        // 如果位置在 gap 部分（>= dashLength），说明最后一个 dash 画完了，返回 0 让下一个 view 跳过 gap
        return if (positionInCycle < dashLength) {
            positionInCycle
        } else {
            0f
        }
    }

    /**
     * 计算在指定长度下，最后一个虚线段（dash + gap）的长度
     * @param lineLength 线的总长度
     * @return 最后一个完整周期（dash + gap）的长度，如果线长度不足以包含一个完整周期则返回实际剩余长度
     */
    fun getLastDashGapLength(): Float {
        val lineLength = height.toFloat()
        if (lineLength <= 0f || dashGapCycle <= 0f) return 0f

        // 计算能容纳多少个完整周期
        val fullCycles = (lineLength / dashGapCycle).toInt()
        // 剩余长度
        val remainder = lineLength % dashGapCycle

        // 如果剩余长度 >= dash，说明最后一个周期是完整的（dash + gap）
        // 如果剩余长度 < dash，说明最后一个周期不完整，只有部分 dash
        return if (remainder >= dashLength) {
            dashGapCycle // 最后一个完整周期
        } else if (remainder > 0f) {
            remainder // 只有部分 dash，没有 gap
        } else {
            dashGapCycle // 正好是完整周期
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        canvas.drawLine(mPaint.strokeWidth * 0.5f, 0f, mPaint.strokeWidth * 0.5f, height, mPaint)
    }
}