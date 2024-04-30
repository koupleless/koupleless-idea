
package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule


import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory
import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory.buildRelation
import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory.buildRelationInOrder
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.MAX_TREE_NODE_LAYER
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil.isParentBundle
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil.listDirectory
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil.listValidFiles
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil.parsePackageRoot
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil.parseResourceRoot
import java.io.File

/**
 *
 * @author lipeng
 * @version : ModuleTreeService, v 0.1 2023-09-05 16:35 lipeng Exp $
 */
object ModuleTreeUtil {

    private fun buildEmptyBundleTree(parentNode: FileWrapperTreeNode, bundleName: String, packageName: String):FileWrapperTreeNode{
        val bundleNode = buildEmptyBundleTree(bundleName,packageName)
        buildRelation(parentNode,bundleNode)
        return bundleNode
    }

    fun buildEmptyBundleTree(bundleName: String, packageName: String):FileWrapperTreeNode{
        val bundleNode = FileWrapperTreeNodeFactory.createVirtualBundleWrapper(bundleName)
        val src = FileWrapperTreeNodeFactory.createVirtualFolderWrapper("src")
        val main = FileWrapperTreeNodeFactory.createVirtualFolderWrapper("main")
        val java = FileWrapperTreeNodeFactory.createVirtualFolderWrapper("java")
        val packageRoot = FileWrapperTreeNodeFactory.createVirtualPackageRootWrapper(packageName)

        val resources = FileWrapperTreeNodeFactory.createVirtualResourceRootWrapper("resources")

        buildRelationInOrder(listOf(bundleNode,src,main,java,packageRoot))
        buildRelation(main,resources)
        return bundleNode
    }

    fun addSubPackages(packageNode: FileWrapperTreeNode, packageNameToAdd:List<String>){
        val prefix = packageNode.getName()
        packageNameToAdd.forEach {
            addSubPackage(packageNode,prefix,it)
        }
    }

    fun getSubPackageByName(packageNode: FileWrapperTreeNode,packageName: String):FileWrapperTreeNode?{
        if(packageNode.getName()==packageName){
            return packageNode
        }
        packageNode.children.forEach {
            val result = getSubPackageByName(it,packageName)
            if(result!=null){
                return result
            }
        }
        return null
    }

    /**
     * 添加子包，包名为 packageNameToAdd，完整包名为：packageNamePrefix + "." + packageNameToAdd
     * @param
     * @return
     */
    private fun addSubPackage(packageNode: FileWrapperTreeNode, packageNamePrefix:String, packageNameToAdd:String): FileWrapperTreeNode {
        require(isPackage(packageNode))

        // 存在同名子包：有子节点名称 == packageNameToAdd
        val subPackageWithSameName = packageNode.children.firstOrNull { child -> isPackage(child) && child.getName() == packageNameToAdd }
        subPackageWithSameName?.let {
            return subPackageWithSameName
        }

        // 存在同名子包：有子节点名称以 "packageNameToAdd." 开头
        val subPackageStartWithSameName = packageNode.children.firstOrNull { child -> isPackage(child) && child.getName().startsWith("$packageNameToAdd.") }
        subPackageStartWithSameName?.let{
            // 拆分 subPackageStartWithSameName 为 "packageNameToAdd" + 剩下部分
            splitPackageNode(subPackageStartWithSameName,packageNameToAdd)
            return subPackageStartWithSameName
        }

        // 不存在同名子包：该节点以 "packageNamePrefix." 开头，需要拆分节点
        if(packageNode.getName().startsWith("${packageNamePrefix}.")){
            splitPackageNode(packageNode,packageNamePrefix)
        }

        // 在该节点添加子节点 packageNameToAdd
        return addSubPackage(packageNode,packageNameToAdd)
    }

    /**
     * 添加子包，包名为 packageNameToAdd，完整包名为：packageNode.getName()+"."+packageNameToAdd
     * @param
     * @return
     */
    private fun addSubPackage(packageNode: FileWrapperTreeNode,packageNameToAdd:String): FileWrapperTreeNode{
        if(packageNode.children.isNotEmpty()){
            val newPackage = FileWrapperTreeNodeFactory.createVirtualPackageWrapper(packageNameToAdd)
            buildRelation(packageNode,newPackage)
            return newPackage
        }else{
            val oldPackageName = packageNode.getName()
            packageNode.srcFile = File("$oldPackageName.$packageNameToAdd")
            return packageNode
        }
    }

    fun addPackageWithExpectPackageName(packageNode:FileWrapperTreeNode, expectPackageName:String):FileWrapperTreeNode{
        require(isPackage(packageNode))

        // 找到 packageNode(A.B.C)和 expectPackageName(A.S.D) 开头的重合部分：A
        val curPackageSubNames = packageNode.getName().split(".")
        val expectPackageSubNames = expectPackageName.split(".")
        val commonSubNames = mutableListOf<String>()
        curPackageSubNames.forEachIndexed { i, curSubName ->
            if(i >= expectPackageSubNames.size || expectPackageSubNames[i] !=curSubName){
                return@forEachIndexed
            }
            commonSubNames.add(curSubName)
        }

        // 把 packageNode 拆成 A 和剩下部分 B.C
        val commonPrefix = commonSubNames.joinToString(".")
        splitPackageNode(packageNode,commonPrefix)

        // 给 A 节点添加子节点 expectPackageName 的剩下部分(S.D)
        val packageNameToAdd = expectPackageName.substringAfter("${commonPrefix}.")
        return addSubPackage(packageNode,packageNameToAdd)
    }

    /**
     * 分裂包名节点，把 packageNode 的包名按照：packageNamePrefix 和 其它，分裂成两个节点，返回 packageNamePrefix 节点
     * 如：
     * @param
     * @return
     */
    private fun splitPackageNode(packageNode:FileWrapperTreeNode,packageNamePrefix:String):FileWrapperTreeNode{
        if(packageNamePrefix.isEmpty() ||  packageNode.getName() == packageNamePrefix){
            return packageNode
        }

        // 创建右节点
        val subPackageName = packageNode.getName().substringAfter(packageNamePrefix).removePrefix(".")
        val rightPackageNode  = FileWrapperTreeNodeFactory.createVirtualPackageWrapper(subPackageName)

        // 更新左节点名称
        packageNode.srcFile = File(packageNamePrefix)

        // 更新节点关系
        buildRelation(rightPackageNode,packageNode.children)
        packageNode.children.clear()
        buildRelationInOrder(listOf(packageNode,rightPackageNode))
        return packageNode
    }

    private fun importPackages(packageNode: FileWrapperTreeNode, packageNamePrefix: String, importedPackages:List<File>?, whitelist:Set<String>?=null){
        importedPackages?.forEach {
            importPackage(packageNode, packageNamePrefix,it,whitelist)
        }
    }

    fun createResourceDir(parentNode: FileWrapperTreeNode, name: String):FileWrapperTreeNode{
        val node = FileWrapperTreeNodeFactory.createVirtualResourceDirWrapper(name)
        buildRelation(parentNode,node)
        return node
    }

    private fun importBundle(parentNode: FileWrapperTreeNode, importedBundle: File, packageName: String): FileWrapperTreeNode {
        require(isBundle(parentNode) || isBundleRoot(parentNode))
        if(isParentBundle(importedBundle.absolutePath)){
            // 如果是父 bundle, 那么只会导入父 bundle 下的子 bundle
            val bundleNode = createParentBundle(parentNode,importedBundle.name)
            addVirtualFiles(bundleNode, listValidFiles(importedBundle))

            val childBundles = importedBundle.listFiles()?.filter { FileParseUtil.isBundle(it.absolutePath) || isParentBundle(it.absolutePath)}
            childBundles?.let {
                it.forEach { child->
                    importBundle(bundleNode,child,packageName)
                }
            }
            return bundleNode
        }else{
            return importSimpleBundle(parentNode,importedBundle,packageName)
        }
    }

    fun importBundle(parentNode: FileWrapperTreeNode, importedBundle: File, packageName: String, whitelist:Set<String>?):FileWrapperTreeNode {
        require(allowToAddBundle(parentNode))
        if(isValidWhitelist(importedBundle,whitelist)){
            return importBundleWithWhitelist(parentNode, importedBundle, packageName,whitelist!!)
        }

        return importBundle(parentNode, importedBundle, packageName)
    }

    private fun importBundleWithWhitelist(parentNode: FileWrapperTreeNode, importedBundle: File, packageName: String,whiteList:Set<String>):FileWrapperTreeNode {
        if(isParentBundle(importedBundle.absolutePath)){
            if(whiteList.contains(importedBundle.absolutePath)){
                val bundleNode = createParentBundle(parentNode,importedBundle.name)
                addVirtualFiles(bundleNode, listValidFiles(importedBundle),whiteList)

                val childBundles = importedBundle.listFiles()?.filter { FileParseUtil.isBundle(it.absolutePath) || isParentBundle(it.absolutePath)}
                childBundles?.let {
                    it.forEach { child->
                        importBundle(bundleNode,child,packageName,whiteList)
                    }
                }
                return bundleNode
            }else{
                // 跳过创建该节点
                addVirtualFiles(parentNode, listValidFiles(importedBundle),whiteList)

                val childBundles = importedBundle.listFiles()?.filter { FileParseUtil.isBundle(it.absolutePath) || isParentBundle(it.absolutePath)}
                childBundles?.let {
                    it.forEach { child->
                        importBundle(parentNode,child,packageName,whiteList)
                    }
                }
                return parentNode
            }
        }else{
            // 走到这个分支的情况为:
            // whitelist 不为空，没选该 folder 或者 还选了该 folder 内的其它文件，则都认为是合理的 whitelist
            // 1. 选了该bundle的父辈节点,没选该bundle,选了其它bundle -> 不应该导入该bundle
            // [不会发生] 2. 选了该bundle的文件节点,选了该bundle,没有选文件节点 -> 应该完全导入该bundle
            // 3. 选了该bundle节点与文件节点 -> 应该选择性地导入该bundle
            // 4. 选了该bundle的文件节点 -> 应该选择性地导入该bundle
            // 因此, 只要用户选择该 bundle 内的文件,则认为需要创建该 bundle 节点
            val selectFilesInFolder = whiteList.any { FileParseUtil.fileInFolder(it,importedBundle.absolutePath)}
            if(selectFilesInFolder){
                return importSimpleBundle(parentNode,importedBundle,packageName,whiteList)
            }
            return parentNode
        }
    }

    private fun importSimpleBundle(parentNode: FileWrapperTreeNode, importedBundle: File, packageName: String,whiteList:Set<String>?=null):FileWrapperTreeNode{
        val bundleNode = createEmptyBundle(parentNode,importedBundle.name,packageName)
        importAllInPackageRoot(bundleNode,importedBundle,whiteList)
        importAllInResourceRoot(bundleNode,importedBundle,whiteList)
        addVirtualFiles(bundleNode, listValidFiles(importedBundle),whiteList)
        importOtherDirsInMainRoot(bundleNode,importedBundle,whiteList)
        return bundleNode
    }

    private fun importOtherDirsInMainRoot(bundleNode: FileWrapperTreeNode, importedBundle: File,whitelist: Set<String>?=null) {
        val mainRoot = FileParseUtil.parseMainRoot(importedBundle.absolutePath)
        val mainNode = getMainRootNode(bundleNode)!!
        if(mainRoot.exists()){
            // 不是 java 和 resources 文件夹
            val otherDirs = mainRoot.listFiles()?.filter { it.isDirectory && it.name!="resources" && it.name!="java"}
            otherDirs?.let{
                otherDirs.forEach {
                    importVirtualDir(mainNode,it,whitelist)
                }
            }
        }
    }

    fun getMainRootNode(bundleNode: FileWrapperTreeNode): FileWrapperTreeNode? {
        if(!isSimpleBundle(bundleNode)) return null
        val src = bundleNode.children.firstOrNull{ it.getName()=="src" }
        src?.let {
            return src.children.firstOrNull{it.getName()=="main"}
        }
        return null
    }

    private fun importVirtualDir(parentNode: FileWrapperTreeNode, dir: File):FileWrapperTreeNode{
        val dirNode = FileWrapperTreeNodeFactory.createVirtualFolderWrapper(dir.name,true)
        buildRelation(parentNode,dirNode)

        // 添加文件
        addVirtualFiles(dirNode, listValidFiles(dir))

        // 添加子文件夹
        val subDirs = listDirectory(dir)
        subDirs.forEach {
            importVirtualDir(dirNode,it)
        }
        return dirNode
    }

    fun importVirtualDir(parentNode: FileWrapperTreeNode, dir: File, whitelist: Set<String>?=null):FileWrapperTreeNode{
        if(isValidWhitelist(dir,whitelist)){
            return importVirtualDirWithWhitelist(parentNode,dir,whitelist!!)
        }
        return importVirtualDir(parentNode,dir)
    }

    private fun importVirtualDirWithWhitelist(parentNode: FileWrapperTreeNode, dir: File, whitelist: Set<String>):FileWrapperTreeNode{
        if(whitelist.contains(dir.absolutePath)){
            val dirNode = FileWrapperTreeNodeFactory.createVirtualFolderWrapper(dir.name,true)
            buildRelation(parentNode,dirNode)

            // 添加文件
            val filesInDir = whitelist.filter { FileParseUtil.folderContainsFile(dir, File(it)) && dir.absolutePath!=it }.map { it }.toSet()
            if(filesInDir.isEmpty()){
                addVirtualFiles(dirNode, listValidFiles(dir))
            }else{
                addVirtualFiles(dirNode, listValidFiles(dir),whitelist)
            }

            // 添加子文件夹
            val subDirs = listDirectory(dir)
            subDirs.forEach {
                importVirtualDirWithWhitelist(dirNode,it,whitelist)
            }

            return dirNode
        }else{
            // 跳过创建该节点
            // 添加文件
            val filesInDir = whitelist.filter { FileParseUtil.folderContainsFile(dir, File(it)) && dir.absolutePath!=it }.map { it }.toSet()
            if(filesInDir.isNotEmpty()){
                addVirtualFiles(parentNode, listValidFiles(dir),whitelist)
            }

            // 添加子文件夹
            val subDirs = listDirectory(dir)
            subDirs.forEach {
                importVirtualDirWithWhitelist(parentNode,it,whitelist)
            }

            return parentNode
        }
    }


    private fun importResourceDirs(resourceRootNode: FileWrapperTreeNode, resourcePaths: List<File>, whitelist: Set<String>?=null) :List<FileWrapperTreeNode>{
        return resourcePaths.map{
            importResourceDir(resourceRootNode,it,whitelist)
        }.toList()
    }

    private fun importResourceDir(parentNode: FileWrapperTreeNode, resourceDir: File):FileWrapperTreeNode {
        require(parentNode.isResourceRoot || parentNode.isResourceDir){"要求 ${parentNode.getPath()} 为 bundle 的资源目录"}
        require(resourceDir.isDirectory){"要求 ${resourceDir.path} 为目录"}

        val resourceDirNode = FileWrapperTreeNodeFactory.createVirtualResourceDirWrapper(resourceDir.name)
        buildRelation(parentNode,resourceDirNode)

        importResourceDirs(resourceDirNode,listDirectory(resourceDir))
        addVirtualFiles(resourceDirNode,listValidFiles(resourceDir))
        return resourceDirNode
    }

    fun importResourceDir(parentNode: FileWrapperTreeNode, resourceDir: File,whitelist: Set<String>?):FileWrapperTreeNode {
        require(parentNode.isResourceRoot || parentNode.isResourceDir){"要求 ${parentNode.getPath()} 为 bundle 的资源目录"}
        require(resourceDir.isDirectory){"要求 ${resourceDir.path} 为目录"}

        if(isValidWhitelist(resourceDir,whitelist)){
            return importResourceDirWithWhitelist(parentNode,resourceDir, whitelist!!)
        }

        return importResourceDir(parentNode,resourceDir)
    }

    private fun importResourceDirWithWhitelist(parentNode: FileWrapperTreeNode, resourceDir: File, whitelist: Set<String>):FileWrapperTreeNode {
        require(parentNode.isResourceRoot || parentNode.isResourceDir){"要求 ${parentNode.getPath()} 为 bundle 的资源目录"}
        require(resourceDir.isDirectory){"要求 ${resourceDir.path} 为目录"}

        if(whitelist.contains(resourceDir.absolutePath)){
            val resourceDirNode = FileWrapperTreeNodeFactory.createVirtualResourceDirWrapper(resourceDir.name)
            buildRelation(parentNode,resourceDirNode)

            val filesInResourceDir = whitelist.filter { FileParseUtil.folderContainsFile(resourceDir, File(it)) && resourceDir.absolutePath!=it }.map { it }.toSet()
            if(filesInResourceDir.isEmpty()){
                addVirtualFiles(resourceDirNode,listValidFiles(resourceDir))
            }else{
                addVirtualFiles(resourceDirNode,listValidFiles(resourceDir),whitelist)
            }

            importResourceDirs(resourceDirNode,listDirectory(resourceDir),whitelist)
            return resourceDirNode
        }else{
            // 跳过创建资源节点

            val filesInResourceDir = whitelist.filter { FileParseUtil.folderContainsFile(resourceDir, File(it)) && resourceDir.absolutePath!=it }.map { it }.toSet()
            if(filesInResourceDir.isNotEmpty()) {
                addVirtualFiles(parentNode,listValidFiles(resourceDir),whitelist)
            }

            importResourceDirs(parentNode,listDirectory(resourceDir),whitelist)
            return parentNode
        }
    }

    fun addSubResources(resourceNode: FileWrapperTreeNode, resourceDirToAdd:List<String>){
        resourceDirToAdd.forEach {
            val subNode =  FileWrapperTreeNodeFactory.createResourceDirWrapper(it)
            buildRelation(resourceNode,subNode)
        }
    }

    /**
     * 导入 packages:
     * 1 解析要移动的 Bundle 里的 packageRoot，把 packageRoot 里按照package 一个个 import
     * 2 添加 packageRoot 里的其它文件
     * @param
     * @return
     */
    private fun importAllInPackageRoot(bundleNode:FileWrapperTreeNode,importedBundle: File,whitelist: Set<String>?=null){
        if(!bundleNode.isBundle) return

        val packageRootNode = getPackageRootNode(bundleNode)!!
        val packageRootPath = parsePackageRoot(importedBundle)
        addVirtualFiles(packageRootNode,listValidFiles(packageRootPath),whitelist)
        importPackages(packageRootNode,packageRootNode.getName(), listDirectory(packageRootPath),whitelist)
    }

    /**
     * 导入 resources:
     * 1 解析要移动的 Bundle 里的 resourcesRoot
     * 2 添加 resourceRoot 里的其它文件
     * @param
     * @return
     */
    private fun importAllInResourceRoot(bundleNode:FileWrapperTreeNode,importedBundle: File,whitelist: Set<String>?=null){
        if(!bundleNode.isBundle) return

        val resourceRootNode = getResourceRootNode(bundleNode)!!
        val resourceRootPath = parseResourceRoot(importedBundle.path)
        importResourceRoot(resourceRootNode,resourceRootPath,whitelist)
    }

    fun importResourceRoot(resourceRootNode:FileWrapperTreeNode,resourceRootFile:File,whitelist: Set<String>?=null):FileWrapperTreeNode{
        importResourceDirs(resourceRootNode,listDirectory(resourceRootFile),whitelist)
        addVirtualFiles(resourceRootNode, listValidFiles(resourceRootFile),whitelist)
        return resourceRootNode
    }

    fun createEmptyBundle(parentNode:FileWrapperTreeNode, bundleName:String, packageName: String):FileWrapperTreeNode{
        return buildEmptyBundleTree(parentNode,bundleName, packageName)
    }

    fun createParentBundle(parentNode:FileWrapperTreeNode, bundleName:String):FileWrapperTreeNode{
        val bundleNode = FileWrapperTreeNodeFactory.createVirtualBundleWrapper(bundleName)
        buildRelation(parentNode,bundleNode)
        return bundleNode
    }

    private fun importPackage(packageNode: FileWrapperTreeNode,packageNamePrefix:String, importedPackage:File):FileWrapperTreeNode{
        require(isPackage(packageNode))
        val nextPackageName = addSubPackage(packageNode,packageNamePrefix,importedPackage.name)
        // 注意先后顺序：先添加文件
        addVirtualFiles(nextPackageName,listValidFiles(importedPackage))

        // 再继续添加子package
        importPackages(nextPackageName,nextPackageName.getName(),listDirectory(importedPackage))
        return nextPackageName
    }

    fun importPackage(packageNode: FileWrapperTreeNode,packageNamePrefix:String, importedPackage:File,whitelist: Set<String>?):FileWrapperTreeNode{
        require(isPackage(packageNode))
        if(isValidWhitelist(importedPackage,whitelist)){
            return importPackageWithWhitelist(packageNode,packageNamePrefix,importedPackage,whitelist!!)
        }else{
            return importPackage(packageNode,packageNamePrefix,importedPackage)
        }
    }

    fun isValidWhitelist(folder:File, whitelist: Set<String>?):Boolean{
        // 1. whitelist 为空或 null, 则不是合理的 whitelist
        if(whitelist.isNullOrEmpty()) return false

        // 2. whitelist 不为空，且仅选择了该 folder, 则不是合理的 whitelist
        val selectFolder = whitelist.contains(folder.absolutePath)
        val notSelectInFolder = whitelist.none { FileParseUtil.fileInFolder(it,folder.absolutePath)}
        val selectFolderOnly = selectFolder && notSelectInFolder
        return !selectFolderOnly
        // 3. 其它情况： whitelist 不为空
        // 3.1 没选该 folder -> TODO 以后可以早停
        // 3.2 选了该 folder 且选了该 folder 内的其它文件
        // 3.3 该 folder 内的其它文件,则都认为是合理的 whitelist
    }

    private fun importPackageWithWhitelist(packageNode: FileWrapperTreeNode, packageNamePrefix:String, importedPackage:File, whitelist: Set<String>):FileWrapperTreeNode{
        if(whitelist.contains(importedPackage.absolutePath)){
            val nextPackageName = addSubPackage(packageNode,packageNamePrefix,importedPackage.name)

            // 注意先后顺序：先添加文件
            val filesInPackage = whitelist.filter { FileParseUtil.folderContainsFile(importedPackage, File(it)) && importedPackage.absolutePath!=it }.map { it }.toSet()
            if(filesInPackage.isEmpty()){
                addVirtualFiles(nextPackageName,listValidFiles(importedPackage))
            }else{
                addVirtualFiles(nextPackageName,listValidFiles(importedPackage),whitelist)
            }

            // 再继续添加子package
            importPackages(nextPackageName,nextPackageName.getName(),listDirectory(importedPackage),whitelist)

            return nextPackageName
        }else{
            // 跳过创建该package

            // 注意先后顺序：先添加文件
            val filesInPackage = whitelist.filter { FileParseUtil.folderContainsFile(importedPackage, File(it)) }.map { it }.toSet()
            if(filesInPackage.isNotEmpty()) {
                addVirtualFiles(packageNode, listValidFiles(importedPackage), whitelist)
            }
            // 再继续添加子package
            importPackages(packageNode,packageNamePrefix,listDirectory(importedPackage),whitelist)

            return packageNode
        }

    }

    fun modifyNodeName(node: FileWrapperTreeNode,name:String):Boolean{
        if(name.isEmpty()){
            return false
        }
        node.srcFile = File(name)
        return true
    }


    fun removePackage(packageNode: FileWrapperTreeNode):Boolean{
        require(isPackage(packageNode))

        if(1 == getPackageLayerCount(packageNode)){
            removeNode(packageNode)
            return true
        }else{
            val newPackageName = packageNode.getName().substringBeforeLast(".")
            packageNode.srcFile = File(newPackageName)
            return false
        }
    }

    fun removeWholePackage(packageNode: FileWrapperTreeNode):Boolean{
        require(isPackage(packageNode))

        if(1 == getPackageLayerCount(packageNode)){
            removeTree(packageNode)
            return true
        }else{
            removeAllChildren(packageNode)
            val newPackageName = packageNode.getName().substringBeforeLast(".")
            modifyNodeName(packageNode,newPackageName)
            return false
        }
    }

    fun removeTree(node:FileWrapperTreeNode):FileWrapperTreeNode?{
        node.parent?.children?.remove(node)
        return node.parent
    }

    private fun removeAllChildren(node:FileWrapperTreeNode){
        node.children.clear()
    }

    private fun getPackageLayerCount(packageNode: FileWrapperTreeNode):Int{
        require(isPackage(packageNode))
        return packageNode.getName().count { it == '.' } + 1
    }

    fun addVirtualFiles(packageNode: FileWrapperTreeNode, selectedFiles:List<File>?,whiteList:Set<String>?=null):List<FileWrapperTreeNode>?{
        return selectedFiles?.mapNotNull {
            addVirtualFile(packageNode,it,whiteList)
        }?.toList()
    }

    fun addVirtualFile(packageNode: FileWrapperTreeNode,selectedFile:File,whitelist: Set<String>?=null):FileWrapperTreeNode?{
        if(whitelist.isNullOrEmpty() || whitelist.contains(selectedFile.absolutePath)){
            val fileNode =  FileWrapperTreeNodeFactory.createVirtualFileWrapper(selectedFile.path)
            buildRelation(packageNode,fileNode)
            return fileNode
        }
        return null
    }

    private fun removeNode(fileNode: FileWrapperTreeNode){
        val parent = fileNode.parent!!
        val child = fileNode.children
        parent.children.remove(fileNode)
        parent.children.addAll(child)
    }


    private fun allowToAddBundle(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return isBundle(fileWrapper) || isBundleRoot(fileWrapper)
    }

    fun isPackage(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isPackage
    }

    fun isBundleRoot(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isBundleRoot
    }

    fun isBundle(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isBundle
    }

    fun isParentBundle(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        if(!isBundle(fileWrapper)) return false
        return fileWrapper.children.any { child-> isBundle(child) }
    }

    fun isSimpleBundle(fileWrapper: FileWrapperTreeNode?):Boolean{
        if(!isBundle(fileWrapper)) return false
        return !isParentBundle(fileWrapper)
    }

    fun isVirtualBundle(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isBundle && fileWrapper.isCustomFile
    }

    fun isFolder(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isFolder
    }

    fun isJavaFile(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.srcFile.extension == "java"
    }

    fun isResourceDir(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isResourceDir
    }

    fun isVirtualNormalFolder(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isFolder && fileWrapper.isCustomFile && !fileWrapper.isPackage && !fileWrapper.isBundle
    }

    fun allowToAddVirtualNormalFolder(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isFolder &&!fileWrapper.isPackage
    }

    fun isVirtualFile(fileWrapper: FileWrapperTreeNode?):Boolean{
        fileWrapper?:return false
        return fileWrapper.isFile && fileWrapper.isCustomFile
    }

    fun getPackageRoot(root: FileWrapperTreeNode): FileWrapperTreeNode?{
        if(root.isPackageRoot){
            return root
        }

        root.children.forEach {
            val packageRoot = getPackageRoot(it)
            if(packageRoot!=null){
                return packageRoot
            }
        }
        return null
    }

    fun getBundleRoot(root: FileWrapperTreeNode): FileWrapperTreeNode?{
        if(root.isBundleRoot){
            return root
        }
        root.children.forEach {
            val bundleRoot = getBundleRoot(it)
            if(bundleRoot!=null){
                return bundleRoot
            }
        }
        return null
    }


    fun getResourceRootNode(root: FileWrapperTreeNode): FileWrapperTreeNode?{
        if(root.isResourceRoot){
            return root
        }

        root.children.forEach {
            val resourceRoot = getResourceRootNode(it)
            if(resourceRoot!=null){
                return resourceRoot
            }
        }
        return null
    }

    /**
     * 列出所有的bundle节点：包括父bundle，但不包括模块本身
     * @param
     * @return
     */
    private fun getBundleNodes(root:FileWrapperTreeNode):List<FileWrapperTreeNode>{
        val res = mutableListOf<FileWrapperTreeNode>()
        if(root.isBundle && !root.isModuleRoot){
            res.add(root)
        }

        root.children.forEach {
            res.addAll(getBundleNodes(it))
        }
        return res
    }

    fun getAllJavaNode(node:FileWrapperTreeNode):List<FileWrapperTreeNode>{
        val res = mutableListOf<FileWrapperTreeNode>()
        if(isVirtualFile(node) &&node.getName().endsWith(".java")){
            res.add(node)
        }
        node.children.forEach {
            res.addAll(getAllJavaNode(it))
        }
        return res
    }

    private fun getPackageRootNode(root: FileWrapperTreeNode):FileWrapperTreeNode?{
        if(root.isPackageRoot){
            return root
        }

        root.children.forEach {
            val packageRootNode = getPackageRootNode(it)
            if(packageRootNode!=null){
                return packageRootNode
            }
        }
        return null
    }

    fun getAllPackageRootNode(root: FileWrapperTreeNode):List<FileWrapperTreeNode>{
        val bundleNodes = getBundleNodes(root)
        return bundleNodes.mapNotNull { getPackageRootNode(it) }
    }

    fun containsUserFile(root: FileWrapperTreeNode):Boolean{
        if(root.isCustomFile){
            return true
        }
        root.children.forEach {
            if(containsUserFile(it)) return true
        }
        return false
    }

    fun contains(root: FileWrapperTreeNode, nodeName :String,layer:Int = 0):Boolean{
        if(layer>MAX_TREE_NODE_LAYER){
            return false
        }
        if(root.getName() == nodeName) return true
        root.children.forEach {
            if(contains(it,nodeName,layer+1)) return true
        }
        return false
    }

    /**
     * 获取所有移动的文件的源文件路径到目标文件路径的对应关系
     * @param
     * @return
     */
    fun getAllSrcPathToNewPath(root: FileWrapperTreeNode):MutableMap<String,String>{
        val res = mutableMapOf<String,String>()
        if(isVirtualFile(root) && root.newPath!=null){
            res[root.srcFile.absolutePath] = root.newPath!!
        }
        root.children.forEach {
            res.putAll(getAllSrcPathToNewPath(it))
        }
        return res
    }

    fun getAbsoluteNodePath(node: FileWrapperTreeNode, targetSrcPath:String, currentDirPath:List<String>):List<String>?{
        val currentNodePath = currentDirPath.toMutableList().apply {
            this.add(node.getName())
        }

        if(node.srcFile.absolutePath == targetSrcPath) return currentNodePath

        node.children.forEach {
            val absoluteNodePath = getAbsoluteNodePath(it,targetSrcPath,currentNodePath)
            if(absoluteNodePath!=null) return absoluteNodePath
        }
        return null
    }
}
