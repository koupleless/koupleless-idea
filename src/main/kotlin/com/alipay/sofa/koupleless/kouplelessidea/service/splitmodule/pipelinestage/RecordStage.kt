package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.record.RecordXMLNodeToMoveService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:05
 */
class RecordStage(proj: Project): PipelineStage(proj) {
    override fun initStage(splitModuleContext: SplitModuleContext) {
        // 记录要移动的 XML节点
        this.addService(RecordXMLNodeToMoveService(proj))
    }

    override fun getName(): String {
        return "记录阶段"
    }

    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return splitModuleContext.autoModify
    }
}
