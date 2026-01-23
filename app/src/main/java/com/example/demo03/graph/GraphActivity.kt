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
            ChartValItem(600f, 20f),
            ChartValItem(1100f, 38f),
            ChartValItem(1200f, 63f),
            ChartValItem(1300f, 70f),
            ChartValItem(1400f, 85f),
            ChartValItem(6000f, 125f)
        )

        /*
            x: ("0", "1000", "2000", "3000", "4000", "5000", "6000", "7000"),
            y: ("0", "25", "50", "75", "100", "125"),
         */

        stepChartView.setChartData(
            "(rpm)",
            0,
            6000,
            ("%"),
            0f,
            125f,
            myPoints
        )


    }
}