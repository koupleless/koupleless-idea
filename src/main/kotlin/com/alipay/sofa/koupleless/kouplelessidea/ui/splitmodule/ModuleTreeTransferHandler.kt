package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.DependencyTreeUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/30 13:35
 */
class ModuleTreeTransferHandler(private val moduleTreeOperator:ModuleTreeOperator): TransferHandler(){
    override fun importData(support: TransferSupport?): Boolean {
        support?:return false

        val selectedFiles = support.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
        if(selectedFiles.isEmpty()) return false

        val nodePath = (support.component as JTree).selectionPath
        nodePath?:return false

        val treeNode = nodePath.lastPathComponent as DefaultMutableTreeNode
        val fileNode = treeNode.userObject as FileWrapperTreeNode

        if(ModuleTreeUtil.isPackage(fileNode)){
            return moduleTreeOperator.importForPackage(treeNode,selectedFiles)
        }
        if(ModuleTreeUtil.isResourceDir(fileNode)){
            return moduleTreeOperator.importForResourceDir(treeNode,selectedFiles)
        }
        if(ModuleTreeUtil.isBundleRoot(fileNode)|| ModuleTreeUtil.isBundle(fileNode)){
            return moduleTreeOperator.importForBundle(treeNode,selectedFiles)
        }
        if(ModuleTreeUtil.allowToAddVirtualNormalFolder(fileNode)){
            return moduleTreeOperator.importForNormalFolder(treeNode,selectedFiles)
        }
        return false
    }

    override fun canImport(support: TransferSupport?): Boolean {
        support?:return false
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
    }

    override fun getSourceActions(c: JComponent?): Int {
        return COPY
    }

    override fun createTransferable(c: JComponent?): Transferable? {
        if(c !is JTree) return null

        val selectedFiles = c.selectionPaths?.map {
            it.lastPathComponent as DefaultMutableTreeNode
        }?.filter {
            ModuleTreeUtil.isJavaFile(it.userObject as FileWrapperTreeNode)
        }?.mapNotNull {
            (it.userObject as FileWrapperTreeNode).srcFile
        }?.toList() ?: emptyList()

        val transferable = object : Transferable {

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
