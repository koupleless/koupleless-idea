package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.record

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.record.RecordBeanXMLToMovePlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/27 22:12
 */
class RecordXMLNodeToMoveService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(RecordBeanXMLToMovePlugin)
    }

    override fun getName(): String {
        return "记录需要移动的xml节点服务"
    }
}
