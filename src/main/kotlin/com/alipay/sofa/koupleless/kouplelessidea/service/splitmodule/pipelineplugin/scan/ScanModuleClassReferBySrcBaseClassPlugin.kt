package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.ModuleClassReferBySrcBaseClassVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: 分析模块类被基座类引用插件：扫描模块被基座/模块中的哪些类引用
 * @author lipeng
 * @date 2023/11/7 17:43
 */
object ScanModuleClassReferBySrcBaseClassPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val pathBlackList = splitModuleContext.moduleContext.getAllAbsolutePaths()
        ParseJavaService.parseFromCache(
            splitModuleContext.srcBaseContext,
            listOf(ModuleClassReferBySrcBaseClassVisitor),
            splitModuleContext,
            pathBlackList
        )
    }

    override fun getName(): String {
        return "分析模块类被基座类引用插件"
    }
}
