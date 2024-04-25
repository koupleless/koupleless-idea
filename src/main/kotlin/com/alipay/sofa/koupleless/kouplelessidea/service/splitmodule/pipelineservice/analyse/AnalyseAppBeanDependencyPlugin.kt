package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.AnalyseBeanUtil


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 16:20
 */
object AnalyseAppBeanDependencyPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val beanContext = splitModuleContext.appContext.beanContext
        beanContext.allBeanInfo.forEach {beanInfo ->
            beanInfo.beanDependOn.forEach { (_, beanRef) ->
                if(beanRef.definedInXML){
                    AnalyseBeanUtil.analyseBeanRefInXML(beanRef,beanContext,null)
                }else{
                    AnalyseBeanUtil.analyseBeanRefInJava(beanRef,beanContext,null)
                }
            }
        }
    }

    override fun getName(): String {
        return "分析应用Bean依赖插件"
    }
}
