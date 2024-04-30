package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfoContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.XMLPropertyPos
import org.apache.commons.configuration2.XMLConfiguration
import org.apache.commons.configuration2.tree.ImmutableNode


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/7 15:24
 */
object ReferByXmlVisitor: XmlVisitor<ProjectContext>() {
    override fun doParse(absolutePath: String, xmlConfig: XMLConfiguration, arg: ProjectContext?) {
        val rootNode = xmlConfig.nodeModel.rootNode
        val classInfoContext = arg!!.classInfoContext
        parseElement(rootNode,"/",true,classInfoContext,absolutePath)
    }

    /**
     * 记录 ClassInfoContext 类被 xml 引用
     * @param
     * @return
     */
    private fun parseElement(node:ImmutableNode, parentNodePath:String, isRoot:Boolean, classInfoContext: ClassInfoContext, absolutePath: String){
        val nodePath = if (isRoot) parentNodePath else "$parentNodePath/${node.nodeName}"

        node.attributes.forEach { (key, value) ->
            if(value is String && classInfoContext.containsClassName(value)){
                val path = "$nodePath[@$key='$value']"
                val classInfo = classInfoContext.getClassInfoByName(value)
                classInfo!!.addReferByXML(XMLPropertyPos(path,key,absolutePath))
            }
        }

        node.children.forEach {
            parseElement(it,nodePath,false,classInfoContext,absolutePath)
        }
    }
}
