package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.CheckoutBranchPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:16
 */
class CheckoutBranchService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(CheckoutBranchPlugin(getContentPanel()))
    }

    override fun getName(): String {
        return "切换分支"
    }
}
