package com.example.demo03.graph

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.demo03.R

class GraphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        val stepChartView = findViewById<StepChartView>(R.id.stepChartView)

        val myPoints = listOf(
            Pair(600f, 20f),
            Pair(1100f, 38f),
            Pair(1200f, 63f),
            Pair(1300f, 70f),
            Pair(1400f, 85f),
            Pair(6000f, 125f)
        )

        stepChartView.setChartData(
            points = myPoints,
            maxRpm = 6000f,       // 横坐标最大值
            maxPercent = 125f,    // 纵坐标最大值
            xGrids = 6,           // 横向分6格
            yGrids = 5,           // 纵向分5格
            labelX = "(rpm)",
            labelY = "(%)"
        )
    }
}