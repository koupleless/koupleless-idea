package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.activate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanAppClassDependencyPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 15:41
 */
class AnalyseAppClassDependencyService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(ScanAppClassDependencyPlugin)
    }

    override fun getName(): String {
        return "分析应用类依赖服务"
    }
}
