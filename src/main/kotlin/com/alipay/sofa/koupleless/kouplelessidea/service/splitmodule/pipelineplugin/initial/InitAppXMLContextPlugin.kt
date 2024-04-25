package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseXmlService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/3 20:42
 */
object InitAppXMLContextPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        parseFromComponentScan(splitModuleContext)

        parseResourceXml(splitModuleContext)
    }

    override fun getName(): String {
        return "初始化 XML 上下文插件"
    }

    private fun parseFromComponentScan(splitModuleContext: SplitModuleContext) {
        val appContext = splitModuleContext.appContext
        val rootPath = splitModuleContext.appContext.projectPath
        ParseXmlService.parseFromComponentScan(rootPath,
            listOf(
                BeanVisitor
            ), appContext)
    }

    private fun parseResourceXml(splitModuleContext: SplitModuleContext){
        val appContext = splitModuleContext.appContext
        val rootPath = splitModuleContext.appContext.projectPath
        // 没有精细化扫描项目中配置的指定位置 mapper.xml。
        // 如果需要精细化扫描，则需要先扫描项目中的 sqlFactoryBean 的 java/xml 定义
        ParseXmlService.parseFromResources(rootPath,
            listOf(
                MybatisMapperVisitor
            ), appContext)
    }
}
