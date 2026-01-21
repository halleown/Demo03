package com.example.demo03.graph

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.demo03.R

class StepChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * X轴最大值
     */
    private var xMax = 6000f

    /**
     * Y轴最大值
     */
    private var yMax = 125f

    /**
     * Y轴格数
     */
    private var yGridCount = 5

    /**
     * X轴格数
     */
    private var xGridCount = 7

    /**
     * X轴单位文本
     */
    private var xUnit = "(rpm)"

    /**
     * Y轴单位文本
     */
    private var yUnit = "(%)"

    /**
     * 横坐标线
     */
    private val xAxisPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#999999"); strokeWidth =
            context.resources.getDimension(R.dimen._3dp)
        }

    /**
     * 纵坐标线
     */
    private val yAxisPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6212A"); strokeWidth =
            context.resources.getDimension(R.dimen._3dp)
        }

    /**
     * 短横线画笔（纵坐标文本和纵轴之间的短横线）
     */
    private val tickPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6212A"); strokeWidth =
            context.resources.getDimension(R.dimen._1dp)
        }

    /**
     * X轴刻度
     */
    private var xLabels = emptyList<String>()

    /**
     * Y轴单位刻度
     */
    private var yLabels = emptyList<String>()

    /**
     * 折线点 (修改为匹配原图的大致阶梯路径)
     */
    private var dataPoints = emptyList<Pair<Float, Float>>()

    /**
     * 坐标系背景画笔（注意，横纵坐标的两条线可以不用画虚线）
     */
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen._1dp)
        pathEffect = DashPathEffect(
            floatArrayOf(
                context.resources.getDimension(R.dimen._10dp),
                context.resources.getDimension(R.dimen._6dp)
            ), 0f
        )
    }

    /**
     * 横坐标刻度文字画笔
     */
    private val xTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = context.resources.getDimension(R.dimen._18sp)
        textAlign = Paint.Align.CENTER
    }

    /**
     * 纵坐标刻度文字画笔
     */
    private val yTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6212A")
        textSize = context.resources.getDimension(R.dimen._18sp)
        textAlign = Paint.Align.RIGHT
    }

    /**
     * 单位画笔
     */
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = context.resources.getDimension(R.dimen._18sp)
    }

    /**
     * 折线画笔
     */
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = context.resources.getDimension(R.dimen._3dp)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#E41F19")
    }

    /**
     * 图表（网格）距左边的距离，为了给文字留出一定空间
     */
    private val chartPaddingLeft = context.resources.getDimension(R.dimen._64dp)
    private val chartPaddingRight = context.resources.getDimension(R.dimen._30dp)
    private val chartPaddingTop = context.resources.getDimension(R.dimen._40dp)
    private val chartPaddingBottom = context.resources.getDimension(R.dimen._40dp)


    init {
        // 避免硬件加速吞掉虚线
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackgroundAndAxes(canvas)
        drawStepPath(canvas)
    }

    /**
     * 绘制背景网格和坐标轴
     */
    fun drawBackgroundAndAxes(canvas: Canvas) {
        if (xLabels.isEmpty() || yLabels.isEmpty()) {
            return
        }
        val width = width - chartPaddingLeft - chartPaddingRight
        val height = height - chartPaddingTop - chartPaddingBottom

        // 1. 绘制纵向虚线和X轴文字
        for (i in 0..xGridCount) {
            val x = chartPaddingLeft + i * (width / xGridCount)
            // 虚线
            if (i > 0) {
                canvas.drawLine(x, chartPaddingTop, x, chartPaddingTop + height, gridPaint)
            }
            // 绘制文字（除横坐标最后一个文字不绘制，其余都绘制）
            if (i < xLabels.size - 1) {
                canvas.drawText(
                    xLabels[i],
                    x,
                    chartPaddingTop + height + context.resources.getDimension(R.dimen._28dp),
                    xTextPaint
                )
            }
        }

        // X轴单位
        canvas.drawText(
            xUnit,
            width + context.resources.getDimension(R.dimen._30dp),
            chartPaddingTop + height + context.resources.getDimension(R.dimen._24dp),
            unitPaint
        )

        // 2. 绘制横向虚线和Y轴文字
        for (i in 0..yGridCount) {
            val y = chartPaddingTop + height - i * (height / yGridCount)
            // 虚线
            if (i > 0) {
                canvas.drawLine(chartPaddingLeft, y, chartPaddingLeft + width, y, gridPaint)
            }
            // 文字
            if (i < yLabels.size) {
                canvas.drawText(
                    yLabels[i],
                    chartPaddingLeft - context.resources.getDimension(R.dimen._20dp),
                    y + context.resources.getDimension(R.dimen._6dp),
                    yTextPaint
                )
            }
            if (i > 0) {
                // 刻度短横线（0刻度不画短横线）
                canvas.drawLine(
                    chartPaddingLeft - context.resources.getDimension(R.dimen._14dp),
                    y,
                    chartPaddingLeft,
                    y,
                    tickPaint
                )
            }
        }
        // Y轴单位
        canvas.drawText(
            yUnit,
            chartPaddingLeft - context.resources.getDimension(R.dimen._8dp),
            chartPaddingTop - context.resources.getDimension(R.dimen._14dp),
            unitPaint
        )

        // 3. 绘制坐标轴主线
        canvas.drawLine(
            chartPaddingLeft,
            chartPaddingTop,
            chartPaddingLeft,
            chartPaddingTop + height,
            yAxisPaint
        )
        canvas.drawLine(
            chartPaddingLeft,
            chartPaddingTop + height,
            chartPaddingLeft + width,
            chartPaddingTop + height,
            xAxisPaint
        )
    }

    /**
     * 画折线
     */
    fun drawStepPath(canvas: Canvas) {
        if (dataPoints.isEmpty()) return

        val width = width - chartPaddingLeft - chartPaddingRight
        val height = height - chartPaddingTop - chartPaddingBottom

        val path = Path()

        for (i in dataPoints.indices) {
            val px = chartPaddingLeft + (dataPoints[i].first / xMax) * width
            val py = chartPaddingTop + height - (dataPoints[i].second / yMax) * height

            if (i == 0) {
                path.moveTo(px, py)
            } else {
                // 实现阶梯效果：先画到前一个点的Y值，当前点的X值
                val prevPx = chartPaddingLeft + (dataPoints[i - 1].first / xMax) * width
                val prevPy = chartPaddingTop + height - (dataPoints[i - 1].second / yMax) * height

                path.lineTo(px, prevPy) // 水平线
                path.lineTo(px, py)     // 垂直线
            }
        }
        canvas.drawPath(path, linePaint)
    }

    fun setChartData(
        points: List<Pair<Float, Float>>,
        xLabels: List<String>,
        yLabels: List<String>,
        xUnit: String,
        yUnit: String,
    ) {
        this.dataPoints = points
        this.xLabels = xLabels
        this.yLabels = yLabels
        this.xUnit = xUnit
        this.yUnit = yUnit
        requestLayout()
        invalidate()
    }
}