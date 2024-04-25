package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfoContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/19 15:20
 */
object InitAppCustomDataSourcePlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        initAppCustomDataSourceClass(splitModuleContext)
    }

    private fun initAppCustomDataSourceClass(splitModuleContext: SplitModuleContext) {
        val appContext = splitModuleContext.appContext
        val customDataSourceClasses = getCustomDataSource(appContext.classInfoContext)
        appContext.analyseConfig.addCustomDataSourceClasses(customDataSourceClasses)
    }

    private fun getCustomDataSource(classInfoContext: ClassInfoContext): Set<String> {
        val customDataSource = mutableSetOf<String>()
        classInfoContext.getAllClassInfo().filter { classInfo ->
            classInfo.extendClass.intersect(SplitConstants.DATA_SOURCE).isNotEmpty()
        }.mapTo(customDataSource) { classInfo ->
            classInfo.fullName
        }
        return customDataSource
    }

    override fun getName(): String {
        return "初始化自定义数据源配置"
    }
}
