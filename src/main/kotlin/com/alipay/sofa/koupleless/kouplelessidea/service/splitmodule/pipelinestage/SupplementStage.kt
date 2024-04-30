package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.supplement.ModifyInSupplementService

import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:08
 */
class SupplementStage(proj: Project): PipelineStage(proj) {
    override fun initStage(splitModuleContext: SplitModuleContext) {
        this.addService(ModifyInSupplementService(proj))
    }

    override fun getName(): String {
        return "补充阶段"
    }
}
