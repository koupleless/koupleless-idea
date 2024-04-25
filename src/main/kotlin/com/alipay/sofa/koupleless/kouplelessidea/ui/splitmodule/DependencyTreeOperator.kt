package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.util.ui.JTreeUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnOrByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.DependencyTreeUtil
import com.intellij.openapi.project.Project
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 11:55
 */
class DependencyTreeOperator(proj: Project, treeModel: JTree) {
    private val jTree = treeModel
    private val project = proj
    private var moduleTree: FileDependencyTreeNode? = null

    fun clear() {
        this.updateWholeModuleView(FileDependencyTreeNode.DefaultRootNode())
    }

    private fun updateWholeModuleView(moduleTree: FileDependencyTreeNode) {
        this.moduleTree = moduleTree
        val root = buildDefaultMutableTree(moduleTree)
        jTree.model = DefaultTreeModel(root,false)
        reloadWholeTree()
    }

    private fun buildDefaultMutableTree(root: FileDependencyTreeNode, dependOnInitialed:MutableSet<String> = mutableSetOf(), dependByInitialed:MutableSet<String> = mutableSetOf(), curLayer:Int = 0): DefaultMutableTreeNode {
        if(curLayer >= SplitConstants.MAX_TREE_NODE_LAYER){
            return DefaultMutableTreeNode(FileDependencyTreeNode.DefaultOverflowNode())
        }

        // 不重复创建
        if(dependOnInitialed.contains(root.file.absolutePath) && DependencyTreeUtil.isDependOnNode(root)){
            // copy 的原因：提示用户该节点虽然不显示子节点，但可以从别的节点中看到子节点
            return DefaultMutableTreeNode(root.copy())
        }

        if(dependByInitialed.contains(root.file.absolutePath) && DependencyTreeUtil.isDependByNode(root)){
            return DefaultMutableTreeNode(root.copy())
        }

        // 构建 root
        val tree = DefaultMutableTreeNode(root)

        // 记录
        if(DependencyTreeUtil.isDependOnNode(root)){
            dependOnInitialed.add(root.file.absolutePath)
        }

        if(DependencyTreeUtil.isDependByNode(root)){
            dependByInitialed.add(root.file.absolutePath)
        }

        // 构建 children
        root.children.sortBy{it.getName()}

        for (child in root.children){
            val subNode = buildDefaultMutableTree(child,dependOnInitialed,dependByInitialed,curLayer+1)
            subNode?:continue
            tree.add(subNode)
        }
        return tree
    }

    fun appendSubTree(subDependTree: FileDependencyTreeNode) {
        val rootNode = jTree.model.root
        rootNode?:return

        val rootUserObject = (rootNode as DefaultMutableTreeNode).userObject as FileDependencyTreeNode
        rootUserObject.addChild(subDependTree)

        val subTree = buildDefaultMutableTree(subDependTree)
        insertNode(rootNode,subTree)
    }

    /**
     * 插入：在 parent 处插入 child 节点
     * @param
     * @return
     */
    fun insertNode(parent:DefaultMutableTreeNode,child:DefaultMutableTreeNode){
        // 1. 插入
        (jTree.model as DefaultTreeModel).insertNodeInto(child,parent,0)
        // 2. 展开到package节点
        JTreeUtil.scrollPathTo(jTree,child)
    }

    /**
     * 刷新整棵树
     */
    private fun reloadWholeTree(){
        // 通知 JTree 数据已更改
        (jTree.model as DefaultTreeModel).reload()
    }

    fun getAllDependOnOrByTreeNodes(): List<FileDependOnOrByTreeNode> {
        jTree.model.root?:return emptyList()
        return getAllDependOnOrByTreeNodes(jTree.model.root as DefaultMutableTreeNode)
    }


    private fun getAllDependOnOrByTreeNodes(treeNode:DefaultMutableTreeNode, curLayer:Int = 0):List<FileDependOnOrByTreeNode>{
        if(curLayer >= SplitConstants.MAX_TREE_NODE_LAYER){
            return emptyList()
        }

        val res = mutableListOf<FileDependOnOrByTreeNode>()
        if(treeNode.userObject is FileDependOnOrByTreeNode){
            res.add(treeNode.userObject as FileDependOnOrByTreeNode)
        }

        for(child in treeNode.children()){
            res.addAll(getAllDependOnOrByTreeNodes(child as DefaultMutableTreeNode,curLayer+1))
        }
        return res
    }
}
