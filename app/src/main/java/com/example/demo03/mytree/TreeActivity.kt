package com.example.demo03.mytree

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo03.R
import com.example.demo03.dash.DashedLineView
import com.google.gson.Gson

class TreeActivity : AppCompatActivity() {

    var treeSideDatas: MutableList<TreeSideItems> = mutableListOf()
    private lateinit var treeSideAdapter: TreeSideNodeAdapter

    // // 实线段长度
    // val fixedDashLength = this.resources.getDimension(R.dimen._10dp)
    // // 虚线段长度
    // val fixedGapLength = this.resources.getDimension(R.dimen._3dp)

    override fun onCreate(savedInstanceState: Bundle?) {
        // setTheme(android.R.style.Theme_Material_Light)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tree)

        val myData = "{\n" +
                "  \"TreeSideItems\": [\n" +
                "    {\n" +
                "      \"Node\": 1,\n" +
                "      \"Name\": \"Root-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 2,\n" +
                "      \"Name\": \"Root-2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 3,\n" +
                "      \"Name\": \"Root-3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4,\n" +
                "      \"Name\": \"Root-4\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 257,\n" +
                "      \"Name\": \"1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 513,\n" +
                "      \"Name\": \"1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 769,\n" +
                "      \"Name\": \"1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 1025,\n" +
                "      \"Name\": \"1.4\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 258,\n" +
                "      \"Name\": \"2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 514,\n" +
                "      \"Name\": \"2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 770,\n" +
                "      \"Name\": \"2.3\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 259,\n" +
                "      \"Name\": \"3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 515,\n" +
                "      \"Name\": \"3.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 65793,\n" +
                "      \"Name\": \"1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66049,\n" +
                "      \"Name\": \"1.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66305,\n" +
                "      \"Name\": \"1.1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66561,\n" +
                "      \"Name\": \"1.1.4\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131585,\n" +
                "      \"Name\": \"1.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131841,\n" +
                "      \"Name\": \"1.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 132097,\n" +
                "      \"Name\": \"1.2.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 132353,\n" +
                "      \"Name\": \"1.2.4\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197121,\n" +
                "      \"Name\": \"1.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197377,\n" +
                "      \"Name\": \"1.3.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197633,\n" +
                "      \"Name\": \"1.3.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 262657,\n" +
                "      \"Name\": \"1.4.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 262913,\n" +
                "      \"Name\": \"1.4.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 65794,\n" +
                "      \"Name\": \"2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66050,\n" +
                "      \"Name\": \"2.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66306,\n" +
                "      \"Name\": \"2.1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131330,\n" +
                "      \"Name\": \"2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131586,\n" +
                "      \"Name\": \"2.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131842,\n" +
                "      \"Name\": \"2.2.3\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197122,\n" +
                "      \"Name\": \"2.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197378,\n" +
                "      \"Name\": \"2.3.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 65795,\n" +
                "      \"Name\": \"3.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66051,\n" +
                "      \"Name\": \"3.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66307,\n" +
                "      \"Name\": \"3.1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66563,\n" +
                "      \"Name\": \"3.1.4\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131587,\n" +
                "      \"Name\": \"3.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131843,\n" +
                "      \"Name\": \"3.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843009,\n" +
                "      \"Name\": \"1.1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843265,\n" +
                "      \"Name\": \"1.1.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843521,\n" +
                "      \"Name\": \"1.1.1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843777,\n" +
                "      \"Name\": \"1.1.1.4\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16908545,\n" +
                "      \"Name\": \"1.1.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16908801,\n" +
                "      \"Name\": \"1.1.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16909057,\n" +
                "      \"Name\": \"1.1.2.3\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16974081,\n" +
                "      \"Name\": \"1.1.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16974337,\n" +
                "      \"Name\": \"1.1.3.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 17039617,\n" +
                "      \"Name\": \"1.1.4.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 17039873,\n" +
                "      \"Name\": \"1.1.4.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 17040129,\n" +
                "      \"Name\": \"1.1.4.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33554945,\n" +
                "      \"Name\": \"1.2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33555201,\n" +
                "      \"Name\": \"1.2.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33555457,\n" +
                "      \"Name\": \"1.2.1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620225,\n" +
                "      \"Name\": \"1.2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620481,\n" +
                "      \"Name\": \"1.2.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620737,\n" +
                "      \"Name\": \"1.2.2.3\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620993,\n" +
                "      \"Name\": \"1.2.2.4\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33685505,\n" +
                "      \"Name\": \"1.2.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33685761,\n" +
                "      \"Name\": \"1.2.3.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33750785,\n" +
                "      \"Name\": \"1.2.4.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33751041,\n" +
                "      \"Name\": \"1.2.4.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40140801,\n" +
                "      \"Name\": \"1.3.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40141057,\n" +
                "      \"Name\": \"1.3.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40205825,\n" +
                "      \"Name\": \"1.3.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40206081,\n" +
                "      \"Name\": \"1.3.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40206337,\n" +
                "      \"Name\": \"1.3.2.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40270849,\n" +
                "      \"Name\": \"1.3.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40271105,\n" +
                "      \"Name\": \"1.3.3.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 46746881,\n" +
                "      \"Name\": \"1.4.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 46811649,\n" +
                "      \"Name\": \"1.4.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 46811905,\n" +
                "      \"Name\": \"1.4.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843010,\n" +
                "      \"Name\": \"2.1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843266,\n" +
                "      \"Name\": \"2.1.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843522,\n" +
                "      \"Name\": \"2.1.1.3\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16908546,\n" +
                "      \"Name\": \"2.1.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16908802,\n" +
                "      \"Name\": \"2.1.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16974082,\n" +
                "      \"Name\": \"2.1.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16974338,\n" +
                "      \"Name\": \"2.1.3.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33554946,\n" +
                "      \"Name\": \"2.2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33555202,\n" +
                "      \"Name\": \"2.2.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620226,\n" +
                "      \"Name\": \"2.2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620482,\n" +
                "      \"Name\": \"2.2.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620738,\n" +
                "      \"Name\": \"2.2.2.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33685506,\n" +
                "      \"Name\": \"2.2.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40140802,\n" +
                "      \"Name\": \"2.3.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40141058,\n" +
                "      \"Name\": \"2.3.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 40205826,\n" +
                "      \"Name\": \"2.3.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843011,\n" +
                "      \"Name\": \"3.1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843267,\n" +
                "      \"Name\": \"3.1.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843523,\n" +
                "      \"Name\": \"3.1.1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16843779,\n" +
                "      \"Name\": \"3.1.1.4\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16908547,\n" +
                "      \"Name\": \"3.1.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16908803,\n" +
                "      \"Name\": \"3.1.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 16974083,\n" +
                "      \"Name\": \"3.1.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 17039619,\n" +
                "      \"Name\": \"3.1.4.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 17039875,\n" +
                "      \"Name\": \"3.1.4.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33554947,\n" +
                "      \"Name\": \"3.2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33555203,\n" +
                "      \"Name\": \"3.2.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620227,\n" +
                "      \"Name\": \"3.2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 33620483,\n" +
                "      \"Name\": \"3.2.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4311810305,\n" +
                "      \"Name\": \"1.1.1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4311810561,\n" +
                "      \"Name\": \"1.1.1.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4311810817,\n" +
                "      \"Name\": \"1.1.1.1.3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4311875841,\n" +
                "      \"Name\": \"1.1.1.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4311876097,\n" +
                "      \"Name\": \"1.1.1.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4311941121,\n" +
                "      \"Name\": \"1.1.1.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4312006657,\n" +
                "      \"Name\": \"1.1.1.4.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4312006913,\n" +
                "      \"Name\": \"1.1.1.4.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4318072833,\n" +
                "      \"Name\": \"1.1.2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4318073089,\n" +
                "      \"Name\": \"1.1.2.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4318138369,\n" +
                "      \"Name\": \"1.1.2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4318138625,\n" +
                "      \"Name\": \"1.1.2.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4318203905,\n" +
                "      \"Name\": \"1.1.2.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4324269825,\n" +
                "      \"Name\": \"1.1.3.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4324335361,\n" +
                "      \"Name\": \"1.1.3.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4324335617,\n" +
                "      \"Name\": \"1.1.3.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4330401537,\n" +
                "      \"Name\": \"1.1.4.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4330401793,\n" +
                "      \"Name\": \"1.1.4.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4330467329,\n" +
                "      \"Name\": \"1.1.4.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4330467585,\n" +
                "      \"Name\": \"1.1.4.2.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4330533121,\n" +
                "      \"Name\": \"1.1.4.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4395638785,\n" +
                "      \"Name\": \"1.2.1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4395639041,\n" +
                "      \"Name\": \"1.2.1.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4395704577,\n" +
                "      \"Name\": \"1.2.1.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4395770113,\n" +
                "      \"Name\": \"1.2.1.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4401836033,\n" +
                "      \"Name\": \"1.2.2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4401836289,\n" +
                "      \"Name\": \"1.2.2.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4401901825,\n" +
                "      \"Name\": \"1.2.2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4401967361,\n" +
                "      \"Name\": \"1.2.2.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4402032897,\n" +
                "      \"Name\": \"1.2.2.4.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4408098817,\n" +
                "      \"Name\": \"1.2.3.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4408164353,\n" +
                "      \"Name\": \"1.2.3.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4414230273,\n" +
                "      \"Name\": \"1.2.4.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4414230529,\n" +
                "      \"Name\": \"1.2.4.1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4414296065,\n" +
                "      \"Name\": \"1.2.4.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4480401921,\n" +
                "      \"Name\": \"1.3.1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4480467457,\n" +
                "      \"Name\": \"1.3.1.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4486533377,\n" +
                "      \"Name\": \"1.3.2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4486598913,\n" +
                "      \"Name\": \"1.3.2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4486664449,\n" +
                "      \"Name\": \"1.3.2.3.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4492730369,\n" +
                "      \"Name\": \"1.3.3.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4492795905,\n" +
                "      \"Name\": \"1.3.3.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4558901761,\n" +
                "      \"Name\": \"1.4.1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4564967681,\n" +
                "      \"Name\": \"1.4.2.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4565033217,\n" +
                "      \"Name\": \"1.4.2.2.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n"
        val bean = Gson().fromJson(myData, Bean::class.java)
        val buildTree = buildTree(bean.TreeSideItems)
        treeSideDatas.clear()
        treeSideDatas.addAll(buildTree)

        val srvTree: CustomHorizontalScrollView = findViewById(R.id.srvTree)

        val rlv_side_menu: RecyclerView = findViewById(R.id.rlv_side_menu)
        rlv_side_menu?.layoutManager = LinearLayoutManager(this)
        // treeSideAdapter = TreeSideNodeAdapter(treeSideDatas, this, srvTree, rlv_side_menu!!)
        // treeSideAdapter.notifyDataSetChanged()


        // 记录当前选中
        var lastSelectedNodeId: Long = 1L

        treeSideAdapter = TreeSideNodeAdapter(treeSideDatas, this, srvTree, rlv_side_menu, lastSelectedNodeId) { nodeId ->
            // 外层只更新选中 id，不用全量 notify
//            lastSelectedNodeId = nodeId
//            treeSideAdapter.updateSelection(nodeId)
            if (nodeId == lastSelectedNodeId) return@TreeSideNodeAdapter

            // 数据层：全树统一改 Sel
            setSel(treeSideDatas, lastSelectedNodeId, false)
            setSel(treeSideDatas, nodeId, true)
            lastSelectedNodeId = nodeId

            Log.d("xialj", "onCreate: ${Gson().toJson(treeSideDatas)}")

            // UI 局部刷新（适配器会递归 childAdapters）
            treeSideAdapter.updateSelection(nodeId)

        }


        rlv_side_menu.adapter = treeSideAdapter

        showTwoDash()
        myTest()

    }

    private fun showTwoDash() {
        val dashView1 = findViewById<DashedLineView>(R.id.dashView1)
        val dashView2 = findViewById<DashedLineView>(R.id.dashView2)

        dashView1.post {
            val h1 = dashView1.height

            // 1. 假设第一个 View 从 0 开始画
            dashView1.phaseOffset = 0f

            // 2. 计算第一个 View 结束时的进度
            // 如果 dashView1 高度是 50，周期是 60，那么它结束时进度是 50
            val nextOffset = dashView1.getNextOffset(h1)

            // 3. 让第二个 View 从这个进度接着画
            // 这样 dashView2 的开头就会从周期的第 50 像素位置开始，视觉上就接上了！
            dashView2.phaseOffset = nextOffset
        }
    }

    private fun myTest() {
        val view_t = findViewById<TShapeView>(R.id.view_t)
        val view_i = findViewById<IShapeView>(R.id.view_i)
        val view_l = findViewById<LShapeView>(R.id.view_l)

        var phase = 0f
        view_t.post {
            view_t.setDashPhase(phase)
            phase = view_t.getNextViewPhase()
        }
        view_i.post {
            view_i.setDashPhase(phase)
            phase = view_i.getNextViewPhase()
        }
        view_l.post {
            view_l.setDashPhase(phase)
            // 最后一个节点没有下一个自定义view了
            // phase = view_l.getLastDashGapLength()
        }
    }

    // 递归修改 Sel
    private fun setSel(list: List<TreeSideItems>?, target: Long, value: Boolean): Boolean {
        if (list == null) return false
        for (item in list) {
            if (item.Node == target) {
                item.Sel = value
                return true
            }
            if (setSel(item.childItems, target, value)) return true
        }
        return false
    }
}

/*
节点规则：
节点值为十进制，对应的十六进制代表节点层级，

十进制   ->  十六进制    ->    节点值
131585      0x020201        1.2.2
197121      0x030201        1.2.3

以十六进制【0x030201】为例，
高位<-低位
0x030201
最低为【01】为根节点，【02】代表根节点的叶子节点，【03】代表根节点的叶子节点的叶子节点



"TreeSideItems": [// 如果当前节点有子节点，那么显示ivExpand，
    {
        "Node": 1,
        "Name": "1",
        "Enable": true,// 是否可用
        "Expand": true,// 是否展开
        "Sel": true,// 是否选中
        "ShowExpand": true,// 是否显示展开图标（此参数非中间件传）【当childItems不为空时，showExpand自动为true】
        "childItems": [
            {
                "Node": 257,// 0x0101，父节点node=01
                "Name": "1.1",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": false
            },
            {
                "Node": 513,// 02 01，父节点node=01
                "Name": "1.2",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": false
            },
        ]
    },
    {
        "Node": 2,
        "Name": "2",
        "Enable": false,
        "Expand": true,
        "Sel": false,
        "ShowExpand": true
        "childItems": [
            {
                "Node": 258,// 0x0102，父节点node=02
                "Name": "2.1",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": false
            },
            {
                "Node": 514,// 0x0202，父节点node=02
                "Name": "2.2",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": true
                "childItems": [
                    {
                        "Node": 66050,// 0x10202，父节点node=0202
                        "Name": "2.2.1",
                        "Enable": true,
                        "Expand": true,
                        "Sel": false,
                        "ShowExpand": false
                    },
                    {
                        "Node": 131586,// 0x20202，父节点node=0202
                        "Name": "2.2.2",
                        "Enable": true,
                        "Expand": true,
                        "Sel": false,
                        "ShowExpand": false
                    }
                ]
            },
        ]
    }
]

 */


/*
{
  "TreeSideItems": [
    {
      "Node": 1,
      "Name": "1",
      "Enable": true,
      "Expand": true,
      "Sel": true
    },
    {
      "Node": 2,
      "Name": "2",
      "Enable": false,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 257,
      "Name": "1.1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 513,
      "Name": "1.2",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 65793,
      "Name": "1.1.1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 66049,
      "Name": "1.2.1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 131585,
      "Name": "1.2.2",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 196121,
      "Name": "1.2.3",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 258,
      "Name": "2.1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 514,
      "Name": "2.2",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 65794,
      "Name": "2.1.1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 131330,
      "Name": "2.1.2",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 66050,
      "Name": "2.2.1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 131586,
      "Name": "2.2.2",
      "Enable": true,
      "Expand": true,
      "Sel": false
    }
  ]
}

 */