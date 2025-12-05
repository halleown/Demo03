package com.example.demo03.tree


// 3. 容器节点（Composite） - 拥有管理子节点的方法和操作行为
class Composite(name: String?) : Component(name!!) {
    val children: MutableList<Component> = ArrayList() // 存储子组件

    // 容器特有的方法：管理子节点
    fun add(component: Component) {
        children.add(component)
    }

    fun remove(component: Component) {
        children.remove(component)
    }

    fun getChild(index: Int): Component {
        return children[index]
    }

    // 容器的操作行为：通常需要递归调用所有子节点的操作
    override fun operation() {
        println("执行容器节点: $name 的操作，它包含以下子节点：")
        for (child in children) {
            child.operation() // 递归调用
        }
    }
}
