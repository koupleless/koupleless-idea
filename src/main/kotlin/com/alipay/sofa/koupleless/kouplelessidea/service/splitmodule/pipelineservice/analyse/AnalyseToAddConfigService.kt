package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse.AnalyseToReuseMybatisConfigPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/5 15:38
 */
class AnalyseToAddConfigService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(AnalyseToReuseMybatisConfigPlugin(getContentPanel()))
    }

    override fun getName(): String {
        return "分析添加配置服务"
    }
    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return splitModuleContext.autoModify
    }
}
