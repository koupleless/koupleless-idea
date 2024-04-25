package com.alipay.sofa.koupleless.kouplelessidea.factory

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnTreeNode
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 17:34
 */
object FileDependencyTreeNodeFactory {

    fun createDependOnNode(path:String):FileDependOnTreeNode{
        return FileDependOnTreeNode(File(path))
    }
    fun createDependByNode(path:String):FileDependByTreeNode{
        return FileDependByTreeNode(File(path))
    }
}
