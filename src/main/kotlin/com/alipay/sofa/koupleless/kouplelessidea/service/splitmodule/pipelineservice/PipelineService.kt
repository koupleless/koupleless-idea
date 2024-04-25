package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.Pipeline
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 16:55
 */
abstract class PipelineService(protected val proj:Project): Pipeline {
    protected val plugins = mutableListOf<PipelinePlugin>()

    protected fun getContentPanel(): ContentPanel {
        return proj.service<ContentPanel>()
    }

    override fun process(splitModuleContext: SplitModuleContext) {
        if(!checkPreCondition(splitModuleContext)) {
            getContentPanel().printLog("前置条件不满足，取消执行 ${getName()}")
            return
        }

        reset()

        initService(splitModuleContext)

        for (plugin in plugins) {
            try {
//                getContentPanel().printMavenLog("开始执行 ${plugin.getName()}")
                plugin.process(splitModuleContext)
//                getContentPanel().printMavenLog("结束执行 ${plugin.getName()}")
            } catch (e: Exception) {
                getContentPanel().printErrorLog("执行 ${plugin.getName()} 失败")
                throw e
            }

        }
    }
    abstract fun initService(splitModuleContext: SplitModuleContext)

    fun addPlugin(plugin: PipelinePlugin): PipelineService {
        plugins.add(plugin)
        return this
    }

    private fun reset(){
        plugins.clear()
    }

    /**
     * 符合前置条件才执行
     * @param
     * @return
     */
    open fun checkPreCondition(splitModuleContext: SplitModuleContext):Boolean{
        return true
    }
}
