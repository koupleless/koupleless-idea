package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnOrByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/23 18:59
 */
object DependencyTreeUtil {

    fun isSubRoot(node:FileDependencyTreeNode?):Boolean{
        node?:return false
        return node is FileDependencyTreeNode.DefaultSubRootNode
    }

    fun isDependByRoot(node:FileDependencyTreeNode):Boolean{
        return node is FileDependencyTreeNode.DefaultDependByRootNode
    }

    fun isDependOnRoot(node:FileDependencyTreeNode):Boolean{
        return node is FileDependencyTreeNode.DefaultDependOnRootNode
    }

    fun isDependOnNode(node:FileDependencyTreeNode?):Boolean{
        node?:return false
        return node is FileDependOnTreeNode
    }
    fun isDependByNode(node:FileDependencyTreeNode?):Boolean{
        node?:return false
        return node is FileDependByTreeNode
    }

    fun isDependOnOrByNode(node: FileDependencyTreeNode?): Boolean {
        node?:return false
        return node is FileDependOnTreeNode || node is FileDependByTreeNode
    }
}
