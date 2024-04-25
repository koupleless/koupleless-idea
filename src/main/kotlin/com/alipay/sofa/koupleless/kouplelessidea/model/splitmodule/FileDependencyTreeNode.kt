package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule

import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/21 21:23
 */
abstract class FileDependencyTreeNode(val file: File) {
    var isBean = false
    var isClass = true
    var parent: FileDependencyTreeNode? = null
    val children = mutableListOf<FileDependencyTreeNode>()
    protected var copied = false

    /**
     * 浅拷贝
     */
    abstract fun copy(): FileDependencyTreeNode

    fun isCopied(): Boolean{
        return copied
    }

    override fun toString(): String {
        return file.name
    }

    fun getName():String{
        return toString().trim()
    }

    fun addChild(child: FileDependencyTreeNode) {
        children.add(child)
        child.parent = this
    }

    fun addChildren(childrenList:List<FileDependencyTreeNode>){
        childrenList.forEach {
            addChild(it)
        }
    }

    class DefaultRootNode: FileDependencyTreeNode(File("ROOT")) {

        override fun copy(): FileDependencyTreeNode {
            val cloned = DefaultRootNode()
            cloned.let {
                it.isBean = this.isBean
                it.isClass = this.isClass
                it.parent = this.parent
                it.children.addAll(this.children)
                it.copied = true
            }
            return cloned
        }
    }

    class DefaultSubRootNode(file: File): FileDependencyTreeNode(file) {

        override fun copy(): FileDependencyTreeNode {
            val cloned = DefaultSubRootNode(this.file)
            cloned.let {
                it.isBean = this.isBean
                it.isClass = this.isClass
                it.parent = this.parent
                it.children.addAll(this.children)
                it.copied = true
            }
            return cloned
        }
    }

    class DefaultOverflowNode:FileDependencyTreeNode(File("超出最大显示层数")){
        override fun copy(): FileDependencyTreeNode {
            return DefaultOverflowNode()
        }
    }

    class DefaultDependOnRootNode:FileDependencyTreeNode(File("依赖树")) {

        override fun copy(): FileDependencyTreeNode {
            val cloned = DefaultDependOnRootNode()
            cloned.let {
                it.isBean = this.isBean
                it.isClass = this.isClass
                it.parent = this.parent
                it.children.addAll(this.children)
                it.copied = true
            }
            return cloned
        }
    }

    class DefaultDependByRootNode:FileDependencyTreeNode(File("被依赖树")) {

        override fun copy(): FileDependencyTreeNode {
            val cloned = DefaultDependByRootNode()
            cloned.let {
                it.isBean = this.isBean
                it.isClass = this.isClass
                it.parent = this.parent
                it.children.addAll(this.children)
                it.copied = true
            }
            return cloned
        }
    }
}
