package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml

import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ApplicationContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.XMLContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import org.apache.commons.configuration2.XMLConfiguration


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/5 11:53
 */
object MybatisMapperVisitor:XmlVisitor<ProjectContext>() {
    override fun doParse(absolutePath: String, xmlConfig: XMLConfiguration, arg: ProjectContext?) {
        val interfaceType = xmlConfig.getString("@namespace")
        val beanInfo = createBeanInfo(interfaceType,absolutePath)

        val mapXML = XMLContext.MapperXML(interfaceType, absolutePath, beanInfo)
        arg!!.xmlContext.registerMapperXML(mapXML)
    }

    override fun checkPreCondition(
        absolutePath: String,
        xmlConfig: XMLConfiguration,
        arg: ProjectContext?
    ): Boolean {
        val nameSpace = xmlConfig.getString("@namespace")
        return StrUtil.isNotEmpty(nameSpace)
    }

    /**
     * 创建beanInfo，mapper_xml的beanName为mapper接口的名称（首字母小写）
     */
    private fun createBeanInfo(interfaceType:String,absolutePath:String): BeanInfo {
        val beanName = ParseBeanService.defaultBeanNameOfQualifiedName(interfaceType)
        val beanInfo = BeanInfo(beanName,fullClassName = null)
        beanInfo.interfaceTypes.add(interfaceType)
        beanInfo.defineObjectMode = BeanInfo.DefineObjectMode.ByXMLFile
        beanInfo.registerAttribute(SplitConstants.MAPPER_BEAN,true)
        beanInfo.filePath = absolutePath
        return beanInfo
    }
}
