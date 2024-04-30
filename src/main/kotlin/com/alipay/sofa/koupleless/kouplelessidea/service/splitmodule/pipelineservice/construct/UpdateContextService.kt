package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.UpdateModifyContextPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.UpdateModuleContextPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 13:54
 */
class UpdateContextService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(UpdateModuleContextPlugin)
            .addPlugin(UpdateModifyContextPlugin)
    }

    override fun getName(): String {
        return "更新上下文服务"
    }
}
