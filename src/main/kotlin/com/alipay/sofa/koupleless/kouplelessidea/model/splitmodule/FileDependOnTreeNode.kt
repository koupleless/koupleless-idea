package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule

import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/23 16:20
 */
class FileDependOnTreeNode(file: File):FileDependOnOrByTreeNode(file) {

    override fun copy(): FileDependencyTreeNode {
        val cloned = FileDependOnTreeNode(this.file)
        cloned.let {
            it.isBean = this.isBean
            it.isClass = this.isClass
            it.parent = this.parent
            it.children.addAll(this.children)
            it.dependByAppClassNum = this.dependByAppClassNum
            it.dependByAppBeanNum = this.dependByAppBeanNum
            it.dependByClassNum = this.dependByClassNum
            it.dependByBeanNum = this.dependByBeanNum
            it.inModule = this.inModule
            it.copied = true
            it.dependByClassPaths.addAll(this.dependByClassPaths)
            it.dependByBeanPaths.addAll(this.dependByBeanPaths)
        }
        return cloned
    }
}
