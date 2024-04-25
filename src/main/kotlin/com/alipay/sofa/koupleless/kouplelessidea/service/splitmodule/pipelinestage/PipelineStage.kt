package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.Pipeline
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 16:51
 */
abstract class PipelineStage(protected val proj:Project): Pipeline {
    private val services = mutableListOf<PipelineService>()

    protected fun getContentPanel(): ContentPanel {
        return proj.service<ContentPanel>()
    }

    override fun process(splitModuleContext: SplitModuleContext) {
        if(!checkPreCondition(splitModuleContext)) {
            getContentPanel().printLog("前置条件不满足，取消执行 ${getName()}")
            return
        }

        reset()

        initStage(splitModuleContext)

        for (service in services){
            try {
                getContentPanel().printLog("开始执行 ${service.getName()}")
                service.process(splitModuleContext)
                getContentPanel().printLog("结束执行 ${service.getName()}")
            } catch (e: Exception) {
                getContentPanel().printErrorLog("执行 ${service.getName()} 失败")
                throw e
            }
        }
    }

    private fun reset(){
        services.clear()
    }

    abstract fun initStage(splitModuleContext: SplitModuleContext)

    fun addService(service: PipelineService): PipelineStage {
        services.add(service)
        return this
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
