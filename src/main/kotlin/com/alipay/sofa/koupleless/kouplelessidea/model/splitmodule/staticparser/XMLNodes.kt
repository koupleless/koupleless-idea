package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import org.apache.commons.configuration2.tree.ImmutableNode


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/7 14:57
 */
data class XMLPropertyPos(val nodeXPath:String, val propertyName:String, val filePath:String)

data class XMLNode(val filePath:String,val node: ImmutableNode?=null,val nodeXPath:String?=null)
