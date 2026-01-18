package com.example.demo03.graph

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class StepChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints: List<Pair<Float, Float>> = emptyList()

    private var maxY = 125f       // Y轴最大值，通常是 100 或 125
    private var maxX = 6000f      // X轴最大值，例如 6000
    private var yGridCount = 5    // Y轴格数，例如 5 (对应 0, 25, 50, 75, 100, 125)
    private var xGridCount = 6    // X轴格数，例如 6 (对应 0, 1000, 2000...6000)

    private var unitY = "(%)"     // Y轴单位文本
    private var unitX = "(rpm)"   // X轴单位文本

    // --- 画笔定义 ---
    private val yAxisPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED; strokeWidth = 4f }
    private val xAxisPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; strokeWidth = 4f }
    private val tickPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED; strokeWidth = 3f }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0") // 浅灰色虚线网格
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    private val stepLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED; strokeWidth = 6f; style = Paint.Style.STROKE; strokeJoin =
        Paint.Join.MITER // 确保是尖角
    }
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; textSize = 34f; typeface = Typeface.DEFAULT_BOLD
    }
    private val labelPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY; textSize = 28f }

    // --- 边距控制 ---
    private val leftPadding = 140f
    private val bottomPadding = 120f
    private val topPadding = 100f
    private val rightPadding = 160f

    /**
     * Activity 调用此方法更新图表数据和所有配置
     * @param points 数据点列表，Pair<Float, Float> 分别代表 X值(rpm) 和 Y值(百分比)
     * @param maxRpm X轴的最大刻度值
     * @param maxPercent Y轴的最大刻度值
     * @param xGrids X轴的网格划分数量
     * @param yGrids Y轴的网格划分数量
     * @param labelX X轴单位文本
     * @param labelY Y轴单位文本
     */
    fun setChartData(
        points: List<Pair<Float, Float>>,
        maxRpm: Float,
        maxPercent: Float,
        xGrids: Int = 6,
        yGrids: Int = 5,
        labelX: String = "(rpm)",
        labelY: String = "(%)"
    ) {
        this.dataPoints = points
        this.maxX = maxRpm
        this.maxY = maxPercent
        this.xGridCount = xGrids
        this.yGridCount = yGrids
        this.unitX = labelX
        this.unitY = labelY

        // 当数据和配置更新后，请求重新测量布局和重绘
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val usableW = widthSize - leftPadding - rightPadding
        val usableH = heightSize - topPadding - bottomPadding

        // 核心：计算单个网格单元的边长，以保证网格是正方形
        val cellSize = Math.min(usableW / xGridCount, usableH / yGridCount)

        // 根据计算出的 cellSize 确定 View 的最终尺寸
        val finalW = (cellSize * xGridCount + leftPadding + rightPadding).toInt()
        val finalH = (cellSize * yGridCount + topPadding + bottomPadding).toInt()

        setMeasuredDimension(
            resolveSize(finalW, widthMeasureSpec),
            resolveSize(finalH, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 如果没有数据，则不绘制曲线，但背景网格和轴线依然绘制
        if (dataPoints.isEmpty()) {
            drawBackgroundAndAxes(canvas) // 即使没有数据，也要绘制背景和轴线
            return
        }

        // 重新计算绘图区域和 cellSize
        val usableW = width - leftPadding - rightPadding
        val usableH = height - topPadding - bottomPadding
        val cellSize = Math.min(usableW / xGridCount, usableH / yGridCount)

        val realChartWidth = cellSize * xGridCount
        val realChartHeight = cellSize * yGridCount
        val tickLength = 20f // 刻度线长度

        // 调用私有方法绘制背景和轴线，避免重复代码
        drawBackgroundAndAxes(canvas)

        // --- 绘制阶梯曲线 ---
        drawStepPath(canvas, leftPadding, topPadding, realChartWidth, realChartHeight)
    }

    // 辅助方法：绘制背景网格和坐标轴 (不依赖 dataPoints)
    private fun drawBackgroundAndAxes(canvas: Canvas) {
        val usableW = width - leftPadding - rightPadding
        val usableH = height - topPadding - bottomPadding
        val cellSize = Math.min(usableW / xGridCount, usableH / yGridCount) // 再次计算确保正确性

        val realChartWidth = cellSize * xGridCount
        val realChartHeight = cellSize * yGridCount
        val tickLength = 20f

        // 1. 绘制 Y 轴背景网格、红色刻度线及动态数字
        val yValuePerGrid = maxY / yGridCount
        for (i in 0..yGridCount) {
            val y = topPadding + realChartHeight - (i * cellSize)
            canvas.drawLine(leftPadding, y, leftPadding + realChartWidth, y, gridPaint) // 横向网格线

            // 绘制红色刻度短线
            canvas.drawLine(leftPadding - tickLength, y, leftPadding, y, tickPaint)

            labelPaint.color = Color.RED
            val labelYStr = String.format("%.0f", i * yValuePerGrid)
            canvas.drawText(labelYStr, leftPadding - tickLength - 80f, y + 10f, labelPaint)
        }

        // 2. 绘制 X 轴背景网格及动态数字
        val xValuePerGrid = maxX / xGridCount
        for (i in 0..xGridCount) {
            val x = leftPadding + (i * cellSize)
            canvas.drawLine(x, topPadding, x, topPadding + realChartHeight, gridPaint) // 纵向网格线

            labelPaint.color = Color.GRAY
            val labelXStr = String.format("%.0f", i * xValuePerGrid)
            canvas.drawText(labelXStr, x - 40f, topPadding + realChartHeight + 50f, labelPaint)
        }

        // 3. 绘制坐标轴 (Y红 X黑)
        canvas.drawLine(
            leftPadding,
            topPadding,
            leftPadding,
            topPadding + realChartHeight,
            yAxisPaint
        ) // Y轴线
        canvas.drawLine(
            leftPadding,
            topPadding + realChartHeight,
            leftPadding + realChartWidth,
            topPadding + realChartHeight,
            xAxisPaint
        ) // X轴线

        // 4. 绘制单位
        canvas.drawText(unitY, leftPadding - 40f, topPadding - 40f, unitPaint)
        canvas.drawText(
            unitX,
            leftPadding + realChartWidth + 10f,
            topPadding + realChartHeight + 45f,
            unitPaint
        )
    }

    // 辅助方法：绘制阶梯曲线
    private fun drawStepPath(
        canvas: Canvas,
        chartLeft: Float,
        chartTop: Float,
        chartWidth: Float,
        chartHeight: Float
    ) {
        val path = Path()

        // 映射函数：使用动态的 maxX 和 maxY 进行映射
        fun mapX(rpm: Float) = chartLeft + (rpm / maxX) * chartWidth
        fun mapY(pct: Float) = chartTop + chartHeight - (pct / maxY) * chartHeight

        // 确保数据点不为空，且至少有一个点
        if (dataPoints.isEmpty()) return

        // 曲线的起点：通常从第一个数据点的X值，但Y轴起点0%开始
        val firstPointX = mapX(dataPoints[0].first)
        val startY = mapY(0f)
        path.moveTo(firstPointX, startY)

        // 当前绘图位置 (用于绘制阶梯线)
        var currentPathX = firstPointX
        var currentPathY = startY

        // 遍历所有数据点绘制阶梯
        dataPoints.forEach { point ->
            val targetX = mapX(point.first)
            val targetY = mapY(point.second)

            // 第一段：水平延伸，到当前数据点的 X 坐标，但保持前一个 Y 坐标
            path.lineTo(targetX, currentPathY)

            // 第二段：垂直延伸，从当前 X 坐标，到当前数据点的 Y 坐标
            path.lineTo(targetX, targetY)

            // 更新当前路径位置
            currentPathX = targetX
            currentPathY = targetY
        }
        canvas.drawPath(path, stepLinePaint)
    }
}