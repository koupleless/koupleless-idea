package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.RefactorPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanReferByXMLPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 14:58
 */
class RefactorService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        // 定位 xml 文件
        this.addPlugin(ScanReferByXMLPlugin)

        // 重构
        this.addPlugin(RefactorPlugin)
    }

    override fun getName(): String {
        return "重构服务"
    }
}
