package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.ClassRefVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 15:45
 */
object ScanAppClassDependencyPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val srcBaseContext = splitModuleContext.srcBaseContext
        ParseJavaService.parseFromCache(
            srcBaseContext,
            listOf(ClassRefVisitor),
            splitModuleContext
        )
    }

    override fun getName(): String {
        return "扫描应用类依赖"
    }
}
