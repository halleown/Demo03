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
                "      \"Node\": 257,\n" +
                "      \"Name\": \"Root-1 Child-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 258,\n" +
                "      \"Name\": \"Root-2 Child-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 513,\n" +
                "      \"Name\": \"Root-1 Child-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 514,\n" +
                "      \"Name\": \"Root-2 Child-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 66050,\n" +
                "      \"Name\": \"Root2 Child-1-0\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 131586,\n" +
                "      \"Name\": \"Root2 Child-1-1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": false\n" +
                "    }\n" +
                "  ]\n" +
                "}\n"
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