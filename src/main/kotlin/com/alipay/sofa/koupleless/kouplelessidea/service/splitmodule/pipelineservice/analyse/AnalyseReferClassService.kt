package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.check.CheckClassRefPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanModuleClassReferBySrcBaseClassPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanModuleClassReferClassPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 17:14
 */
class AnalyseReferClassService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(ScanModuleClassReferBySrcBaseClassPlugin)
            .addPlugin(ScanModuleClassReferClassPlugin)

        this.addPlugin(CheckClassRefPlugin(getContentPanel()))
    }

    override fun getName(): String {
        return "分析类引用服务"
    }
}
