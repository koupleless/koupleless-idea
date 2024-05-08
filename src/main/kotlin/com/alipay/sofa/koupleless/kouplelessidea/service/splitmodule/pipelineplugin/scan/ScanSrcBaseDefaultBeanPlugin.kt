package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 19:39
 */
object ScanSrcBaseDefaultBeanPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val srcBaseContext = splitModuleContext.srcBaseContext
        val pathBlacklist = splitModuleContext.moduleContext.getAllAbsolutePaths()

        // 1. 解析 xml 中的 bean
        scanSrcBaseBeanInXml(splitModuleContext)

        // 2. 解析 java 文件中的 bean，解析 bean 的类名到接口名的对应，和配置
        ParseJavaService.parseFromCache(
            srcBaseContext,
            listOf(DefaultBeanVisitor, BeanExtraInfoVisitor,MethodBeanVisitor, BeanDependedOnVisitor, MybatisConfigVisitor, MybatisMethodConfigVisitor,MybatisMapperInterfaceVisitor),
            srcBaseContext,
            pathBlacklist
        )
    }

    private fun scanSrcBaseBeanInXml(splitModuleContext: SplitModuleContext) {
        val xmlContext = splitModuleContext.appContext.xmlContext
        val srcBaseContext = splitModuleContext.srcBaseContext
        val classInfoContext = srcBaseContext.classInfoContext
        val beanContext = srcBaseContext.beanContext
        xmlContext.getBeanNodes().forEach {beanXmlNode->
            beanXmlNode.fullClassName?.let {
                if(classInfoContext.containsClassName(it)){
                    beanContext.addBeanInfo(beanXmlNode.beanInfo)
                }
            }
        }
    }

    override fun getName(): String {
        return "扫描原应用常规 Bean 插件"
    }
}
