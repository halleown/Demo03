package com.example.demo03.mytree

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.demo03.R

abstract class BaseDashShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 虚线点长度
    protected val dashLength: Float
    // 间隔长度
    protected val gapLength: Float
    // 虚线的一个完整周期
    protected val dashGapCycle: Float
    // 当前虚线起点的偏移距离，用于接上上一条view
    protected var mDashPhase: Float = 0f
    private val intervals: FloatArray

    protected val paint = Paint().apply {
        color = dashColor
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
    }

    init {
        // 避免硬件加速吞掉虚线
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        dashLength = resources.getDimension(R.dimen._10dp)
        gapLength = resources.getDimension(R.dimen._4dp)
        dashGapCycle = dashLength + gapLength
        intervals = floatArrayOf(dashLength, gapLength)
        updatePathEffect()
    }

    protected open val dashColor: Int
        get() = Color.BLACK


    private fun updatePathEffect() {
        paint.pathEffect = DashPathEffect(intervals, mDashPhase)
    }

    /**
     * 设置当前虚线需偏移的距离
     */
    fun setDashPhase(phase: Float) {
        mDashPhase = phase
        updatePathEffect()
        invalidate()
    }

    /**
     * 计算下一个View应该偏移的距离
     */
    fun getNextViewPhase(): Float {
        if (dashGapCycle <= 0f) return 0f
        val viewHeight = height.toFloat()
        if (viewHeight <= 0f) return 0f
        // 考虑当前 dashPhase，计算画完 lineLength 后在周期中的位置
        val positionInCycle = (mDashPhase + viewHeight) % dashGapCycle
        // 如果位置在 dash 部分（< dashLength），说明最后一个 dash 没画完，返回这个位置让下一个 view 补齐
        // 如果位置在 gap 部分（>= dashLength），说明最后一个 dash 画完了，返回 0 让下一个 view 跳过 gap，开始新的dash
//        return if (positionInCycle < dashLength) {
//            positionInCycle
//        } else {
//            0f
//        }
        return positionInCycle
    }
}

