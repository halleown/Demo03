package com.example.demo03.tree

// 1. 抽象组件（Component） - 仅定义公共操作，不包含管理子节点的方法
abstract class Component(var name: String) {
    var isRootNode = true

    // 所有组件（叶子和容器）都拥有的行为，比如显示、执行操作等
    abstract fun operation()
}