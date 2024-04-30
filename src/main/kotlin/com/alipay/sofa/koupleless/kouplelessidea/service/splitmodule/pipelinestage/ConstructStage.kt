package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.construct.CheckoutBranchService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.construct.ConstructModuleService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.construct.UpdateContextService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:04
 */
class ConstructStage(proj: Project): PipelineStage(proj) {
    override fun initStage(splitModuleContext: SplitModuleContext) {
        // 切换分支
        this.addService(CheckoutBranchService(proj))
        // 构造模块
        this.addService(ConstructModuleService(proj))
        // 更新上下文
        this.addService(UpdateContextService(proj))
    }

    override fun getName(): String {
        return "构造阶段"
    }
}
