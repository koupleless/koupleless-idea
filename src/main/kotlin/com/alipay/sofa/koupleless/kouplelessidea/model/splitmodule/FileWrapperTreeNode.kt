
package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule

import java.io.File

/**
 *
 * @author lipeng
 * @version : FileWrapperTreeNode, v 0.1 2023-09-05 17:34 lipeng Exp $
 */
open class FileWrapperTreeNode(myFile: File?){
    var srcFile:File = myFile?:File("default")

    var isFolder = false
    var isPackage = false
    var isBundle = false
    var isFile = false
    var isResourceDir = false

    var isPackageRoot =false
    var isResourceRoot = false
    var isBundleRoot=false
    var isModuleRoot=false

    var allowedToAddFile = false
    var isCustomFile = false
    var markedAsCopy = false

    var parent: FileWrapperTreeNode? = null
    val children = mutableListOf<FileWrapperTreeNode>()

    var newPath:String? = null

    override fun toString(): String {
        return srcFile.name
    }

    fun addChild(fileWrapper: FileWrapperTreeNode){
        children.add(fileWrapper)
    }

    fun getName(): String {
        return srcFile.name.trim()
    }

    fun getPath():String{
        return if(null!=newPath){
            newPath!!
        }else{
            srcFile.path
        }
    }
}
