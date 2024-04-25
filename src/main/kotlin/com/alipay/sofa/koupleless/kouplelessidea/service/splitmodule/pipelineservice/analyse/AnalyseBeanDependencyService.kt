package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse.AnalyseModuleBeanDependencyPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse.AnalyseSrcBaseBeanDependencyPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.check.CheckInvokedBeanWithoutTgtBasePlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: Bean 依赖分析：分析模块和基座间 Bean 的调用关系
 * @author lipeng
 * @date 2023/11/8 13:59
 */
class AnalyseBeanDependencyService(proj:Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(AnalyseModuleBeanDependencyPlugin)
            .addPlugin(AnalyseSrcBaseBeanDependencyPlugin)

        this.addPlugin(CheckInvokedBeanWithoutTgtBasePlugin(getContentPanel()))
    }

    override fun getName(): String {
        return "分析 Bean 依赖服务"
    }
}
