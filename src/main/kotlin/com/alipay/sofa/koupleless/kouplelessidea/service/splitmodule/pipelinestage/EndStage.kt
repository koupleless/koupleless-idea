package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.end.RefreshModuleService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:10
 */
class EndStage(proj: Project): PipelineStage(proj) {
    override fun initStage(splitModuleContext: SplitModuleContext) {
        this.addService(RefreshModuleService(proj))
    }

    override fun getName(): String {
        return "结束阶段"
    }
}
