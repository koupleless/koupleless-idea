package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.activate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.analyse.AnalyseAppBeanDependencyPlugin
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 15:42
 */
class AnalyseAppBeanDependencyService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        // 分析 Bean 的依赖关系
        this.addPlugin(AnalyseAppBeanDependencyPlugin)
    }

    override fun getName(): String {
        return "分析应用bean依赖插件"
    }
}
