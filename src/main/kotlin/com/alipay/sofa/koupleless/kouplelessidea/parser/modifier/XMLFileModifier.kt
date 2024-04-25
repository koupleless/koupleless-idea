package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import cn.hutool.core.io.FileUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.XmlVisitor
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import org.apache.commons.configuration2.XMLConfiguration
import org.apache.commons.configuration2.tree.ImmutableNode
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/29 09:00
 */
class XMLFileModifier(filePath: String): XmlVisitor<Void>() {
    protected val filePath: String

    val nodesToAdd = mutableSetOf<ImmutableNode>()

    val nodesToRemove = mutableSetOf<String>()

    val propertiesToSet = mutableMapOf<String,Any>()

    var rootElementName:String? = null

    var resourceToCopy:String = SplitConstants.SPLIT_SPRING_TEMPLATE_RESOURCE

    var absolutePathToCopy:String? = null

    init {
        this.filePath = filePath
    }

    fun activate(){
        if(File(filePath).exists()) return

        if (absolutePathToCopy!=null){
            FileUtil.copyFile(absolutePathToCopy, filePath)
            return
        }

        val templateUrl = this.javaClass.classLoader.getResource(resourceToCopy)
        val templateFileInputStream = templateUrl!!.openStream()
        templateFileInputStream.use {
            FileUtil.writeFromStream(templateFileInputStream, filePath)
        }
        return
    }

    override fun doParse(absolutePath: String, xmlConfig: XMLConfiguration, arg: Void?) {
        if(rootElementName!=null){
            xmlConfig.rootElementName = rootElementName
        }

        // 删除节点
        nodesToRemove.forEach {
            xmlConfig.clearTree(it)
        }

        // 新增节点
        xmlConfig.addNodes("",nodesToAdd)

        // 修改节点的属性名
        modifyProperties(xmlConfig)
    }
    private fun modifyProperties(xmlConfig: XMLConfiguration){
        propertiesToSet.forEach { (key, value) ->
            val nodePath = key.substringBeforeLast("/")
            val subPropertyPath = "/" + key.substringAfterLast("/")

            val nodeNum = xmlConfig.configurationsAt(nodePath).size
            for (i in 1..nodeNum) {
                // 每次都修改第一个节点
                val firstKey = "$nodePath[1]$subPropertyPath"
                xmlConfig.setProperty(firstKey,value)
            }
        }
    }
}
