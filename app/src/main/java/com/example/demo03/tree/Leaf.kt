package com.example.demo03.tree

// 2. 叶子节点（Leaf） - 只需实现操作行为，无需关心子节点管理
class Leaf(name: String?) : Component(name!!) {
    override fun operation() {
        println("执行叶子节点: $name 的操作")
    } // 注意：这里没有 add, remove 等方法
}