package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitAppDefaultBeanPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project

/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/9 19:43
 */
class InitAppContextService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        // 初始化类信息
        this.addPlugin(InitAppClassContextPlugin)

        // 初始化自定义注解信息
        this.addPlugin(InitAppCustomAnnoPlugin)

        // 初始化自定义数据源信息
        this.addPlugin(InitAppCustomDataSourcePlugin)

        // 初始化xml信息
        this.addPlugin(InitAppXMLContextPlugin)

        // 初始化bean信息
        this.addPlugin(InitAppDefaultBeanPlugin)
    }

    override fun getName(): String {
        return "初始化应用上下文服务"
    }
}
