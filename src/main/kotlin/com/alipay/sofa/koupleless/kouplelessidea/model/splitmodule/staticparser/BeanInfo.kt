package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil.addOrPutSet
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description: TODO -> 后续要有子类：MethodBeanInfo,XMLBeanInfo,SofaReferenceBeanInfo
 * 如果是 MethodBeanInfo，则 fullClassName = null
 * @author lipeng
 * @date 2023/9/8 14:53
 */
class BeanInfo(val beanName:String?,val fullClassName:String?) {
    val interfaceTypes = mutableSetOf<String>()
    var definedByXML = false
    var defineObjectMode = DefineObjectMode.ByJavaFile

    /**
     * 实现类的路径，即：java 文件对应的路径
     */
    var filePath = ""
    var publishedAsSofaService = false
    /**
     *  三种方式：byType, byName, no
     */
    var beanXmlAutowiredMode: BeanRef.AutowiredMode = BeanRef.AutowiredMode.NO

    /**
     * 依赖的Bean，key 为 fieldName 或 paramName
     */
    val beanDependOn = mutableMapOf<String, BeanRef>()
    /**
     * 被 Bean 依赖
     */
    val beanDependBy = mutableSetOf<BeanInfo>()


    /**
     * 缺失的外部 Bean，key 为该引用的BeanRef，value 为缺失的外部 SofaService 对应的 BeanInfo，需要发布为 SofaService
     */
    val missedOutsideBean = mutableMapOf<BeanRef,MutableSet<BeanInfo>>()

    val missedOutsideBeanToReport = mutableMapOf<BeanRef,MutableSet<BeanInfo>>()

    var parentContext: BeanContext? = null

    private val relatedXmlNodes = mutableMapOf<String, XMLNode>()

    private val attributes = mutableMapOf<String,Any>()

    constructor(beanName:String?,className:String?,parentContext: BeanContext):this(beanName,className){
        this.parentContext = parentContext
    }

    fun defineByXML(autowiredMode:String){
        definedByXML = true
        beanXmlAutowiredMode = BeanRef.AutowiredMode.getByMode(autowiredMode)
    }

    fun defineByMethod(methodSignature:String){
        registerAttribute(SplitConstants.METHOD_BEAN,methodSignature)
    }

    fun definedByMethod():Boolean{
        return attributes.containsKey(SplitConstants.METHOD_BEAN)
    }

    fun registerModularName(modularName:String?){
        modularName?:return
        registerAttribute(SplitConstants.MODULAR_MODULE_NAME,modularName)
    }

    fun getModularName():String?{
        return attributes[SplitConstants.MODULAR_MODULE_NAME] as String?
    }

    fun getAttribute(key:String):Any?{
        return attributes[key]
    }

    fun registerAttribute(key:String,value:Any){
        attributes[key] = value
    }

    fun addMissedOutsideBean(key: BeanRef, value: BeanInfo){
        addOrPutSet(missedOutsideBean,key,value)
    }

    fun addMissedOutsideBeanToReport(key: BeanRef, value: BeanInfo){
        addOrPutSet(missedOutsideBeanToReport,key,value)
    }

    fun registerXMLNode(name: String, node: XMLNode) {
        relatedXmlNodes[name] = node
    }

    fun getXMLNode(name:String): XMLNode?{
        return relatedXmlNodes[name]
    }

    fun getClassOrInterfaceType():String{
        return fullClassName?:interfaceTypes.firstOrNull()?:""
    }
    /**
     * 获取定义时的绝对路径，如果是注解方式定义，则为该注解所在的 java 文件
     * 如果是 xml 方式定义，则为 xml 文件
     */
    fun getAbsolutePathWhenDefined():String?{
        if(definedByXML){
            val beanXmlNode = getXMLNode(SplitConstants.BEAN_XML_NODE)
            beanXmlNode?.let { return beanXmlNode.filePath }

            val sofaRefNode = getXMLNode(SplitConstants.SOFA_REFERENCE_NODE)
            sofaRefNode?.let { return sofaRefNode.filePath }

            return null
        }else{
            return filePath
        }
    }

    enum class DefineObjectMode{
        ByXMLFile,
        ByJavaFile
    }
}
