package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.AnalyseBeanUtil


/**
 * @description: 分析模块 Bean 依赖
 * @author lipeng
 * @date 2023/11/8 14:01
 */
object AnalyseModuleBeanDependencyPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        analyseBeanDependency(splitModuleContext)
    }

    private fun analyseBeanDependency(splitModuleContext: SplitModuleContext){
        val moduleContext = splitModuleContext.moduleContext
        val beanContextInModule = moduleContext.beanContext
        val beanContextInSrcBase = splitModuleContext.srcBaseContext.beanContext

        beanContextInModule.allBeanInfo.forEach { beanInfo ->
            beanInfo.beanDependOn.forEach { (_, beanRef) ->
                if(beanRef.definedInXML){
                    AnalyseBeanUtil.analyseBeanRefInXML(beanRef,beanContextInModule,beanContextInSrcBase)
                }else{
                    AnalyseBeanUtil.analyseBeanRefInJava(beanRef,beanContextInModule,beanContextInSrcBase)
                }
            }
        }
    }

    override fun getName(): String {
        return "分析模块 Bean 依赖插件"
    }
}
