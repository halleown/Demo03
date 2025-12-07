package com.example.demo03.mytree

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo03.R
import com.google.gson.Gson

class TreeActivity : AppCompatActivity() {

    var treeSideDatas: MutableList<TreeSideItems> = mutableListOf()
    private lateinit var treeSideAdapter: TreeSideNodeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // setTheme(android.R.style.Theme_Material_Light)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tree)

        val myData = "{\n" +
                "  \"TreeSideItems\": [\n" +
                "    {\n" +
                "      \"Node\": 1,\n" +
                "      \"Name\": \"RootNode-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 2,\n" +
                "      \"Name\": \"RootNode-2\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 3,\n" +
                "      \"Name\": \"RootNode-3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 4,\n" +
                "      \"Name\": \"RootNode-4\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 257,\n" +
                "      \"Name\": \"Root-1 Child-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 258,\n" +
                "      \"Name\": \"Root-1 Child-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 259,\n" +
                "      \"Name\": \"Root-1 Child-2\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 260,\n" +
                "      \"Name\": \"Root-1 Child-3\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 514,\n" +
                "      \"Name\": \"Root-2 Child-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 515,\n" +
                "      \"Name\": \"Root-2 Child-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 516,\n" +
                "      \"Name\": \"Root-2 Child-2\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 771,\n" +
                "      \"Name\": \"Root-3 Child-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 772,\n" +
                "      \"Name\": \"Root-3 Child-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 1028,\n" +
                "      \"Name\": \"Root-4 Child-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 1029,\n" +
                "      \"Name\": \"Root-4 Child-1\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 1030,\n" +
                "      \"Name\": \"Root-4 Child-2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66049,\n" +
                "      \"Name\": \"Root-1 Child-0 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66050,\n" +
                "      \"Name\": \"Root-1 Child-0 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66051,\n" +
                "      \"Name\": \"Root-1 Child-0 Sub-2\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66305,\n" +
                "      \"Name\": \"Root-1 Child-1 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66306,\n" +
                "      \"Name\": \"Root-1 Child-1 Sub-1\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66307,\n" +
                "      \"Name\": \"Root-1 Child-1 Sub-2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66561,\n" +
                "      \"Name\": \"Root-1 Child-2 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66817,\n" +
                "      \"Name\": \"Root-1 Child-3 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66818,\n" +
                "      \"Name\": \"Root-1 Child-3 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66819,\n" +
                "      \"Name\": \"Root-1 Child-3 Sub-2\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131330,\n" +
                "      \"Name\": \"Root-2 Child-0 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131331,\n" +
                "      \"Name\": \"Root-2 Child-0 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131332,\n" +
                "      \"Name\": \"Root-2 Child-0 Sub-2\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131586,\n" +
                "      \"Name\": \"Root-2 Child-1 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131587,\n" +
                "      \"Name\": \"Root-2 Child-1 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131588,\n" +
                "      \"Name\": \"Root-2 Child-1 Sub-2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131842,\n" +
                "      \"Name\": \"Root-2 Child-2 Sub-0\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131843,\n" +
                "      \"Name\": \"Root-2 Child-2 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197379,\n" +
                "      \"Name\": \"Root-3 Child-0 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197380,\n" +
                "      \"Name\": \"Root-3 Child-0 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197636,\n" +
                "      \"Name\": \"Root-3 Child-1 Sub-0\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197637,\n" +
                "      \"Name\": \"Root-3 Child-1 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 197638,\n" +
                "      \"Name\": \"Root-3 Child-1 Sub-2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263172,\n" +
                "      \"Name\": \"Root-4 Child-0 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263173,\n" +
                "      \"Name\": \"Root-4 Child-0 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263174,\n" +
                "      \"Name\": \"Root-4 Child-0 Sub-2\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263429,\n" +
                "      \"Name\": \"Root-4 Child-1 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263430,\n" +
                "      \"Name\": \"Root-4 Child-1 Sub-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263686,\n" +
                "      \"Name\": \"Root-4 Child-2 Sub-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263687,\n" +
                "      \"Name\": \"Root-4 Child-2 Sub-1\",\n" +
                "      \"Enable\": false,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 263688,\n" +
                "      \"Name\": \"Root-4 Child-2 Sub-2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": false,\n" +
                "      \"Sel\": false\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        val bean = Gson().fromJson(myData, Bean::class.java)
        val buildTree = buildTree2(bean.TreeSideItems)
        treeSideDatas.clear()
        treeSideDatas.addAll(buildTree)

        val srvTree: CustomHorizontalScrollView = findViewById(R.id.srvTree)

        val rlv_side_menu: RecyclerView = findViewById(R.id.rlv_side_menu)
        rlv_side_menu?.layoutManager = LinearLayoutManager(this)
        treeSideAdapter = TreeSideNodeAdapter(treeSideDatas, this, srvTree, rlv_side_menu!!)
        treeSideAdapter.notifyDataSetChanged()

        rlv_side_menu.adapter = treeSideAdapter

    }
}
/*
{
  "TreeSideItems": [
    {
      "Node": 1,
      "Name": "RootNode-1",
      "Enable": true,
      "Expand": true,
      "Sel": true
    },
    {
      "Node": 2,
      "Name": "RootNode-2",
      "Enable": false,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 257,
      "Name": "Root-1 Child-0",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 258,
      "Name": "Root-2 Child-0",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 513,
      "Name": "Root-1 Child-1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 514,
      "Name": "Root-2 Child-1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 66050,
      "Name": "Root2 Child-1-0",
      "Enable": true,
      "Expand": true,
      "Sel": false
    },
    {
      "Node": 131586,
      "Name": "Root2 Child-1-1",
      "Enable": true,
      "Expand": true,
      "Sel": false
    }
  ]
}

 */




/*
"TreeSideItems": [// 如果当前节点有子节点，那么显示ivExpand，
    {
        "Node": 1,
        "Name": "RootNode-1",
        "Enable": true,// 是否可用
        "Expand": true,// 是否展开
        "Sel": true,// 是否选中
        "ShowExpand": true,// 是否显示展开图标（此参数非中间件传）【当childItems不为空时，showExpand自动为true】
        "childItems": [
            {
                "Node": 257,// 01 01，父节点node=01
                "Name": "Root-1 Child-0",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": false
            },
            {
                "Node": 513,// 02 01，父节点node=01
                "Name": "Root-1 Child-1",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": false
            },
        ]
    },
    {
        "Node": 2,
        "Name": "RootNode-2",
        "Enable": false,
        "Expand": true,
        "Sel": false,
        "ShowExpand": true
        "childItems": [
            {
                "Node": 258,// 01 02，父节点node=02
                "Name": "Root-2 Child-0",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": false
            },
            {
                "Node": 514,// 02 02，父节点node=02
                "Name": "Root-2 Child-1",
                "Enable": true,
                "Expand": true,
                "Sel": false,
                "ShowExpand": true
                "childItems": [
                    {
                        "Node": 66050,// 01 0202，父节点node=0202
                        "Name": "Root2 Child-1-0",
                        "Enable": true,
                        "Expand": true,
                        "Sel": false,
                        "ShowExpand": false
                    },
                    {
                        "Node": 131586,// 02 0202，父节点node=0202
                        "Name": "Root2 Child-1-1",
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