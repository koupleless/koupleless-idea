package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.ClearTemplatePlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.ConstructModulePlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.CreateModulePlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:45
 */
class ConstructModuleService(proj: Project): PipelineService(proj) {

    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(CreateModulePlugin(getContentPanel()))
            .addPlugin(ClearTemplatePlugin)
            .addPlugin(ConstructModulePlugin)
    }

    override fun getName(): String {
        return "构建模块服务"
    }
}
