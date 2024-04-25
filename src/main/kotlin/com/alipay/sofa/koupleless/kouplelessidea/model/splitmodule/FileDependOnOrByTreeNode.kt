package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule

import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/11 21:46
 */
abstract class FileDependOnOrByTreeNode(file: File):FileDependencyTreeNode(file) {
    var dependByAppClassNum:Int = 0
    var dependByAppBeanNum:Int = 0
    var dependByClassNum:Int = 0
    var dependByBeanNum:Int = 0
    var inModule:Boolean = false
    val dependByClassPaths = mutableSetOf<String>()
    val dependByBeanPaths = mutableSetOf<String>()

    fun isModuleSafe():Boolean{
        return dependByAppBeanNum == 0 && dependByAppClassNum == 0
    }

    fun moreThanOneDependBy():Boolean{
        return dependByAppBeanNum > 1 || dependByAppClassNum > 1
    }
}
