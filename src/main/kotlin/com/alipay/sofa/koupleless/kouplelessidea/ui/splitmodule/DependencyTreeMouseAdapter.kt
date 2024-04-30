package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.DependencyTreeUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 14:38
 */
class DependencyTreeMouseAdapter(private val project: Project,private val jTree: JTree,private val panel:DependencyViewPanel): MouseAdapter(){
    override fun mouseClicked(e: MouseEvent) {
        if(e.button == MouseEvent.BUTTON3 && DependencyTreeUtil.isDependOnOrByNode(getDependencyNode(e))){
            showNodePopup(e)
            return
        }

        if(e.button == MouseEvent.BUTTON1 && e.clickCount == 1 && DependencyTreeUtil.isDependOnOrByNode(getDependencyNode(e))){
            openInEditor(e)
            return
        }
    }

    private fun openInEditor(e: MouseEvent) {
        val fileTreeNode = getDependencyNode(e)
        fileTreeNode?.let {
            val file = fileTreeNode.file
            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
            virtualFile?.let {
                FileEditorManager.getInstance(project).openFile(virtualFile,true)
            }
        }
    }

    private fun showNodePopup(e: MouseEvent) {
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToAnalyse(e))
        popupMenu.show(e.component, e.x, e.y)
    }



    private fun createItemToAnalyse(e: MouseEvent): JMenuItem {
        val analyseItem = JMenuItem("可拖拽至待分析或模块视图中")
        analyseItem.isEnabled = false
        return analyseItem
    }

    private fun getTreeNode(e: MouseEvent): DefaultMutableTreeNode? {
        val treePath = jTree.getPathForLocation(e.x, e.y) ?: return null
        return treePath.lastPathComponent as DefaultMutableTreeNode
    }

    private fun getDependencyNode(e:MouseEvent):FileDependencyTreeNode?{
        val treeNode = getTreeNode(e)
        return treeNode?.userObject as FileDependencyTreeNode?
    }
}
