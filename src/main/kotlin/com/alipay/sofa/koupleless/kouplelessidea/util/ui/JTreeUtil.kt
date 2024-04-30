package com.alipay.sofa.koupleless.kouplelessidea.util.ui

import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath


object JTreeUtil {
    fun scrollPathTo(jTree:JTree,node: DefaultMutableTreeNode){
        val nodePath = TreePath(node.path)
        jTree.scrollPathToVisible(nodePath)
        jTree.selectionPath = nodePath
    }

    fun modifyNode(model: DefaultTreeModel,old:DefaultMutableTreeNode,new:DefaultMutableTreeNode):DefaultMutableTreeNode{
        val parent = old.parent as DefaultMutableTreeNode
        // 1. 删除
        model.removeNodeFromParent(old)
        // 2. 插入
        model.insertNodeInto(new, parent, 0)
        return new
    }

    fun modifyNode(model: DefaultTreeModel,node:DefaultMutableTreeNode):DefaultMutableTreeNode{
        model.nodeChanged(node)
        return node
    }

    fun deleteNode(model: DefaultTreeModel, node: DefaultMutableTreeNode) {
        model.removeNodeFromParent(node)
    }

    fun updateSubNodes(model: DefaultTreeModel, node: DefaultMutableTreeNode){
        model.nodeStructureChanged(node)
    }
}
