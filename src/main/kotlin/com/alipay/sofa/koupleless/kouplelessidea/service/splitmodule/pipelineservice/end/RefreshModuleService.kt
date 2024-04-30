package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.end

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.end.RefreshMonoModulePlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 18:15
 */
class RefreshModuleService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(RefreshMonoModulePlugin)
    }

    override fun getName(): String {
        return "刷新模块服务"
    }
}
