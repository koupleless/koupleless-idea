package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import cn.hutool.core.io.FileUtil

import com.alibaba.fastjson.JSON
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.ModuleDescriptionInfo
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.ui.JTreeUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTemplateUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import java.io.File
import java.nio.charset.Charset
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/30 13:07
 */
class ModuleTreeOperator(proj: Project, treeModel: JTree) {
    private val jTree = treeModel
    private val project = proj
    var moduleTree: FileWrapperTreeNode? = null
    private var moduleDescriptionInfo: ModuleDescriptionInfo? = null

    fun reset(info: ModuleDescriptionInfo) {
        this.moduleDescriptionInfo = info
        requireNotNull(info.templateType){"模板类型不能为空"}
        requireNotNull(info.name){"模板名称不能为空"}
        requireNotNull(info.packageName){"模板包名不能为空"}

        val moduleTree = ModuleTemplateUtil.buildModuleTree(info.templateType,info.name,info.packageName)
        this.updateModuleView(moduleTree)
    }

    fun importForBundle(treeNode: DefaultMutableTreeNode,selected:List<File>):Boolean{
        if(selected.isEmpty()) return false

        // 先检查是否满足导入条件：如果不是bundle根目录或者bundle，则不允许
        val fileNode = treeNode.userObject as FileWrapperTreeNode
        if(!ModuleTreeUtil.isBundleRoot(fileNode)&& !ModuleTreeUtil.isBundle(fileNode)){
            printWarning("${fileNode.getName()} 无法导入 bundle，请选择 bundle 或根目录")
            return false
        }

        // 1. 导入bundles
        val selectedToImport = selected.toMutableList()
        val whitelistForBundles = selected.map { it.absolutePath}.toSet()
        val selectedBundles = selected.filter { FileParseUtil.isBundle(it.absolutePath) }
        val selectedParentBundles = filterParentFolders(selectedBundles)
        importBundles(treeNode,selectedBundles,whitelistForBundles)

        // 2. 导入普通文件夹
        selectedParentBundles.forEach { bundle ->
            selectedToImport.removeIf { FileParseUtil.folderContainsFile(bundle, it) }
        }
        val selectedNormalDirs = selectedToImport.filter { FileParseUtil.isNormalFolder(it) }
        val selectedInNormalDirs = selectedToImport.filter {file->
            selectedNormalDirs.any { dir->FileParseUtil.folderContainsFile(dir,file) } }.toList()
        importForNormalFolder(treeNode,selectedInNormalDirs)

        // 3. 导入文件
        selectedNormalDirs.forEach {dir ->
            selectedToImport.removeIf{ FileParseUtil.folderContainsFile(dir,it) }
        }
        val selectedFiles = selectedToImport.filter { FileParseUtil.isValidFile(it) }
        importFiles(treeNode,selectedFiles)

        // 4. 打印日志：已导入的文件
        val imported = mutableListOf<String>()
        selectedParentBundles.mapTo(imported){it.absolutePath}
        selectedFiles.mapTo(imported){it.absolutePath}
        val tip = "${fileNode.getName()} 已导入以下文件夹/文件：${imported.joinToString(",", prefix = "[", postfix = "]")}"
        printTip(tip)

        return true
    }

    private fun importBundles(nodeTree: DefaultMutableTreeNode,selectedBundles: List<File>, whitelist: Set<String>) {
        selectedBundles.forEach { bundle ->
            importBundle(nodeTree,bundle,whitelist)
        }
    }

    private fun importBundle(parentTreeNode:DefaultMutableTreeNode,bundle:File,whitelist: Set<String>?){
        val parentFileNode = parentTreeNode.userObject as FileWrapperTreeNode

        val whitelistForBundle = getWhitelist(bundle,whitelist)
        val newFileNode= ModuleTreeUtil.importBundle(parentFileNode, bundle, getModulePackageName(), whitelistForBundle)
        val newTreeNode = buildDefaultMutableTree(newFileNode)
        insertNode(parentTreeNode,newTreeNode)
    }

    fun importForResourceDir(treeNode: DefaultMutableTreeNode, selected:List<File>):Boolean{
        if(selected.isEmpty()) return false
        // 0. 先检查是否满足导入条件
        val fileNode = treeNode.userObject as FileWrapperTreeNode
        if(!ModuleTreeUtil.isResourceDir(fileNode)){
            printWarning("${fileNode.getName()} 无法导入资源，请选择资源目录")
            return false
        }
        // 1. 过滤出需要导入的文件夹
        val selectedToImport = selected.toMutableList()
        val selectedResourceDirs = selectedToImport.filter { FileParseUtil.isResourceDir(it) }
        val selectedParentResourceDirs = filterParentFolders(selectedResourceDirs)

        // 2. 过滤出需要导入的文件
        val selectedResourceFiles = selected.filter { FileParseUtil.isResourceFile(it) }.toMutableList()
        selectedParentResourceDirs.forEach { resourceDir ->
            selectedResourceFiles.removeIf {
                FileParseUtil.folderContainsFile(resourceDir, it)
            }
        }

        // 3. 导入文件
        importFiles(treeNode,selectedResourceFiles)

        // 4. 导入其它资源文件夹
        val whitelist = selected.map { it.absolutePath }.toSet()
        importResourceDirs(treeNode,selectedParentResourceDirs,whitelist)

        // 5. 打印日志：已导入的文件
        val imported = mutableListOf<String>()
        selectedParentResourceDirs.mapTo(imported){it.absolutePath}
        selectedResourceFiles.mapTo(imported){it.absolutePath}
        val tip = "${fileNode.getName()} 已导入以下文件夹/文件：${imported.joinToString(",", prefix = "[", postfix = "]")}"
        printTip(tip)
        return true
    }

    private fun importResourceDirs(treeNode: DefaultMutableTreeNode, selected: List<File>, whitelist: Set<String>) {
        selected.forEach {
            importResourceDir(treeNode,it,whitelist)
        }
    }

    private fun importResourceDir(parentTreeNode: DefaultMutableTreeNode, dir: File, whitelist: Set<String>) {
        val parentFileNode = parentTreeNode.userObject as FileWrapperTreeNode
        val whitelistForResourceDir = getWhitelist(dir,whitelist)

        val newResourceDirNode = ModuleTreeUtil.importResourceDir(parentFileNode,dir,whitelistForResourceDir)
        val newResourceDirTreeNode = buildDefaultMutableTree(newResourceDirNode)
        insertNode(parentTreeNode,newResourceDirTreeNode)
    }


    fun importForNormalFolder(treeNode: DefaultMutableTreeNode,selected:List<File>):Boolean{
        if(selected.isEmpty()) return false
        // 0. 先检查是否满足导入条件
        val fileNode = treeNode.userObject as FileWrapperTreeNode
        if(!ModuleTreeUtil.allowToAddVirtualNormalFolder(fileNode)){
            printWarning("${fileNode.getName()} 无法导入，请选择普通文件夹")
            return false
        }

        // 1. 过滤出需要导入的文件夹
        val selectedToImport = selected.toMutableList()
        val selectedNormalDirs = selectedToImport.filter { FileParseUtil.isNormalFolder(it) }
        val selectedParentNormalDirs = filterParentFolders(selectedNormalDirs)

        // 2. 过滤出需要导入的文件
        val selectedFiles = selected.filter { FileParseUtil.isValidFile(it) }.toMutableList()
        selectedParentNormalDirs.forEach { normalDir ->
            selectedFiles.removeIf {
                FileParseUtil.folderContainsFile(normalDir, it)
            }
        }

        // 3. 导入文件
        importFiles(treeNode,selectedFiles)

        // 4. 导入其它普通文件夹
        val whitelist = selected.map { it.absolutePath }.toSet()
        importNormalDirs(treeNode,selectedParentNormalDirs,whitelist)

        val imported = mutableListOf<String>()
        selectedParentNormalDirs.mapTo(imported){it.absolutePath}
        selectedFiles.mapTo(imported){it.absolutePath}
        val tip = "${fileNode.getName()} 已导入以下文件夹/文件：${imported.joinToString(",", prefix = "[", postfix = "]")}"
        printTip(tip)
        return true
    }

    private fun importNormalDirs(treeNode: DefaultMutableTreeNode, selected: List<File>, whitelist: Set<String>) {
        selected.forEach {
            importNormalDir(treeNode,it,whitelist)
        }
    }

    private fun importNormalDir(treeNode: DefaultMutableTreeNode, dir: File, whitelist: Set<String>) {
        val parentFileNode = treeNode.userObject as FileWrapperTreeNode
        val whitelistForNormalDir = getWhitelist(dir,whitelist)
        val newNormalDirNode = ModuleTreeUtil.importVirtualDir(parentFileNode,dir,whitelistForNormalDir)
        val newNormalDirTreeNode = buildDefaultMutableTree(newNormalDirNode)
        insertNode(treeNode,newNormalDirTreeNode)
    }

    fun importForPackage(treeNode: DefaultMutableTreeNode,selected:List<File>):Boolean{
        if(selected.isEmpty()) return false

        // 0. 先检查是否满足导入条件
        val fileNode = treeNode.userObject as FileWrapperTreeNode
        if(!ModuleTreeUtil.isPackage(fileNode)){
            printWarning("${fileNode.getName()} 不是包目录，请选择包目录")
            return false
        }

        // 1. 过滤出需要导入的包
        val selectedToImport = selected.toMutableList()
        val selectedPackages = selectedToImport.filter { FileParseUtil.isPackage(it) }
        val selectedParentPackages = filterParentFolders(selectedPackages)

        // 2. 过滤出需要导入的文件
        val selectedJavaFiles = selected.filter { FileParseUtil.isFileInJavaRoot(it) }.toMutableList()
        // 过滤出不属于 package 的 资源文件夹, 普通文件夹 和 files
        selectedParentPackages.forEach { packageFile ->
            selectedJavaFiles.removeIf {
                FileParseUtil.folderContainsFile(packageFile, it)
            }
        }

        // 3. 先导入文件
        importFiles(treeNode,selectedJavaFiles)

        // 4. 再导入其它包
        val whitelist = selected.map { it.absolutePath }.toSet()
        importPackages(treeNode,selectedParentPackages,whitelist)

        // 5. 打印日志：已导入的文件
        val imported = mutableListOf<String>()
        selectedParentPackages.mapTo(imported){it.absolutePath}
        selectedJavaFiles.mapTo(imported){it.absolutePath}
        val tip = "${fileNode.getName()} 已导入以下文件夹/文件：${imported.joinToString(",", prefix = "[", postfix = "]")}"
        printTip(tip)
        return true
    }

    private fun importFiles(parentTreeNode: DefaultMutableTreeNode, selected: List<File>) {
        val parentFileNode = parentTreeNode.userObject as FileWrapperTreeNode
        val importedFiles = ModuleTreeUtil.addVirtualFiles(parentFileNode, selected)
        importedFiles?.let {
            importedFiles.forEach {newFileNode->
                val newTreeNode = DefaultMutableTreeNode(newFileNode)
                insertNode(parentTreeNode,newTreeNode)
            }
        }
    }

    private fun importPackages(nodeTree:DefaultMutableTreeNode,selected:List<File>, whitelist: Set<String>?){
        val parentFileNode = nodeTree.userObject as FileWrapperTreeNode
        val packageNamePrefix = parentFileNode.getName()

        var currentNode = nodeTree
        selected.forEach { packageFile ->
            // 导入 package
            currentNode = importPackage(packageNamePrefix,currentNode,packageFile,whitelist)
        }
    }

    /**
     *
     * @param
     * @return 返回导入的包所在的父节点
     */
    private fun importPackage(packageNamePrefix:String,parentTreeNode:DefaultMutableTreeNode,packageFile: File,whitelist: Set<String>?):DefaultMutableTreeNode{
        val parentFileNode = parentTreeNode.userObject as FileWrapperTreeNode

        val whitelistForPackage = getWhitelist(packageFile,whitelist)
        val newFileNode = ModuleTreeUtil.importPackage(parentFileNode, packageNamePrefix, packageFile, whitelistForPackage)
        val newParentTreeNode = buildDefaultMutableTree(parentFileNode)
        replaceNode(parentTreeNode,newParentTreeNode)
        if(newFileNode!=parentFileNode){
            // 展示新节点
            val newTreeNode = getDefaultMutableTreeNode(newParentTreeNode,newFileNode)!!
            JTreeUtil.scrollPathTo(jTree,newTreeNode)
        }
        return newParentTreeNode
    }

    fun buildDefaultMutableTree(root: FileWrapperTreeNode): DefaultMutableTreeNode {
        val tree = DefaultMutableTreeNode(root)
        root.children.sortBy{it.getName()}
        for (child in root.children){
            tree.add(buildDefaultMutableTree(child))
        }
        return tree
    }

    private fun getWhitelist(folder:File,whitelist: Set<String>?):Set<String>?{
        whitelist?:return null

        val filesInFolder = whitelist.filter { FileParseUtil.fileInFolder(it,folder.absolutePath) }
        if(filesInFolder.isEmpty()){
            return null
        }
        val validWhitelist = filesInFolder.toMutableSet()
        validWhitelist.add(folder.absolutePath)
        return validWhitelist
    }

    private fun printTip(tip:String){
        val contentPanel = project.service<ContentPanel>()
        contentPanel.printMavenLog(tip)
    }

    private fun printWarning(warning:String){
        val contentPanel = project.service<ContentPanel>()
        contentPanel.printMavenErrorLog(warning)
    }

    private fun filterParentFolders(selected: List<File>): List<File> {
        val parentFolders = mutableMapOf<String,File>()
        val selectedFolders = selected.toMutableList()
        selectedFolders.forEach { folder->
            val folderIsNotParentFolder = parentFolders.any { (parentFolderPath,_)-> FileParseUtil.fileInFolder(folder.absolutePath,parentFolderPath) }
            if(folderIsNotParentFolder){
                return@forEach
            }

            val parentFoldersInFolder = parentFolders.filter { (parentFolderPath,_)-> FileParseUtil.fileInFolder(parentFolderPath,folder.absolutePath)}
            parentFoldersInFolder.forEach { (k, _) -> parentFolders.remove(k) }

            parentFolders[folder.absolutePath] = folder
        }
        return parentFolders.values.toList()
    }

    private fun getDefaultMutableTreeNode(root:DefaultMutableTreeNode,target:FileWrapperTreeNode,layer:Int=0):DefaultMutableTreeNode?{
        if(root.userObject==target){
            return root
        }

        // 防止爆栈
        if(layer >= SplitConstants.MAX_TREE_NODE_LAYER){
            return null
        }

        if(root.childCount>0){
            for (child in root.children()){
                val childNode = getDefaultMutableTreeNode(child as DefaultMutableTreeNode,target,layer+1)
                if(childNode!=null){
                    return childNode
                }
            }
        }

        return null
    }

    private fun getModulePackageName():String{
        return moduleDescriptionInfo!!.packageName!!
    }

    fun importModuleView(){
        val pathChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        pathChooserDescriptor.setRoots(project.baseDir)
        val virtualFile = FileChooser.chooseFile(pathChooserDescriptor, project, null)
        if(virtualFile!=null){
            val str = FileUtil.readString(virtualFile.path, Charset.forName("UTF-8"))
            val moduleTree = JSON.parseObject(str,FileWrapperTreeNode::class.java)
            val packageRootNodes = ModuleTreeUtil.getAllPackageRootNode(moduleTree)
            packageRootNodes.forEach {
                ModuleTreeUtil.modifyNodeName(it, getModulePackageName())
            }

            updateModuleView(moduleTree)
            printTip("模块视图导入成功")
        }
    }

    private fun updateModuleView(moduleTree: FileWrapperTreeNode){
        this.moduleTree = moduleTree
        val root = buildDefaultMutableTree(moduleTree)
        jTree.model = DefaultTreeModel(root,false)
        reloadTree()
    }

    /**
     * 刷新整棵树
     */
    private fun reloadTree(){
        // 通知 JTree 数据已更改
        (jTree.model as DefaultTreeModel).reload()

        // 展开所有节点
        val rootTreePath = jTree.getPathForRow(0)
        expandNodes(rootTreePath)
    }

    private fun expandNodes(parentPath: TreePath){
        val node = parentPath.lastPathComponent as DefaultMutableTreeNode
        val fileNode = node.userObject as FileWrapperTreeNode

        val notExpand = fileNode.parent?.isPackageRoot
        if(notExpand!=null && notExpand){
            return
        }

        for (child in node.children()){
            val childPath  = parentPath.pathByAddingChild(child)
            expandNodes(childPath)
        }
        jTree.expandPath(parentPath)
    }

    fun updateWholeTree(){
        updateModuleView(moduleTree!!)
    }

    /**
     * 修改：先删后增节点
     * @param
     * @return
     */
    fun replaceNode(old:DefaultMutableTreeNode, new:DefaultMutableTreeNode){
        JTreeUtil.modifyNode(jTree.model as DefaultTreeModel,old,new)
        JTreeUtil.scrollPathTo(jTree,new)
    }

    fun modifyNode(node:DefaultMutableTreeNode){
        JTreeUtil.modifyNode(jTree.model as DefaultTreeModel,node)
        JTreeUtil.scrollPathTo(jTree,node)
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

    fun deleteNode(node:DefaultMutableTreeNode){
        val parent = node.parent as DefaultMutableTreeNode
        JTreeUtil.deleteNode(jTree.model as DefaultTreeModel,node)
        JTreeUtil.updateSubNodes(jTree.model as DefaultTreeModel,parent)
    }
}
