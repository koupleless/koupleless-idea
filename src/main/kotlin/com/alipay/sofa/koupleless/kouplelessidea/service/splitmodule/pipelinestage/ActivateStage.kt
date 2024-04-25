package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.activate.AnalyseAppBeanDependencyService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.activate.AnalyseAppClassDependencyService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 15:37
 */
class ActivateStage(proj: Project): PipelineStage(proj) {
    override fun initStage(splitModuleContext: SplitModuleContext) {
        // 分析类依赖
        this.addService(AnalyseAppClassDependencyService(proj))

        // 分析Bean依赖
        this.addService(AnalyseAppBeanDependencyService(proj))

    }

    override fun getName(): String {
        return "激活依赖分析阶段"
    }
}
