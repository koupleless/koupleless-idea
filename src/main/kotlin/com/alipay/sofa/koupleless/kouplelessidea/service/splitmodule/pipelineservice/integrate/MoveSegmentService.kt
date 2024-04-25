package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.MoveSegmentPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 14:50
 */
class MoveSegmentService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(MoveSegmentPlugin)
    }

    override fun getName(): String {
        return "移动片段服务"
    }
}
