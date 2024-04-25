package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.github.javaparser.JavaParser


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 15:21
 */
object MoveSegmentPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        splitModuleContext.integrationStageContext.integrateContext.modifyAndSave(JavaParser())
    }

    override fun getName(): String {
        return "移动片段插件"
    }
}
