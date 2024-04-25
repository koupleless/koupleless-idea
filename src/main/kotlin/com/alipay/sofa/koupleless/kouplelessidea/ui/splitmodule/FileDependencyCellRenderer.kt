package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnOrByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEIcons
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.DependencyTreeUtil
import com.intellij.icons.AllIcons
import java.awt.Component
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.UIManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/23 18:47
 */
class FileDependencyCellRenderer: DefaultTreeCellRenderer() {
    // 需要用IDEA自己的IconLoader加载Icon，否则不生效
    private val DEPEND_BY_ROOT_ICON = IDEIcons.load("images/showAutowiredCandidates.svg")
    private val DEPEND_ON_ROOT_ICON = IDEIcons.load("images/showAutowiredDependencies.svg")
    private val IN_MODULE_ICON = IDEIcons.load("images/InModuleIcon.svg")
    private val SHOULD_NOT_IN_MODULE_ICON = IDEIcons.load("images/balloonError.svg")
    private val CAN_MOVE_TO_MODULE_ICON = IDEIcons.load("images/balloonInformation.svg")
    private val SHOULD_ANALYSE_MORE_ICON = IDEIcons.load("images/balloonWarning.svg")
    override fun getTreeCellRendererComponent(
        tree: JTree, value: Any?,
        sel: Boolean,
        expanded: Boolean,
        leaf: Boolean, row: Int,
        hasFocus: Boolean
    ): Component?{
        setTip(value)

        val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        // 设置 icon
        if(value is DefaultMutableTreeNode && value.userObject!=null && value.userObject is FileDependencyTreeNode){
            var icon: Icon?
            icon = if (leaf) {
                getLeafIcon()
            } else if (expanded) {
                getOpenIcon()
            } else {
                getClosedIcon()
            }

            val node = value.userObject as FileDependencyTreeNode
            icon = if(DependencyTreeUtil.isSubRoot(node)){
                AllIcons.Nodes.Class
            } else if(DependencyTreeUtil.isDependByRoot(node)){
                DEPEND_BY_ROOT_ICON
            } else if(DependencyTreeUtil.isDependOnRoot(node)){
                DEPEND_ON_ROOT_ICON
            } else if(node is FileDependOnOrByTreeNode && node.isModuleSafe() && node.inModule){
                IN_MODULE_ICON
            } else if(node is FileDependOnOrByTreeNode && node.isModuleSafe() && !node.inModule){
                CAN_MOVE_TO_MODULE_ICON
            } else if(node is FileDependOnOrByTreeNode && node.moreThanOneDependBy()){
                SHOULD_NOT_IN_MODULE_ICON
            } else if(node is FileDependOnOrByTreeNode && !node.moreThanOneDependBy()){
                SHOULD_ANALYSE_MORE_ICON
            }
            else{
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

    private fun setTip(value: Any?){
        // 设置 tip
        if(value is DefaultMutableTreeNode && value.userObject!=null && value.userObject is FileDependOnTreeNode){
            val node = value.userObject as FileDependOnTreeNode
            if(node.isModuleSafe() && node.inModule){
                toolTipText = "已在模块"
            }

            if(node.inModule && !node.isModuleSafe()){
                toolTipText = "已在模块，但存在风险，因被基座依赖：被 ${node.dependByAppBeanNum} 个基座 Bean 依赖，被 ${node.dependByAppClassNum} 个基座类依赖"
            }

            if(node.isModuleSafe() && !node.inModule){
                toolTipText = "仅被模块依赖，建议移动至模块：被 ${node.dependByBeanNum} 个 Bean 依赖，被 ${node.dependByClassNum} 个类依赖"
            }

            if(!node.isModuleSafe() && !node.moreThanOneDependBy()){
                toolTipText = "被基座少量依赖，请分析被依赖：仅被 ${node.dependByAppBeanNum} 个基座 Bean 依赖，仅被 ${node.dependByAppClassNum} 个基座类依赖"
            }

            if(!node.isModuleSafe() && node.moreThanOneDependBy()){
                toolTipText = "被基座依赖：被 ${node.dependByAppBeanNum} 个基座 Bean 依赖，被 ${node.dependByAppClassNum} 个基座类依赖"
            }
        }

        if(value is DefaultMutableTreeNode && value.userObject!=null && value.userObject is FileDependByTreeNode){
            val node = value.userObject as FileDependByTreeNode
            if(node.isModuleSafe() && !node.inModule){
                toolTipText = "仅被模块依赖，建议移动至模块：被 ${node.dependByBeanNum} 个 Bean 依赖，被 ${node.dependByClassNum} 个类依赖"
            }

            if(!node.isModuleSafe() && !node.moreThanOneDependBy()){
                toolTipText = "被基座少量依赖，请分析被依赖：仅被 ${node.dependByAppBeanNum} 个基座 Bean 依赖，仅被 ${node.dependByAppClassNum} 个基座类依赖"
            }

            if(!node.isModuleSafe() && node.moreThanOneDependBy()){
                toolTipText = "被基座依赖：被 ${node.dependByAppBeanNum} 个基座 Bean 依赖，被 ${node.dependByAppClassNum} 个基座类依赖"
            }
        }

        if(value is DefaultMutableTreeNode && value.userObject!=null && value.userObject is FileDependencyTreeNode){
            val node = value.userObject as FileDependencyTreeNode
            if(node.isCopied() && DependencyTreeUtil.isDependOnOrByNode(node) && node.children.size!=0){
                toolTipText += "。深层依赖关系见树中其它同名节点"
            }
        }
    }
}
