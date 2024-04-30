package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.ClassRefVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: 分析模块类引用了哪些模块类和基座类
 * @author lipeng
 * @date 2023/11/11 20:43
 */
object ScanModuleClassReferClassPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val javaFiles = splitModuleContext.moduleContext.getJavaFiles()
        ParseJavaService.parseOnly(javaFiles,splitModuleContext.moduleContext.getParserConfig()!!, listOf(ClassRefVisitor),splitModuleContext)
    }

    override fun getName(): String {
        return "分析模块类引用类插件"
    }
}
