package com.example.demo03.mytree

data class TreeSideItems(
    var Node: Long = -1,// 通过uint64_t转成10进制
    var Name: String = "",// 节点名称
    var Weak: String = "",// 节点弱文本
    var Enable: Boolean = false,// 是否可选中 true可选中 false不可选中但可以展开折叠
    var Expand: Boolean = false,// 是否展开与折叠 true展开 false折叠
    var Sel: Boolean = false,// 是否选中 true选中 false未选中
    var All: Boolean = false,// 是否为全部选中节点, 规定只有当前节点层级首个节点为true才生效,生效后勾选该节点,所有当前级兄弟节点全部选中或取消选中
    var childItems: MutableList<TreeSideItems>? = null,//子节点
    var ShowExpand:Boolean=false,//当前节点是否显示展开折叠图标  默认不显示
    var showHorizonLine: Boolean= false,// 当前节点是否显示竖线
)


data class Bean (
    val TreeSideItems: List<TreeSideItems>
)