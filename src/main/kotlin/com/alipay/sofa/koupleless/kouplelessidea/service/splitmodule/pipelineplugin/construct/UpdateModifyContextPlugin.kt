package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 13:57
 */
object UpdateModifyContextPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val moduleContext = splitModuleContext.moduleContext
        val srcPathToTgtPath = moduleContext.getSrcPathToTgtPath()

        val modifyContext = splitModuleContext.modifyStageContext.modifyContext
        srcPathToTgtPath.forEach{(srcPath, tgtPath)->
            modifyContext.updateNewPath(srcPath, tgtPath)
        }
    }

    override fun getName(): String {
        return "更新修改上下文插件"
    }
}
