package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.modify

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.github.javaparser.JavaParser


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 18:00
 */
object DoModifyContextPlugin:PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        modify(splitModuleContext)
    }

    override fun getName(): String {
        return "执行修改上下文插件"
    }

    private fun modify(splitModuleContext: SplitModuleContext){
        val modifyContext = splitModuleContext.modifyStageContext.modifyContext
        val parser = JavaParser()
        modifyContext.modifyAndSave(parser)
    }
}
