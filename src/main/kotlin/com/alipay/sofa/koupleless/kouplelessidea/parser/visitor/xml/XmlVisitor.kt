package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml


import org.apache.commons.configuration2.XMLConfiguration


/**
 * @description: Xml 解析的 Visitor抽象类，用于处理 XMLConfiguration
 * @author lipeng
 * @date 2023/8/14 18:14
 */
abstract class XmlVisitor<A> {
    fun parse(absolutePath:String,xmlConfig: XMLConfiguration,arg:A?){
        try {
            if(!checkPreCondition(absolutePath,xmlConfig,arg)) return

            doParse(absolutePath,xmlConfig,arg)
        }catch (e:Exception){
            throw RuntimeException("ERROR, XmlVisitor for absolutePath: $absolutePath", e)
        }
    }

    abstract fun doParse(absolutePath:String,xmlConfig: XMLConfiguration,arg:A?)

    /**
     * 符合前置条件才执行
     * @param
     * @return
     */
    open fun checkPreCondition(absolutePath:String,xmlConfig: XMLConfiguration,arg:A?):Boolean{
        return true
    }
}
