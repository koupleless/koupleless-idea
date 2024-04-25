
package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.ui.TextPopupFactory
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.addPackageWithExpectPackageName
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.allowToAddVirtualNormalFolder
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.createResourceDir
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isBundleRoot
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isPackage
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isResourceDir
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isVirtualBundle
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.isVirtualFile
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.modifyNodeName
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.removePackage
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.removeTree
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.removeWholePackage
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.popup.AbstractPopup
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

/**
 *
 * @author lipeng
 * @version : ModuleTreeOperator, v 0.1 2023-09-05 10:58 lipeng Exp $
 */
class ModuleTreeMouseAdapter(proj:Project, treeModel: JTree, operator:ModuleTreeOperator): MouseAdapter(){
    private val jTree = treeModel
    private val project = proj
    private val moduleTreeOperator = operator
    private var modulePackageName:String?=null
    /**
     * 和 com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule.SplitStagePanel.mode 的默认值保持一致
     */
    private var mode = SplitConstants.Labels.MOVE_MODE

    private fun selectMultiNode():Boolean{
        val selectedPaths = jTree.selectionPaths
        selectedPaths?:return false

        return selectedPaths.size>1
    }

    private fun selectFiles():Boolean{
        val selectedPaths = jTree.selectionPaths
        selectedPaths?:return false

        if(selectedPaths.isEmpty()) return false
        selectedPaths.forEach {path ->
            path.lastPathComponent?.let {node ->
                val fileNode = getFileWrapperTreeNode(node as DefaultMutableTreeNode)
                fileNode?.let {
                    if(!isVirtualFile(fileNode)){
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun mouseClicked(e: MouseEvent) {
        // 右键多个文件+文件夹混合
        if(SwingUtilities.isRightMouseButton(e) && selectMultiNode() && (!selectFiles())){
            showMultiNodePopup(e)
            return
        }

        // 右键Package
        if (e.button == MouseEvent.BUTTON3 && isPackage(getFileWrapperTreeNode(e)) ) {
            showPackagePopup(e)
            return
        }

        // 右键文件
        if(SwingUtilities.isRightMouseButton(e) && selectFiles()){
            showVirtualFilePopup(e)
            return
        }

        // 右键 bundleRoot
        if(e.button == MouseEvent.BUTTON3 && isBundleRoot(getFileWrapperTreeNode(e))){
            showBundleRootPopup(e)
            return
        }

        // 右键 bundle
        if(e.button == MouseEvent.BUTTON3 && isVirtualBundle(getFileWrapperTreeNode(e))){
            showBundlePopup(e)
            return
        }

        // 右键 resourceDir
        if(e.button == MouseEvent.BUTTON3 && isResourceDir(getFileWrapperTreeNode(e))){
            showResourceDirPopup(e)
            return
        }
        
        // 右键 文件夹
        if(e.button == MouseEvent.BUTTON3 && allowToAddVirtualNormalFolder(getFileWrapperTreeNode(e))){
            showVirtualNormalFolderPopup(e)
            return
        }

        // 单击
        if(e.button == MouseEvent.BUTTON1 && e.clickCount == 1 && isVirtualFile(getFileWrapperTreeNode(e))){
            openInEditor(e)
        }
    }

    private fun openInEditor(e: MouseEvent) {
        val file = getFileWrapperTreeNode(e)?.srcFile
        file?:return

        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
        virtualFile?.let {
            FileEditorManager.getInstance(project).openFile(virtualFile,true)
        }
    }

    private fun showMultiNodePopup(e: MouseEvent) {
        // 只提供删除操作
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToRemoveMultiNode(e))
        popupMenu.show(e.component, e.x, e.y)
    }

    private fun createItemToRemoveMultiNode(e:MouseEvent): JMenuItem{
        val removeItem = JMenuItem("删除")
        val selectedPaths = jTree.selectionPaths!!
        removeItem.addActionListener {
            removeMultiNode(selectedPaths)
            moduleTreeOperator.updateWholeTree()
        }
        return removeItem
    }

    private fun removeMultiNode(selectedPaths: Array<TreePath>) {
        selectedPaths.forEach {path ->
            path.lastPathComponent?.let {node ->
                val fileNode = getFileWrapperTreeNode(node as DefaultMutableTreeNode)
                fileNode?.let {
                    removeTree(fileNode)
                }
            }
        }
    }

    private fun showVirtualNormalFolderPopup(e: MouseEvent) {
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToImport(e))
        popupMenu.add(createItemToRemoveNodeTree(e))
        popupMenu.show(e.component, e.x, e.y)
    }

    private fun showResourceDirPopup(e: MouseEvent) {
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToImport(e))
        popupMenu.add(createItemToNewResourceDir(e))

        val resourceDirNode = getFileWrapperTreeNode(e)!!
        if(resourceDirNode.isCustomFile){
            popupMenu.add(createItemToRename(e,"重命名"))
            popupMenu.add(createItemToRemoveNodeTree(e))
        }
        popupMenu.show(e.component, e.x, e.y)
    }

    private fun createItemToRemoveNodeTree(e: MouseEvent): JMenuItem {
        val removeItem = JMenuItem("删除")
        val jTreeNode = getTreeNode(e)!!
        val node = jTreeNode.userObject as FileWrapperTreeNode

        removeItem.addActionListener{
            removeTree(node)
            moduleTreeOperator.deleteNode(jTreeNode)
        }
        return removeItem
    }


    private fun createItemToNewResourceDir(e: MouseEvent): JMenuItem {
        val jTreeNode = getTreeNode(e)!!
        val rootNode = getFileWrapperTreeNode(e)!!
        val createItem = JMenuItem("新建文件夹")
        createItem.addActionListener {
            val popup = TextPopupFactory.createTextPopup("新建资源文件夹", "")
            popup.addListener(object : JBPopupListener {
                override fun onClosed(event: LightweightWindowEvent) {
                    if (event.isOk) {
                        val bundleNameToAdd = ((popup as AbstractPopup).component as JBTextArea).text
                        val subResourceDir = createResourceDir(rootNode,bundleNameToAdd)
                        val subResourceDirTreeNode = moduleTreeOperator.buildDefaultMutableTree(subResourceDir)
                        moduleTreeOperator.insertNode(jTreeNode,subResourceDirTreeNode)
                    }
                }
            })
            popup.showInFocusCenter()
        }
        return createItem
    }

    private fun showBundlePopup(e: MouseEvent) {
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToImport(e))
        popupMenu.add(createItemToRename(e,"重命名 bundle"))
        popupMenu.add(createItemToRemoveNodeTree(e))
        popupMenu.show(e.component, e.x, e.y)
    }

    private fun showBundleRootPopup(e: MouseEvent) {
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToImport(e))
        popupMenu.add(createItemToNewBundle(e))
        popupMenu.show(e.component, e.x, e.y)
    }

    private fun createItemToRename(e: MouseEvent,textAreaTip:String): JMenuItem {
        val modifyItem = JMenuItem("重命名")
        val jTreeNode = getTreeNode(e)!!
        val node = jTreeNode.userObject as FileWrapperTreeNode

        modifyItem.addActionListener{
            val popup = TextPopupFactory.createTextPopup(textAreaTip, node.getName())

            popup.addListener(object : JBPopupListener {
                override fun onClosed(event: LightweightWindowEvent) {
                    if (event.isOk) {
                        val newName = ((popup as AbstractPopup).component as JBTextArea).text.trim().replace("\n","")
                        if(modifyNodeName(node,newName)){
                            moduleTreeOperator.modifyNode(jTreeNode)
                        }else{
                            printWarning("重命名：不允许删除。如需删除，请点击'删除'")
                        }
                    }
                }
            })
            popup.showInFocusCenter()
        }

        return modifyItem
    }

    private fun createItemToNewBundle(e: MouseEvent): JMenuItem {
        val jTreeNode = getTreeNode(e)!!
        val rootNode = jTreeNode.userObject as FileWrapperTreeNode
        val createItem = JMenuItem("新建 Bundle")
        createItem.addActionListener {
            val popup = TextPopupFactory.createTextPopup("新建 Bundle", "")
            popup.addListener(object : JBPopupListener {
                override fun onClosed(event: LightweightWindowEvent) {
                    if (event.isOk) {
                        val bundleNameToAdd = ((popup as AbstractPopup).component as JBTextArea).text
                        val newBundle = createBundle(rootNode,bundleNameToAdd)
                        val newBundleTreeNode = moduleTreeOperator.buildDefaultMutableTree(newBundle)
                        moduleTreeOperator.insertNode(jTreeNode,newBundleTreeNode)
                    }
                }
            })
            popup.showInFocusCenter()
        }
        return createItem
    }

    private fun createBundle(parentNode: FileWrapperTreeNode, bundleName:String):FileWrapperTreeNode{
        return ModuleTreeUtil.createEmptyBundle(parentNode,bundleName,modulePackageName!!)
    }

    private fun getTreeNode(e: MouseEvent): DefaultMutableTreeNode? {
        val treePath = jTree.getPathForLocation(e.x, e.y) ?: return null
        return treePath.lastPathComponent as DefaultMutableTreeNode
    }

    private fun getFileWrapperTreeNode(e: MouseEvent):FileWrapperTreeNode?{
        return getFileWrapperTreeNode(getTreeNode(e))
    }

    private fun getFileWrapperTreeNode(node: DefaultMutableTreeNode?):FileWrapperTreeNode?{
        return node?.userObject as FileWrapperTreeNode?
    }

    private fun showVirtualFilePopup(e: MouseEvent){
        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToMarkAsCopy(e))
        popupMenu.add(createItemToRemoveNodeTree(e))
        popupMenu.show(e.component, e.x, e.y)
    }

    private fun createItemToMarkAsCopy(e: MouseEvent): JMenuItem {
        val markAsCopyItem = JMenuItem("标记为复制模式")
        val jTreeNode = getTreeNode(e)!!
        val node = jTreeNode.userObject as FileWrapperTreeNode

        markAsCopyItem.addActionListener{
            node.markedAsCopy = true
        }
        if(mode == SplitConstants.Labels.COPY_MODE){
            markAsCopyItem.isEnabled = false
            markAsCopyItem.toolTipText = "仅在拆分模式下有效"
        }
        return markAsCopyItem
    }

    private fun showPackagePopup(e: MouseEvent){
        val jTreeNode = getTreeNode(e)!!
        val packageNode = jTreeNode.userObject as FileWrapperTreeNode

        val popupMenu = JPopupMenu()
        popupMenu.add(createItemToImport(e))
        popupMenu.add(createItemToNewPackage(e))

        if(packageNode.isCustomFile){
            popupMenu.add(createItemToRename(e,"重命名 Package"))
            popupMenu.add(createItemToRemovePackageName(e))
            popupMenu.add(createItemToRemoveWholePackage(e))
        }
        popupMenu.show(e.component, e.x, e.y)
    }

    private fun createItemToRemoveWholePackage(e: MouseEvent): JMenuItem {
        val removeItem = JMenuItem("删除整个包")
        val jTreeNode = getTreeNode(e)!!
        val packageNode = getFileWrapperTreeNode(jTreeNode)!!

        removeItem.addActionListener{
            val parentNode = packageNode.parent!!
            val packageNodeRemoved = removeWholePackage(packageNode)
            if(packageNodeRemoved){
                // 如果删除了packageNode，则重新构建父节点
                val newParentTreeNode = moduleTreeOperator.buildDefaultMutableTree(parentNode)
                val oldParentTreeNode = jTreeNode.parent as DefaultMutableTreeNode
                moduleTreeOperator.replaceNode(oldParentTreeNode,newParentTreeNode)
            }else{
                // 如果只更改了packageNode，则重新构建该节点
                val newJTreeNode = moduleTreeOperator.buildDefaultMutableTree(packageNode)
                moduleTreeOperator.replaceNode(jTreeNode,newJTreeNode)
            }
        }
        return removeItem
    }

    private fun createItemToImport(e: MouseEvent):JMenuItem {
        val item = JMenuItem("导入文件：请从左侧面板中拖拽")
        item.isEnabled = false
        return item
    }


    private fun createItemToNewPackage(e: MouseEvent):JMenuItem{
        val curTreeNode = getTreeNode(e)!!
        val packageNode = getFileWrapperTreeNode(e)!!
        val createItem = JMenuItem("新建 Package")

        val currentPackageName = packageNode.getName()
        val firstPackageName = if(packageNode.getName().contains(".")){
            packageNode.getName().substringBefore(".")
        }else{
            packageNode.getName()
        }

        createItem.addActionListener {

            val popup = TextPopupFactory.createTextPopup("新建 Package", "${currentPackageName}.")
            popup.addListener(object : JBPopupListener {
                override fun onClosed(event: LightweightWindowEvent) {
                    if (event.isOk) {
                        val expectPackageName = (((popup as AbstractPopup).component as JBTextArea).text).trim().replace("\n","")
                        if(expectPackageName == currentPackageName || currentPackageName.contains("${expectPackageName}.")){
                            printWarning("新建 Package：不允许创建同名的 Package")
                            return
                        }

                        if(!expectPackageName.startsWith(firstPackageName)){
                            printWarning("新建 Package：必须以 $firstPackageName 开头")
                            return
                        }

                        val newPackageNode = addPackageWithExpectPackageName(packageNode,expectPackageName)
                        if(packageNode.getName()==currentPackageName){
                            // 没有修改当前节点，则仅创建新子节点，并插入
                            val newTreeNode = moduleTreeOperator.buildDefaultMutableTree(newPackageNode)
                            moduleTreeOperator.insertNode(curTreeNode,newTreeNode)
                        }else{
                            // 修改当前节点，则重新构建当前节点，并替换
                            val newTreeNode = moduleTreeOperator.buildDefaultMutableTree(packageNode)
                            moduleTreeOperator.replaceNode(curTreeNode,newTreeNode)
                        }
                    }
                }
            })
            popup.showInFocusCenter()
        }
        return createItem
    }

    private fun createItemToRemovePackageName(e: MouseEvent):JMenuItem{
        val removeItem = JMenuItem("删除包名")
        val jTreeNode = getTreeNode(e)!!
        val packageNode = getFileWrapperTreeNode(jTreeNode)!!

        removeItem.addActionListener{
            val parentNode = packageNode.parent!!
            val packageNodeRemoved = removePackage(packageNode)

            if(packageNodeRemoved){
                // 如果删除了packageNode，则构建父节点，更新父节点
                val newParentTreeNode = moduleTreeOperator.buildDefaultMutableTree(parentNode)
                val oldParentTreeNode = jTreeNode.parent as DefaultMutableTreeNode
                moduleTreeOperator.replaceNode(oldParentTreeNode,newParentTreeNode)
            }else{
                // 如果只更改了packageNode，则仅更新该节点
                moduleTreeOperator.modifyNode(jTreeNode)
            }
        }
        return removeItem
    }

    private fun printWarning(warning:String){
        val contentPanel = project.service<ContentPanel>()
        contentPanel.printMavenErrorLog(warning)
    }
}
