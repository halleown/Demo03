package com.example.demo03.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.example.demo03.R

class StepChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * X轴最小值
     */
    private var xMin: Float = 0f

    /**
     * X轴最大值
     */
    private var xMax: Float = 0f

    /**
     * X轴单位文本
     */
    private var xUnit: String = "(X)"

    /**
     * Y轴最小值
     */
    private var yMin: Float = 0f

    /**
     * Y轴最大值
     */
    private var yMax: Float = 0f

    /**
     * Y轴单位文本
     */
    private var yUnit: String = "(Y)"

    /**
     * X轴刻度
     */
    private var xScales = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)

    /**
     * Y轴单位刻度
     */
    private var yScales = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f)

    /**
     * 折线点 (修改为匹配原图的大致阶梯路径)
     */
    private var dataPoints = emptyList<ChartValItem>()

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
        textAlign = Paint.Align.CENTER
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
    private val chartPaddingLeft = context.resources.getDimension(R.dimen._94dp)
    private val chartPaddingRight = context.resources.getDimension(R.dimen._30dp)
    private val chartPaddingTop = context.resources.getDimension(R.dimen._40dp)
    private val chartPaddingBottom = context.resources.getDimension(R.dimen._40dp)

    init {
        // 避免硬件加速吞掉虚线
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setChartData(
        xUnit: String,
        xMin: Float,
        xMax: Float,
        yUnit: String,
        yMin: Float,
        yMax: Float,
        points: List<ChartValItem>
    ) {
        this.xUnit = xUnit
        this.xMin = xMin
        this.xMax = xMax
        this.yUnit = yUnit
        this.yMin = yMin
        this.yMax = yMax

        val xInterval: Float = ((xMax - xMin) * 1.0f / (xScales.size - 1))
        for (i in 0 until xScales.size) {
            when (i) {
                0 -> xScales[i] = xMin
                xScales.size - 1 -> xScales[i] = xMax
                else -> xScales[i] = xMin + i * xInterval
            }
        }

        val yInterval: Float = ((yMax - yMin) * 1.0f / (yScales.size - 1))
        for (i in 0 until yScales.size) {
            when (i) {
                0 -> yScales[i] = yMin
                yScales.size - 1 -> yScales[i] = yMax
                else -> yScales[i] = yMin + i * yInterval
            }
        }

        setStepPathData(points)
    }

    /**
     * 设置折线图数据值
     */
    fun setStepPathData(points: List<ChartValItem>) {
        this.dataPoints = points
        requestLayout()
        invalidate()
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
        if (xScales.isEmpty() || yScales.isEmpty()) {
            return
        }
        val width = width - chartPaddingLeft - chartPaddingRight
        val height = height - chartPaddingTop - chartPaddingBottom

        // 绘制纵向虚线和X轴文字
        for (i in 0 until xScales.size) {
            val x = chartPaddingLeft + i * (width / xScales.size - 1)
            // 虚线
            if (i > 0) {
                canvas.drawLine(x, chartPaddingTop, x, chartPaddingTop + height, gridPaint)
            }
            // 绘制文字
            canvas.drawText(
                xScales[i].toString(),
                x,
                chartPaddingTop + height + context.resources.getDimension(R.dimen._28dp),
                xTextPaint
            )
        }
        val lastXLinePos = chartPaddingLeft + (xScales.size - 1) * (width / (xScales.size - 1))

        // 绘制最后一根纵轴虚线
        canvas.drawLine(lastXLinePos, chartPaddingTop, lastXLinePos, chartPaddingTop + height, gridPaint)

        // X轴单位
        canvas.drawText(
            xUnit,
            lastXLinePos,
            chartPaddingTop + height + context.resources.getDimension(R.dimen._28dp),
            unitPaint
        )

        // 绘制横向虚线和Y轴文字
        for (i in 0 until yScales.size) {
            val y = chartPaddingTop + height - i * (height / (yScales.size - 1))
            // 虚线
            if (i > 0) {
                canvas.drawLine(chartPaddingLeft, y, chartPaddingLeft + width, y, gridPaint)
            }
            // 文字
            if (i < yScales.size) {
                canvas.drawText(
                    yScales[i].toString(),
                    chartPaddingLeft - context.resources.getDimension(R.dimen._20dp),
                    y + context.resources.getDimension(R.dimen._6dp),
                    yTextPaint
                )
            }
            if (i > 0) {
                // 刻度短横线（除了0刻度不画短横线）
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
            chartPaddingLeft,
            chartPaddingTop - context.resources.getDimension(R.dimen._14dp),
            unitPaint
        )

        // 绘制坐标轴主线
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
        val xRange = xMax - xMin
        val yRange = yMax - yMin

        for (i in dataPoints.indices) {
            val px = chartPaddingLeft + ((dataPoints[i].x - xMin) / xRange) * width
            val py = chartPaddingTop + height - ((dataPoints[i].y - yMin) / yRange) * height

            if (i == 0) {
                path.moveTo(px, py)
            } else {
                // 实现阶梯效果：先画到前一个点的Y值，当前点的X值
                val prevPx = chartPaddingLeft + ((dataPoints[i - 1].x - xMin) / xRange) * width
                val prevPy = chartPaddingTop + height - ((dataPoints[i - 1].y - yMin) / yRange) * height

                path.lineTo(px, prevPy) // 水平线
                path.lineTo(px, py)     // 垂直线
            }
        }
        canvas.drawPath(path, linePaint)
    }
}