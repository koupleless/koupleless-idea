package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.initial.InitAppContextService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.initial.InitModuleAndSrcBaseContextService
import com.intellij.openapi.project.Project

/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/9 19:42
 */
class InitialStage(proj: Project): PipelineStage(proj) {

    override fun initStage(splitModuleContext: SplitModuleContext) {
        // 初始化应用上下文
        this.addService(InitAppContextService(proj))

        // 初始化模块基座上下文
        this.addService(InitModuleAndSrcBaseContextService(proj))
    }

    override fun getName(): String {
        return "初始化阶段"
    }
}
