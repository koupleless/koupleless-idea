package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import org.apache.commons.configuration2.XMLConfiguration
import org.apache.commons.configuration2.tree.ImmutableNode
import org.apache.commons.configuration2.tree.InMemoryNodeModel


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/4 10:23
 */
object BeanVisitor: XmlVisitor<ProjectContext>() {
    override fun doParse(absolutePath: String, xmlConfig: XMLConfiguration, arg: ProjectContext?) {
        val xmlAutowiredMode = parseAutowiredMode(xmlConfig)
        val beans = xmlConfig.configurationsAt("//bean")

        for(bean in beans){
            val className = bean.getString("@class")
            val parsedId = bean.getString("@id")
            if(className==null&&parsedId==null){
                continue
            }

            val id = bean.getString("@id")?: ParseBeanService.defaultBeanNameOfQualifiedName(className)
            val autowiredMode = bean.getString("@autowire")?:xmlAutowiredMode?:"no"
            val node = (bean.nodeModel as InMemoryNodeModel).rootNode
            val xmlNode = XMLNode(absolutePath,node = node, nodeXPath = "//bean[@class='${className}']")
            val beanInfo = createBeanInfo(id, className, autowiredMode,xmlNode)
            val beanXMLNode = XMLContext.BeanXMLNode(id, className, autowiredMode, beanInfo,xmlNode)
            arg!!.xmlContext.registerBeanNode(beanXMLNode)
        }
    }

    private fun parseAutowiredMode(xmlConfig: XMLConfiguration):String?{
        return xmlConfig.configurationsAt("/")[0].getString("@default-autowire")
    }


    override fun checkPreCondition(absolutePath: String, xmlConfig: XMLConfiguration, arg: ProjectContext?): Boolean {
        return xmlConfig.configurationsAt("//bean").isNotEmpty()
    }

    private fun createBeanInfo(id:String?, className:String?, autowiredMode:String,xmlNode:XMLNode):BeanInfo{
        val beanInfo = BeanInfo(id,className)
        beanInfo.defineByXML(autowiredMode)
        beanInfo.registerXMLNode(SplitConstants.BEAN_XML_NODE, xmlNode)
        val beanDependOn = parseBeanDependedOn(beanInfo,xmlNode.node!!,autowiredMode)
        beanInfo.beanDependOn.putAll(beanDependOn)
        return beanInfo
    }

    /**
     * 仅解析了 property 节点中依赖的 bean，TODO：解析construct 等节点依赖的 bean
     * @param
     * @return
     */
    private fun parseBeanDependedOn(beanInfo: BeanInfo,node:ImmutableNode,autowiredMode: String):MutableMap<String, BeanRef>{
        val propertyRef = parsePropertyRef(node)
        val beanDependedOn = mutableMapOf<String, BeanRef>()
        propertyRef.forEach { (propertyName, refSet) ->
            val autowired = BeanRef.AutowiredMode.getByMode(autowiredMode)
            val beanRef = BeanRef(propertyName,null,beanInfo,beanNameDefinedInXML=refSet,autowired)
            beanDependedOn[propertyName] = beanRef
        }
        return beanDependedOn
    }

    private fun parsePropertyRef(node: ImmutableNode):MutableMap<String,MutableSet<String>>{
        val res = mutableMapOf<String,MutableSet<String>>()
        node.children.forEach {
            if(it.nodeName == "property"){
                val fieldName =  it.attributes["name"] as String
                res[fieldName] = parseRef(it)
            }
        }
        return res
    }

    private fun parseRef(node: ImmutableNode):MutableSet<String>{
        val res = mutableSetOf<String>()
        // 1. 分析 attributes: ref, value-ref, key-ref
        SplitConstants.BEAN_REF_XML_ATTRIBUTES.forEach {
            if(node.attributes.contains(it)){
                res.add(node.attributes[it] as String)
            }
        }

        // 2. 分析 elements: ref, idref(注入该bean的name)
        SplitConstants.BEAN_REF_XML_ELEMENTS.forEach {elementName->
            if(node.nodeName.equals(elementName)){
                val bean = node.attributes[SplitConstants.BEAN_REF_XML_ATTRIBUTE_IN_ELEMENT]
                bean?.let {b->
                    res.add(b as String)
                }
            }
        }

        // 3. 分析 child
        node.children.forEach {
            res.addAll(parseRef(it))
        }
        return res
    }
}
