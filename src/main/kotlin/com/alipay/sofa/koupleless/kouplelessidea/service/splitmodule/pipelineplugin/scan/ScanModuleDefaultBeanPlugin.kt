package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 19:34
 */
object ScanModuleDefaultBeanPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val moduleContext = splitModuleContext.moduleContext
        val parserConfiguration = moduleContext.getParserConfig()!!

        // 1. 解析 xml 文件中模块 java 文件对应的 bean
        scanModuleBeanInXml(splitModuleContext)

        // 2. 解析 java 文件中的 bean、bean 的类名到接口名的对应，注意顺序！
        val javaPaths = moduleContext.getJavaFiles()
        ParseJavaService.parseOnly(javaPaths,
            parserConfiguration,
            listOf(DefaultBeanVisitor, BeanExtraInfoVisitor,MethodBeanVisitor, BeanDependedOnVisitor,MybatisConfigVisitor,MybatisMethodConfigVisitor,MybatisMapperInterfaceVisitor),
            moduleContext)
    }

    private fun scanModuleBeanInXml(splitModuleContext: SplitModuleContext){
        val xmlContext = splitModuleContext.appContext.xmlContext
        val moduleContext = splitModuleContext.moduleContext
        val classInfoContext = moduleContext.classInfoContext
        val beanContext = moduleContext.beanContext
        xmlContext.getBeanNodes().forEach {beanXmlNode->
            beanXmlNode.fullClassName?.let {
                if(classInfoContext.containsClassName(it)){
                    beanContext.addBeanInfo(beanXmlNode.beanInfo)
                }
            }
        }
    }

    override fun getName(): String {
        return "扫描模块常规 Bean 插件"
    }
}
