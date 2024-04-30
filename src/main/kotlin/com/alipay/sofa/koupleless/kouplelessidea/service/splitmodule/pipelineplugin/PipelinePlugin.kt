package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.Pipeline


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 17:21
 */
abstract class PipelinePlugin: Pipeline {

    override fun process(splitModuleContext: SplitModuleContext) {
        checkInterrupt()

        if(!checkPreCondition(splitModuleContext)) return

        doProcess(splitModuleContext)
    }

    private fun checkInterrupt(){
        if(Thread.currentThread().isInterrupted) throw InterruptedException()
    }

    abstract fun doProcess(splitModuleContext: SplitModuleContext)

    /**
     * 符合前置条件才执行
     * @param
     * @return
     */
    open fun checkPreCondition(splitModuleContext: SplitModuleContext):Boolean{
        return true
    }
}
