package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.ModuleDescriptionInfo
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.ui.MutableTreeUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.ui.VerticalFlowLayoutPanel
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Font
import java.awt.event.ActionListener
import java.nio.charset.Charset
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel


/**
 * @description: 模块视图面板：展示模块视图、搜索框、模块视图相关按钮，可以增、删、改、查模块节点
 * @author lipeng
 * @date 2023/10/20 15:22
 */
class ModuleViewPanel(private val project: Project): BorderLayoutPanel() {
    private val jTrees: JTree = JTree(DefaultMutableTreeNode())
    private val fieldToSearch: JTextField = JTextField()
    private val moduleTreeOperator = ModuleTreeOperator(project,jTrees)
    private val moduleTreeMouseAdapter = ModuleTreeMouseAdapter(project,jTrees,moduleTreeOperator)
    private val clearButton = JButton("清空")
    private val saveButton = JButton("保存")
    private val importButton = JButton("导入")
    private var moduleDescriptionInfo: ModuleDescriptionInfo? = null

    init {
        // 搜索框
        val searchTitle = JBLabel("搜索模块中的文件")
        searchTitle.foreground = JBColor.GRAY
        searchTitle.font = Font(searchTitle.font.name, Font.ITALIC, 12)
        fieldToSearch.foreground = JBColor.GRAY
        fieldToSearch.font = Font(fieldToSearch.font.name, Font.ITALIC, 12)
        fieldToSearch.addActionListener {
            val text = fieldToSearch.text.trim().lowercase()
            if(StrUtil.isNotBlank(text)){
                val root = (jTrees.model as DefaultTreeModel).root  as DefaultMutableTreeNode
                val searchedNode = MutableTreeUtil.search(root,text)
                searchedNode?.let {
                    val searchedPath = (jTrees.model as DefaultTreeModel).getPathToRoot(searchedNode)
                    if(searchedPath!=null&& searchedPath.isNotEmpty()){
                        val treePath = TreePath(searchedPath)
                        jTrees.selectionPath = treePath
                        jTrees.scrollPathToVisible(treePath)
                    }
                }
            }
        }

        // 模块视图
        jTrees.cellRenderer = ModuleFileTreeCellRenderer()
        jTrees.isRootVisible = true
        jTrees.selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
        jTrees.dragEnabled = true
        jTrees.transferHandler = ModuleTreeTransferHandler(moduleTreeOperator)
        jTrees.addMouseListener(moduleTreeMouseAdapter)

        // 按钮
        importButton.addActionListener{e->
            showImportPopup()
        }

        saveButton.addActionListener{
            val timestamp = System.currentTimeMillis()
            val moduleName = getModuleName()
            val fileName = if(moduleName==null) {
                "split_default_module_view${timestamp}.json"
            }else{
                "split_${moduleName}_view${timestamp}.json"
            }

            val targetPath = StrUtil.join(IDEConstants.SEPARATOR,project.basePath,"..",".KouplelessIDE_Module_View",fileName)
            val str = JSON.toJSONString(moduleTreeOperator.moduleTree)
            FileUtil.writeString(str,targetPath, Charset.forName("UTF-8"))
            val contentPanel = project.service<ContentPanel>()
            contentPanel.printMavenLog("模块视图保存成功：$targetPath")
        }

        val subBorder = VerticalFlowLayoutPanel()
        subBorder.add(BorderLayoutPanel().addToLeft(JBLabel("选择模块文件")))
        subBorder.add(BorderLayoutPanel().addToLeft(searchTitle).addToCenter(fieldToSearch))
        this.addToTop(subBorder)

        val scrollPane = JBScrollPane(jTrees)
        this.addToCenter(scrollPane)

        val subBorder1 = VerticalFlowLayoutPanel()
        subBorder1.add(BorderLayoutPanel().addToRight(BorderLayoutPanel().addToLeft(importButton).addToCenter(saveButton).addToRight(clearButton)))
        this.addToBottom(subBorder1)
        border = BorderFactory.createEmptyBorder(5,10,5,10)
    }

    private fun showImportPopup() {
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToImportFile())
        popupMenu.add(createItemToImportModuleView())
        popupMenu.show(importButton, importButton.x, importButton.y)

    }

    private fun createItemToImportModuleView(): JMenuItem {
        val item = JMenuItem("导入模块视图")
        item.addActionListener {
            moduleTreeOperator.importModuleView()
        }
        return item
    }

    private fun createItemToImportFile(): JMenuItem {
        val item = JMenuItem("导入应用文件")
        item.isEnabled = false
        item.toolTipText = "请在左侧 Project 面板选择文件，右键导入"
        return item
    }

    fun resetModuleTree(moduleDescriptionInfo: ModuleDescriptionInfo) {
        moduleTreeOperator.reset(moduleDescriptionInfo)
    }

    fun addClearActionListener(l: ActionListener){
        clearButton.addActionListener{ e->
            l.actionPerformed(e)
        }
    }

    fun disableAll() {
        jTrees.isEnabled = false
        clearButton.isEnabled = false
        saveButton.isEnabled = false
        importButton.isEnabled = false
        fieldToSearch.isEnabled = false
        IDEConstants.allowMovingToModule.set(false)
    }

    fun enableAll(){
        jTrees.isEnabled = true
        clearButton.isEnabled = true
        saveButton.isEnabled = true
        importButton.isEnabled = true
        fieldToSearch.isEnabled = true
        IDEConstants.allowMovingToModule.set(true)
    }

    private fun getModuleName():String?{
        return moduleDescriptionInfo?.name
    }

    private fun getPackageName():String?{
        return moduleDescriptionInfo?.packageName
    }

    fun getModuleTreeRoot(): FileWrapperTreeNode? {
        return moduleTreeOperator.moduleTree
    }

    fun setModuleDescriptionInfo(info:ModuleDescriptionInfo){
        moduleDescriptionInfo = info
    }
}
