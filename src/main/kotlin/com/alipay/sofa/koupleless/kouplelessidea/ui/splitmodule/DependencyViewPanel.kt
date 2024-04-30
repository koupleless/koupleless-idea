package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnOrByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.DependencyTreeUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.ui.VerticalFlowLayoutPanel
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Font
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/21 21:20
 */
class DependencyViewPanel(private val project: Project): BorderLayoutPanel() {
    private val activateButton = JButton("激活")
    private val stopButton = JButton("停止")

    private val fileField = JBTextField("请先激活")
    private var currentFile:File?= null
    private val analyseButton = JButton("分析依赖")
    private val updateButton = JButton("刷新")
    private var activated = false

    private val jTrees: JTree = JTree(DefaultMutableTreeNode())
    private val dependencyTreeOperator = DependencyTreeOperator(project,jTrees)
    private val dependencyTreeMouseAdapter = DependencyTreeMouseAdapter(project,jTrees,this)

    private val clearButton = JButton("清空")

    init {
        // 分析框
        val topPanel = VerticalFlowLayoutPanel()
        stopButton.isEnabled = false
        topPanel.add(BorderLayoutPanel().addToLeft(JBLabel("分析文件依赖")).addToRight(BorderLayoutPanel().addToLeft(activateButton).addToRight(stopButton)))

        val fileLabel = JBLabel("待分析文件：")
        fileLabel.foreground = JBColor.GRAY
        fileLabel.font = Font(fileLabel.font.name, Font.ITALIC, 12)

        fileField.transferHandler = FileFieldTransferHandler(this)
        fileField.foreground = JBColor.GRAY
        fileField.font = Font(fileField.font.name, Font.ITALIC, 12)
        fileField.isEnabled = false
        analyseButton.isEnabled = false
        updateButton.isEnabled = false

        topPanel.add(BorderLayoutPanel().addToLeft(fileLabel).addToCenter(fileField).addToRight(BorderLayoutPanel().addToLeft(analyseButton).addToRight(updateButton)))
        this.addToTop(topPanel)

        // 分析结果
        jTrees.isRootVisible = false
        jTrees.cellRenderer = FileDependencyCellRenderer()
        jTrees.transferHandler = TreeNodeTransferHandler()
        jTrees.dragEnabled = true
        jTrees.selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
        ToolTipManager.sharedInstance().registerComponent(jTrees) // 用于展示提示
        jTrees.addMouseListener(dependencyTreeMouseAdapter)
        val scrollPane = JBScrollPane(jTrees)
        this.addToCenter(scrollPane)

        // 清空
        val bottomPanel = VerticalFlowLayoutPanel()
        bottomPanel.add(BorderLayoutPanel().addToRight(BorderLayoutPanel().addToRight(clearButton)))
        clearButton.addActionListener{
            dependencyTreeOperator.clear()
        }

        this.addToBottom(bottomPanel)
        border = BorderFactory.createEmptyBorder(0,10,5,10)
    }

    fun showDependencies(file:File, subTrees: List<FileDependencyTreeNode>) {
        val subRootNode = FileDependencyTreeNode.DefaultSubRootNode(file)
        val dependOnRootNode = FileDependencyTreeNode.DefaultDependOnRootNode()
        val dependByRootNode = FileDependencyTreeNode.DefaultDependByRootNode()

        val dependOnTree = subTrees.firstOrNull { it is FileDependOnTreeNode  }
        dependOnTree?.let{ dependOnRootNode.addChild(dependOnTree) }
        val dependByTree = subTrees.firstOrNull{ it is FileDependByTreeNode }
        dependByTree?.let{ dependByRootNode.addChild(dependByTree) }

        subRootNode.addChildren(listOf(dependOnRootNode,dependByRootNode))
        dependencyTreeOperator.appendSubTree(subRootNode)
    }

    fun activating(){
        stopButton.isEnabled = true
    }

    fun activated(){
        activated = true

        analyseButton.isEnabled = true
        updateButton.isEnabled = true
        fileField.text = "请拖入待分析文件"
        fileField.isEnabled = true
        stopButton.isEnabled = false
    }

    fun getCurrentFile():File?{
        return currentFile
    }

    fun addActivateListener(l: ActionListener) {
        activateButton.addActionListener {e->
            activateButton.isEnabled = false
            fileField.isEnabled = false
            activateButton.text = "重新激活"
            analyseButton.isEnabled = false
            updateButton.isEnabled = false
            l.actionPerformed(e)
        }
    }

    fun addRefreshListener(l:ActionListener){
        updateButton.addActionListener {e->
            updateButton.isEnabled = false
            l.actionPerformed(e)
            updateButton.isEnabled = true
        }
    }

    fun addAnalyseListener(l: ActionListener) {
        analyseButton.addActionListener {e->
            analyseButton.isEnabled = false
            updateButton.isEnabled = false
            l.actionPerformed(e)
            analyseButton.isEnabled = true
            updateButton.isEnabled = true
        }
    }

    fun setCurrentFile(file: File): Boolean {
        currentFile = file
        fileField.text = file.name
        return true
    }

    fun reset(){
        dependencyTreeOperator.clear()
        activated = false
        disableAll()
        activateButton.isEnabled = true
    }

    fun disableAll() {
        jTrees.isEnabled = false
        clearButton.isEnabled = false
        activateButton.isEnabled = false
        analyseButton.isEnabled = false
        updateButton.isEnabled = false
        stopButton.isEnabled = false
    }

    fun enableAll(){
        jTrees.isEnabled = true
        clearButton.isEnabled = true
        activateButton.isEnabled = true
        analyseButton.isEnabled = activated
        fileField.isEnabled = activated
        updateButton.isEnabled = activated
        stopButton.isEnabled = false
    }

    fun getAllDependNodes(): List<FileDependOnOrByTreeNode> {
        return dependencyTreeOperator.getAllDependOnOrByTreeNodes()
    }

    fun refresh(){
        jTrees.revalidate()
        jTrees.repaint()
    }

    class FileFieldTransferHandler(private val panel: DependencyViewPanel):TransferHandler(){

        override fun importData(support: TransferSupport?): Boolean {
            support?:return false

            val selectedFiles = support.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
            val selectedFile = selectedFiles.first()

            return panel.setCurrentFile(selectedFile)
        }

        override fun canImport(support: TransferSupport?): Boolean {
            support?:return false

            // fileField 可用
            if(!panel.fileField.isEnabled) return false

            // 是 java 文件
            val isJavaFile = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
            if (!isJavaFile) return false

            // 只选了一个 java 文件
            val selectedFiles = support.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
            return selectedFiles.size == 1
        }
    }

    private class TreeNodeTransferHandler:TransferHandler(){
        override fun getSourceActions(c: JComponent?): Int {
            return COPY
        }

        override fun createTransferable(c: JComponent?): Transferable? {
            if(c !is JTree) return null

            val selectedFiles = c.selectionPaths?.map {
                it.lastPathComponent as DefaultMutableTreeNode
            }?.filter {
                DependencyTreeUtil.isDependOnOrByNode(it.userObject as FileDependencyTreeNode?) || DependencyTreeUtil.isSubRoot(it.userObject as FileDependencyTreeNode?)
            }?.map {
                (it.userObject as FileDependencyTreeNode).file
            }?.toList() ?: emptyList()

            val transferable = object : Transferable{

                override fun getTransferDataFlavors(): Array<DataFlavor> {
                    return arrayOf(DataFlavor.javaFileListFlavor)
                }

                override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
                    return DataFlavor.javaFileListFlavor.equals(flavor)
                }

                override fun getTransferData(flavor: DataFlavor?): Any {
                    return selectedFiles
                }
            }

            return transferable
        }
    }
}
