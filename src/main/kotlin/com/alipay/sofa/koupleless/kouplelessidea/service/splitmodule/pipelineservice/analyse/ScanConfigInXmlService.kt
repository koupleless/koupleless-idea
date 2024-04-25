package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanSrcBaseMybatisConfigXmlPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/12 17:16
 */
class ScanConfigInXmlService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(ScanSrcBaseMybatisConfigXmlPlugin)
    }

    override fun getName(): String {
        return "扫描 xml 中的配置"
    }
    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return splitModuleContext.autoModify
    }
}
