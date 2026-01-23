package com.example.demo03.graph

import com.google.gson.annotations.SerializedName

class ChartValItem {
    /**
     * X轴对应点
     */
    @SerializedName("X")
    var x: Float = 0.0F

    /**
     * Y轴对应点
     */
    @SerializedName("Y")
    var y: Float = 0.0F

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}