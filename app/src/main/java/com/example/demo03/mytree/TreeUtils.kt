package com.example.demo03.mytree

import android.util.Log

/**
 * @author: Luuuzi
 * @Date: 2024-08-28
 * @description:侧拉工具类
 */

// 中间件不传showExpand参数时，调用此方法
// fun buildTree2(items: List<TreeSideItems>): MutableList<TreeSideItems> {
//     val rootItems = mutableListOf<TreeSideItems>()
//     val nodeMap = mutableMapOf<Long, TreeSideItems>()
//     for (item in items) {// 遍历中间件传过来的原始节点数据
//         val node = item.Node
//         val isRoot = getLevel(node)
//         if (isRoot) {// 根节点，直接添加到根节点列表
//             rootItems.add(item)
//         } else {// 子节点
//             // 计算父节点的 Node
//             val parentNode = getParentNode(node)
//             val parentItem = nodeMap[parentNode]
//
//             // 将当前节点添加到父节点的子节点列表中
//             if (parentItem != null) {
//                 if (parentItem.childItems == null) {
//                     // 父节点有子节点是，显示展开图标
//                     parentItem.ShowExpand = true
//                     parentItem.childItems = mutableListOf()
//                 }
//                 parentItem.childItems!!.add(item)
//             }
//         }
//
//         // 将当前节点放入 nodeMap 中
//         nodeMap[node] = item
//     }
//     return rootItems
// }

fun buildTree(items: List<TreeSideItems>): MutableList<TreeSideItems> {
    val rootItems = mutableListOf<TreeSideItems>()
    val nodeMap = mutableMapOf<Long, TreeSideItems>()

    for (item in items) {
        val node = item.Node
        val level = getLevel(node)

        if (level) {
            // 如果是根节点，直接添加到根节点列表
            rootItems.add(item)
        } else {
            // 计算父节点的 Node
            val parentNode = getParentNode(node)
            val parentItem = nodeMap[parentNode]

            // 将当前节点添加到父节点的子节点列表中
            if (parentItem != null) {
                if (parentItem.childItems == null) {
                    parentItem.childItems = mutableListOf()
                }
                parentItem.childItems!!.add(item)
            }
        }

        // 将当前节点放入 nodeMap 中
        nodeMap[node] = item
    }
    return rootItems
}

// 通过 Node 是否是根节点
fun getLevel(node: Long): Boolean {
    val format = toEvenLengthHex(node)
    return format.length == 2
    /*var level = 0
    var value = node

    // 每次右移8位，统计层级
    while (value > 0) {
        value = value shr 8
        level++
    }

    return level*/
}

// 计算父节点的 Node
fun getParentNode(number: Long): Long {
    // 将十进制数转换为十六进制字符串
//        val hexString = number.toString(16)
    val hexString = toEvenLengthHex(number)

    // 确保十六进制字符串的长度至少为2，以便去掉前两位
    if (hexString.length <= 2) {
        return 0 // 或者返回特殊的标识
    }
    // 去掉前两位十六进制数字
    val trimmedHex = hexString.substring(2)
    // 将剩余的十六进制字符串转换回十进制数
    return trimmedHex.toLong(16)
}

fun clearSelected(selectNode: Long, selectdatas: MutableList<TreeSideItems>): MutableList<TreeSideItems> {
    var oldDatas: MutableList<TreeSideItems> = mutableListOf()
    selectdatas.forEach {
        run {
            if (it.Node != selectNode) {
                if (it.Sel) {
                    oldDatas.add(it)
                }
                it.Sel = false

                //将父节点的字体设置为橙色
                val reversed = toEvenLengthHex(selectNode).reversed()
                val reversed1 = toEvenLengthHex(it.Node).reversed()
                Log.i("aaa", "当前节点：$reversed，对比节点：$reversed1")
                val startsWith = reversed.startsWith(reversed1)
//                if (startsWith) {
//                    it.isSelectText = true
//                } else {
//                    it.isSelectText = false
//                }

            }
            if (!it.childItems.isNullOrEmpty()) {
                oldDatas.addAll(clearSelected(selectNode, it.childItems!!))
            }
        }
    }
    return oldDatas
}

/**
 * 查找节点
 */
fun findNode(selectNode: Long, selectdatas: MutableList<TreeSideItems>): TreeSideItems? {
    var result: TreeSideItems? = null
    selectdatas.forEach {
        run {
            if (it.Node == selectNode) {
                result = it
                return result
            }
            if (!it.childItems.isNullOrEmpty()) {
                result = findNode(selectNode, it.childItems!!)
            }
        }
    }
    return result
}

fun deleteNode(selectNode: Long, selectdatas: MutableList<TreeSideItems>) {
    val iterator = selectdatas.iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (item.Node == selectNode) {
            iterator.remove()
            return
        }
        if (!item.childItems.isNullOrEmpty()) {
            deleteNode(selectNode, item.childItems!!)
        }
    }
}

fun clearSelected2(selectNode: Long, selectdatas: MutableList<TreeSideItems>): MutableList<TreeSideItems> {
    var oldDatas: MutableList<TreeSideItems> = mutableListOf()
    selectdatas.forEach {
        run {
            if (it.Node != selectNode) {
                if (it.Sel) {
                    oldDatas.add(it)
                }
                it.Sel = false

                //将父节点的字体设置为橙色
                val reversed = toEvenLengthHex(selectNode).reversed()
                val reversed1 = toEvenLengthHex(it.Node).reversed()
//                LogUtils.i("aaa", "当前节点：$reversed，对比节点：$reversed1")
                val startsWith = reversed.startsWith(reversed1)
//                if (startsWith) {
//                    it.isSelectText = true
//                } else {
//                    it.isSelectText = false
//                }
            }else {

            }

            if (!it.childItems.isNullOrEmpty()) {
                oldDatas.addAll(clearSelected2(selectNode, it.childItems!!))
            }
        }
    }
    return oldDatas
}

//复选框:点击没有子节点的
fun modifyCheckItemStatus(datas: MutableList<TreeSideItems>, selectNode: Long): MutableList<TreeSideItems> {
    var oldDatas: MutableList<TreeSideItems> = mutableListOf()
    if (getLevel(selectNode)) {
        return oldDatas
    }
    val hexString = toEvenLengthHex(selectNode)
    val length = hexString.length / 2
    for (i in 1 until length) {
        val trimmedHex = hexString.substring(2 * i)
        val findNode = findNode(trimmedHex.toLong(16), datas)
        if (findNode != null && !findNode.childItems.isNullOrEmpty()) {
            var tem = true
            findNode.childItems?.forEach {
                run {
                    if (!it.Sel) {
                        tem = it.Sel
                        return@forEach
                    }
                }
            }
            if (findNode.Sel != tem) {
                oldDatas.add(findNode)
            }
            findNode.Sel = tem
        }
    }
    return oldDatas
}

//复选框点击有子节点的(向下递归)
fun modifyCheckItemsStatusDown(datas: MutableList<TreeSideItems>?, sel: Boolean): MutableList<TreeSideItems> {
    var oldDatas: MutableList<TreeSideItems> = mutableListOf()
    datas?.forEach {
        run {
            if (it.childItems.isNullOrEmpty()) {
                if (it.Sel != sel) {
                    oldDatas.add(it)
                }
                it.Sel = sel
            } else {
                oldDatas.addAll(modifyCheckItemsStatusDown(it.childItems, sel))
                if (it.Sel != sel) {
                    oldDatas.add(it)
                }
                it.Sel = sel
            }
        }
    }
    return oldDatas
}

//复选框点击(向上递归)
fun modifyCheckItemsStatusUp(datas: MutableList<TreeSideItems>, selectNode: Long): MutableList<TreeSideItems> {
    var oldDatas: MutableList<TreeSideItems> = mutableListOf()
    if (getLevel(selectNode)) {
        return oldDatas
    }
    val hexString = toEvenLengthHex(selectNode)
    val length = hexString.length / 2
    for (i in 1 until length) {
        val trimmedHex = hexString.substring(2 * i)
        val findNode = findNode(trimmedHex.toLong(16), datas)
        if (findNode != null && !findNode.childItems.isNullOrEmpty()) {

            var tem = true
            findNode.childItems?.forEach {
                run {
                    if (!it.Sel) {
                        tem = it.Sel
                        return@forEach
                    }
                }
            }
            if (findNode.Sel != tem) {
                oldDatas.add(findNode)
            }
            findNode.Sel = tem
        }
    }
    return oldDatas
}
//如果子列表里面有选中，父node标题标记为橙色
fun selectParentNode(datas: MutableList<TreeSideItems>, all: MutableList<TreeSideItems>) {
    datas.forEach {
        run {
            if(it.Sel){
//                findNode(getParentNode(it.Node),all)?.isSelectText = true
            }
            if (!it.childItems.isNullOrEmpty()) {
                selectParentNode(it.childItems!!, all)
            }
        }
    }
}
//修改是否可选中状态
fun modifyitemsEnableValue(datas: MutableList<TreeSideItems>, all: MutableList<TreeSideItems>) {
    datas.forEach {
        if(it.Sel){
//            findNode(getParentNode(it.Node),all)?.isSelectText = true
        }
        run {
            if (getLevel(it.Node)) {
                if (!it.childItems.isNullOrEmpty()) {
                    modifyitemsEnableValue(it.childItems!!, all)
                }
            } else {
                val hexString = toEvenLengthHex(it.Node)
                val length = hexString.length / 2
                for (i in 1 until length) {
                    val trimmedHex = hexString.substring(2 * i)
                    val findNode = findNode(trimmedHex.toLong(16), all)
                    if (findNode != null) {
                        if (!findNode.Enable) {
                            it.Enable = findNode.Enable
                        }
                    }
                }
                if (!it.childItems.isNullOrEmpty()) {
                    modifyitemsEnableValue(it.childItems!!, all)
                }
            }
        }
    }
}

/**
 * 10进制转换为对应的16进制去掉0x并且是偶数位不是偶数位的前面补0
 */
fun toEvenLengthHex(value: Long): String {
    // 将整数转换为16进制字符串，并去掉 "0x" 前缀
    var hexString = value.toString(16)

    // 如果结果长度是奇数，在前面补零
    if (hexString.length % 2 != 0) {
        hexString = "0$hexString"
    }

    return hexString
}


