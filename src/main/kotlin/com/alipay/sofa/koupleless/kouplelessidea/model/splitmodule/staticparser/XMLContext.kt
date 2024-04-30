package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/3 20:16
 */
class XMLContext(parent: ProjectContext) {
    // bean节点：id?,class?,autowired,beanInfo,XMLNode
    private val beanNodes = mutableListOf<BeanXMLNode>()
    // mapper xml文件: Mapper接口名,MapperXML
    private val mapperXMLs = mutableMapOf<String,MapperXML>()

    val parentContext = parent

    fun clear(){
        beanNodes.clear()
    }

    fun registerBeanNode(node: BeanXMLNode){
        beanNodes.add(node)
    }

    fun registerMapperXML(mapperXml:MapperXML){
        mapperXMLs[mapperXml.interfaceType] = mapperXml
    }


    fun getMapperXMLs(): Map<String,MapperXML> {
        return mapperXMLs
    }

    fun getBeanNodes():List<BeanXMLNode>{
        return beanNodes
    }

    data class BeanXMLNode(val beanName: String?, val fullClassName: String?, val autowired: String, val beanInfo: BeanInfo, val xmlNode: XMLNode)

    data class MapperXML(val interfaceType: String, val filePath: String,val beanInfo: BeanInfo){
        var classInfo: ClassInfo? = null
    }
}
