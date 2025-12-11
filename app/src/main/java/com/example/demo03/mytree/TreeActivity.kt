package com.example.demo03.mytree

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo03.R
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
                "      \"Name\": \"1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 257,\n" +
                "      \"Name\": \"1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 65793,\n" +
                "      \"Name\": \"1.1.1\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"Node\": 513,\n" +
                "      \"Name\": \"1.2\",\n" +
                "      \"Enable\": true,\n" +
                "      \"Expand\": true,\n" +
                "      \"Sel\": true\n" +
                "    }\n" +
                "  ]\n" +
                "}\n"
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

        treeSideAdapter = TreeSideNodeAdapter(treeSideDatas, this, srvTree, rlv_side_menu, false, lastSelectedNodeId) { nodeId ->
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