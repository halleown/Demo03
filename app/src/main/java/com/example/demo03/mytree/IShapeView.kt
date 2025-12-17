package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.compose.ui.text.TextPainter.paint
import com.example.demo03.R

class IShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "IShapeView"

    private val mPaint = Paint().apply {
       // color = "#9A9A9A".toColorInt()
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

    init {
        // 避免硬件加速吞掉虚线
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        dashLength = resources.getDimension(R.dimen._10dp)
        gapLength = resources.getDimension(R.dimen._4dp)
        dashGapCycle = dashLength + gapLength
        mPaint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    }

    /**
     * 计算在指定长度下，最后一个虚线段（dash + gap）的长度
     * @param lineLength 线的总长度
     * @return 最后一个完整周期（dash + gap）的长度，如果线长度不足以包含一个完整周期则返回实际剩余长度
     */
    private fun getLastDashGapLength(lineLength: Float): Float {
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

    /**
     * 获取最后一个完整周期 剩余部分长度
     */
    fun getRemainLength(): Float {
        return dashGapCycle - getLastDashGapLength(height.toFloat())
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

        // Log.d(TAG, "onDraw: ${width}-----${height}")

        // val minSize = min(verticalSize, horizontalSize)
        // val dashLength = minSize / 4f
        // val gapLength = dashLength / 2f
        // paint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)

//        val verticalStartX = centerX - horizontalSize / 2  // 竖线起点X与横线起点对齐
//        val verticalStartY = centerY - verticalSize / 2
//        val verticalEndX = centerX - horizontalSize / 2
//        val verticalEndY = centerY + verticalSize / 2

        // 计算竖线的长度
        val verticalLineLength = verticalSize

        // 获取最后一个虚线段（dash + gap）的长度
        val lastDashGapLength = getLastDashGapLength(verticalLineLength)

        // todo 需要将最后一个虚线段的长度传给adapter，以便下一个item可以偏移相应的距离
        //  但是adapter 设置偏移后，但是实际偏移无效果

        Log.d(TAG, "竖线总长度: $verticalLineLength")
        Log.d(TAG, "一个周期长度(dash+gap): $dashGapCycle")
        Log.d(TAG, "最后一个虚线段长度: $lastDashGapLength")

        canvas.drawLine(mPaint.strokeWidth * 0.5f, 0f, mPaint.strokeWidth * 0.5f, height, mPaint)
    }
}