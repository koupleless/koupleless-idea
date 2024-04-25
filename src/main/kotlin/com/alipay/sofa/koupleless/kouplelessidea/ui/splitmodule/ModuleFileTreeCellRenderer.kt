package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isBundle
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isFolder
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isJavaFile
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isPackage
import com.intellij.icons.AllIcons.Nodes.*
import java.awt.Component
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.UIManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import com.intellij.icons.AllIcons.FileTypes.Java

/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/14 10:11
 */
class ModuleFileTreeCellRenderer: DefaultTreeCellRenderer() {

    override fun getTreeCellRendererComponent(
        tree: JTree, value: Any?,
        sel: Boolean,
        expanded: Boolean,
        leaf: Boolean, row: Int,
        hasFocus: Boolean
    ): Component?{
        val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
        if(value is DefaultMutableTreeNode && value.userObject!=null && value.userObject is FileWrapperTreeNode){
            var icon: Icon? = null
            icon = if (leaf) {
                getLeafIcon()
            } else if (expanded) {
                getOpenIcon()
            } else {
                getClosedIcon()
            }

            val fileWrapper = value.userObject as FileWrapperTreeNode
            icon = if(isPackage(fileWrapper)){
                Package
            } else if(isBundle(fileWrapper)){
                Module
            } else if(isFolder(fileWrapper)){
                Folder
            } else if(isJavaFile(fileWrapper)){
                Java
            }else{
                icon
            }


            if (!tree.isEnabled) {
                isEnabled = false
                val laf = UIManager.getLookAndFeel()
                val disabledIcon = laf.getDisabledIcon(tree, icon)
                if (disabledIcon != null) icon = disabledIcon
                setDisabledIcon(icon)
            } else {
                isEnabled = true
                setIcon(icon)
            }
        }

        return component
    }
}
