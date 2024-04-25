package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitSrcBaseAndModuleJavaContextPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 16:50
 */
class InitModuleAndSrcBaseContextService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        // 初始化模块和基座类信息
        this.addPlugin(InitSrcBaseAndModuleJavaContextPlugin)
    }

    override fun getName(): String {
        return "初始化模块和基座信息"
    }
}
