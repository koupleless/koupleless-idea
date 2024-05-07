package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.record

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.record.RecordAutowiredFromBasePlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project

/**
 * @description: TODO
 * @author lipeng
 * @date 2024/5/7 16:20
 */
class RecordModuleInvokeBaseBeanService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(RecordAutowiredFromBasePlugin(getContentPanel()))
    }

    override fun getName(): String {
        return "记录 SOFAService 服务"
    }
}
