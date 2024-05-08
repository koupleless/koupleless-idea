package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.AnalyseBeanUtil


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/8 14:14
 */
object AnalyseSrcBaseBeanDependencyPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val moduleContext = splitModuleContext.moduleContext
        val beanContextInModule = moduleContext.beanContext
        val beanContextInSrcBase = splitModuleContext.srcBaseContext.beanContext

        beanContextInSrcBase.allBeanInfo.forEach {beanInfo->
            beanInfo.beanDependOn.forEach { (_, beanRef) ->
                if(beanRef.definedInXML){
                    AnalyseBeanUtil.analyseBeanRefInXML(beanRef,beanContextInSrcBase,beanContextInModule)
                }else{
                    AnalyseBeanUtil.analyseBeanRefInJava(beanRef,beanContextInSrcBase,beanContextInModule)
                }
            }
        }
    }

    override fun getName(): String {
        return "分析原应用 Bean 依赖插件"
    }
}
