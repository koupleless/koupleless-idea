package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 16:11
 */
object InitAppDefaultBeanPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {

        // 1. 解析 xml 文件中模块 java 文件对应的 bean
        scanBeanInXml(splitModuleContext)

        // 2. 解析 java 文件对应的 bean
        val appContext = splitModuleContext.appContext
        ParseJavaService.parseFromCache(
            appContext,
            listOf(
                DefaultBeanVisitor,
                BeanExtraInfoVisitor,
                MethodBeanVisitor,
                BeanDependedOnVisitor
            ),
            appContext)

    }

    private fun scanBeanInXml(splitModuleContext: SplitModuleContext) {
        scanDefaultBeanInXml(splitModuleContext)
    }

    private fun scanDefaultBeanInXml(splitModuleContext: SplitModuleContext){
        val xmlContext = splitModuleContext.appContext.xmlContext
        val classInfoContext = splitModuleContext.appContext.classInfoContext
        val beanContext = splitModuleContext.appContext.beanContext

        xmlContext.getBeanNodes().forEach {beanXmlNode->
            beanXmlNode.fullClassName?.let {
                if(classInfoContext.containsClassName(it)){
                    beanContext.addBeanInfo(beanXmlNode.beanInfo)
                }
            }
        }
    }

    override fun getName(): String {
        return "扫描应用Bean插件"
    }
}
