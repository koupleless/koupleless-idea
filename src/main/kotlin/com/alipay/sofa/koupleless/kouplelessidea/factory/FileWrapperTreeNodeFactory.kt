package com.alipay.sofa.koupleless.kouplelessidea.factory

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import java.io.File

/**
 *
 * @author lipeng
 * @version : FileWrapperFactory, v 0.1 2023-09-05 17:34 lipeng Exp $
 */
object FileWrapperTreeNodeFactory{

    fun createFileWrapper(path:String): FileWrapperTreeNode {
        val node = FileWrapperTreeNode(File(path.trim()))
        node.isFile = true
        return node
    }

    fun createVirtualFileWrapper(path:String): FileWrapperTreeNode {
        val node = createFileWrapper(path)
        node.isCustomFile = true
        return node
    }

    fun createVirtualPackageWrapper(name:String): FileWrapperTreeNode {
        val file = FileWrapperTreeNode(File(name.trim()))
        file.isPackage = true
        file.allowedToAddFile = true
        file.isCustomFile = true
        return file
    }

    fun createPackageRootWrapper(name:String): FileWrapperTreeNode {
        val file = FileWrapperTreeNode(File(name.trim()))
        file.isPackage = true
        file.allowedToAddFile = true
        file.isPackageRoot = true
        return file
    }

    fun createVirtualPackageRootWrapper(name:String): FileWrapperTreeNode {
        val file = createPackageRootWrapper(name)
        file.isCustomFile = true
        return file
    }

    fun createFolderWrapper(name:String,allowedToAddFile:Boolean=false): FileWrapperTreeNode {
        val file = FileWrapperTreeNode(File(name.trim()))
        file.isFolder = true
        file.allowedToAddFile=allowedToAddFile
        return file
    }

    fun createVirtualFolderWrapper(name: String,allowedToAddFile:Boolean=false):FileWrapperTreeNode{
        val file = createFolderWrapper(name,allowedToAddFile)
        file.isCustomFile = true
        return file
    }

    fun createVirtualResourceDirWrapper(name: String):FileWrapperTreeNode{
        val node = createResourceDirWrapper(name)
        node.isCustomFile =  true
        return node
    }

    fun createResourceDirWrapper(name:String):FileWrapperTreeNode{
        val node = createFolderWrapper(name,true)
        node.isResourceDir = true
        return node
    }

    fun createResourceRootWrapper(name:String): FileWrapperTreeNode {
        val node  =createResourceDirWrapper(name)
        node.isResourceRoot=true
        return node
    }

    fun createVirtualResourceRootWrapper(name:String): FileWrapperTreeNode {
        val node = createResourceRootWrapper(name)
        node.isCustomFile=true
        return node
    }


    fun createBundleWrapper(name:String): FileWrapperTreeNode {
        val file = FileWrapperTreeNode(File(name.trim()))
        file.isFolder = true
        file.isBundle = true
        return file
    }

    fun createVirtualBundleWrapper(name:String): FileWrapperTreeNode {
        val node = createBundleWrapper(name)
        node.isCustomFile = true
        return node
    }

    fun createBundleRootWrapper(name:String): FileWrapperTreeNode {
        val file = FileWrapperTreeNode(File(name.trim()))
        file.isFolder = true
        file.isBundleRoot = true
        return file
    }

    fun buildRelation(parent: FileWrapperTreeNode, child: FileWrapperTreeNode){
        parent.addChild(child)
        child.parent = parent
    }

    fun buildRelation(parent: FileWrapperTreeNode, child: List<FileWrapperTreeNode>){
        child.forEach {
            buildRelation(parent,it)
        }
    }


    fun buildRelationInOrder(nodes:List<FileWrapperTreeNode>){
        for(i in 0 until nodes.size-1){
            buildRelation(nodes[i],nodes[i+1])
        }
    }

    fun createModuleRootWrapper(moduleName: String): FileWrapperTreeNode {
        val node = createVirtualBundleWrapper(moduleName)
        node.isModuleRoot = true
        return node
    }
}
