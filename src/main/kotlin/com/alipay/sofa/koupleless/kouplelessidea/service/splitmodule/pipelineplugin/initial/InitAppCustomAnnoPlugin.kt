package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfoContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/24 15:34
 */
object InitAppCustomAnnoPlugin: PipelinePlugin() {

    override fun doProcess(splitModuleContext: SplitModuleContext) {
        initAppCustomAnno(splitModuleContext)
    }

    private fun initAppCustomAnno(splitModuleContext: SplitModuleContext){
        val appContext = splitModuleContext.appContext
        val customBeanAnno = getCustomBeanAnno(appContext.classInfoContext)
        appContext.analyseConfig.addCustomBeanAnnotations(customBeanAnno)
    }

    override fun getName(): String {
        return "初始化自定义注解配置"
    }

    private fun getCustomBeanAnno(classInfoContext:ClassInfoContext):Set<String>{
        val customBeanAnno = mutableSetOf<String>()

        classInfoContext.getAllClassInfo().filter { classInfo ->
            classInfo.isAnnotation && classInfo.annotations.any { anno -> SplitConstants.BEAN_ANNOTATIONS.contains(anno) }
        }.mapTo(customBeanAnno) { classInfo ->
            classInfo.className
        }

        return customBeanAnno
    }
}
