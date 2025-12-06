package com.example.demo03.mytree

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import com.example.demo03.R

class VerticalScrollBarView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var scroll_width = 0f
    private var radius_width = 0f

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalScrollBarView)
        scroll_width = ta.getDimension(R.styleable.VerticalScrollBarView_scroll_width, 0f)
        radius_width = ta.getDimension(R.styleable.VerticalScrollBarView_radius_width, 0f)
    }

    // Paint for drawing the scrollbar track and thumb
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#acacac")  // scrollbar track color
    }


    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            color =
//                context.getColor(com.obdstar.common.ui.R.color.colorPrimary)
//        }else {
//
//        } // scrollbar thumb color
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private var scrollPercentage: Float = 0f  // Current scroll percentage (0 to 1)
    private var thumbHeightPercentage: Float = 0.2f  // Height of thumb as a percentage of the view's height
    private var isThumbVisible = false  // Visibility state of the thumb

    init {
        visibility = INVISIBLE  // Initialize as invisible
    }

    // 设置滚动位置的百分比
    fun setScrollPercentage(percentage: Float) {
        scrollPercentage = percentage.coerceIn(0f, 1f)  // Keep percentage between 0 and 1
        invalidate()  // Refresh the view
    }

    // 设置滑块的可见性
    fun showThumb(visible: Boolean) {
        isThumbVisible = visible
        visibility = if (visible) VISIBLE else INVISIBLE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate dimensions
        val width = scroll_width  // Width of scrollbar from XML, adjust as needed
        val cornerRadius = radius_width  // Corner radius from XML
        val thumbHeight = height * thumbHeightPercentage  // Thumb height relative to view height
        val thumbTop = scrollPercentage * (height - thumbHeight)

        // Draw scrollbar track
        val trackRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(trackRect, cornerRadius.toFloat(), cornerRadius.toFloat(), trackPaint)

        // Draw scrollbar thumb only when visible
        if (isThumbVisible) {
            val thumbRect = RectF(0f, thumbTop, width.toFloat(), thumbTop + thumbHeight)
            canvas.drawRoundRect(thumbRect, cornerRadius.toFloat(), cornerRadius.toFloat(), thumbPaint)
        }
    }

    // Helper function to convert dp to pixels
    private fun dpToPx(dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}