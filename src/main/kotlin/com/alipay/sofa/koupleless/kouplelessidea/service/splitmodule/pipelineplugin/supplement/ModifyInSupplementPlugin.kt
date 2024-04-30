package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.supplement

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.github.javaparser.JavaParser


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/29 11:11
 */
object ModifyInSupplementPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        updateModifyContext(splitModuleContext)
        modify(splitModuleContext)
    }

    private fun updateModifyContext(splitModuleContext: SplitModuleContext) {
        val srcPathToTgtPath = splitModuleContext.moduleContext.getSrcPathToTgtPath()
        val modifyContext = splitModuleContext.supplementStageContext.modifyContext
        srcPathToTgtPath.forEach{(srcPath, tgtPath)->
            modifyContext.updateNewPath(srcPath, tgtPath)
        }
    }

    override fun getName(): String {
        return "执行补充阶段修改的插件"
    }

    private fun modify(splitModuleContext: SplitModuleContext){
        val modifyContext = splitModuleContext.supplementStageContext.modifyContext
        modifyContext.modifyAndSave(JavaParser())
    }
}
