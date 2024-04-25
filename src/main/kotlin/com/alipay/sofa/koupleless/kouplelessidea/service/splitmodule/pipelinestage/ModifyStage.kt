package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.integrate.ModifyFileService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.integrate.RefactorService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 14:26
 */
class ModifyStage(proj: Project): PipelineStage(proj) {
    override fun initStage(splitModuleContext: SplitModuleContext) {
        // 修改文件
        this.addService(ModifyFileService(proj))

        // 重构文件（处于整合阶段的最后一步）
        this.addService(RefactorService(proj))
    }

    override fun getName(): String {
        return "修改阶段"
    }
}
