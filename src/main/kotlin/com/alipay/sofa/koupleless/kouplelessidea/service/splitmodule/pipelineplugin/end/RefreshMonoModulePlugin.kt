package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.end

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 18:13
 */
object RefreshMonoModulePlugin:PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        splitModuleContext.project.baseDir?.findChild("arkmodule")?.refresh(false, true)
    }

    override fun getName(): String {
        return "刷新共库模块插件"
    }
    override fun checkPreCondition(splitModuleContext: SplitModuleContext):Boolean{
        return splitModuleContext.moduleContext.isMono
    }
}
