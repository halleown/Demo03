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
            myPoints,
            listOf("0", "1000", "2000", "3000", "4000", "5000", "6000", "7000"),
            listOf("0", "25", "50", "75", "100", "125"),
            "(rpm)",
            "(%)"
        )
    }
}