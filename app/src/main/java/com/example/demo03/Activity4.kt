package com.example.demo03

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo03.tree.Component
import com.example.demo03.tree.Composite
import com.example.demo03.tree.Leaf
import com.example.demo03.tree.TreeAdapter

/**
 * 菜单树
 */
class Activity4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_4)



        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val composite1 = Composite("文件夹1")
        composite1.isRootNode=false
        val composite2 = Composite("文件夹2")
        composite2.isRootNode=false
        val composite21 = Composite("文件夹2-2")

        val leaf1 = Leaf("文件1-1")
        val leaf2 = Leaf("文件1-2")
        val leaf3 = Leaf("文件2-1")
        val leaf4 = Leaf("文件2-2-1")
        val leaf5 = Leaf("文件2-2-2")

        composite21.add(leaf4)
        composite21.add(leaf5)

        composite1.add(leaf1)
        composite1.add(leaf2)

        composite2.add(leaf3)
        composite2.add(composite21)

        var datas: MutableList<Component> = mutableListOf()
        datas.add(composite1)
        datas.add(composite2)
        val treeAdapter = TreeAdapter(this)
        recyclerView.adapter = treeAdapter
        treeAdapter.addDataList(datas)
    }
}

