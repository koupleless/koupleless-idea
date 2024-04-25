package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.supplement

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.supplement.ModifyInSupplementPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/29 11:29
 */
class ModifyInSupplementService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(ModifyInSupplementPlugin)
    }

    override fun getName(): String {
        return "在补充阶段修改文件的服务"
    }
}
