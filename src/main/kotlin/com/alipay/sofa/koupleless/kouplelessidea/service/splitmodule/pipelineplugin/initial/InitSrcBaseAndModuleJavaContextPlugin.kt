package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/9 21:37
 */
object InitSrcBaseAndModuleJavaContextPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        initClassInfo(splitModuleContext)

        initModuleAndSrcBaseCustomConfig(splitModuleContext)
    }

    override fun getName(): String {
        return "初始化原应用和模块的类信息插件"
    }

    private fun initClassInfo(splitModuleContext: SplitModuleContext){
        // 划分模块类和基座类
        val moduleContext = splitModuleContext.moduleContext
        val srcBaseContext = splitModuleContext.srcBaseContext

        val moduleJavaFiles = moduleContext.getJavaFiles().map { it.absolutePath }.toSet()
        val appClassInfoContext = splitModuleContext.appContext.classInfoContext
        appClassInfoContext.getAllClassInfo().forEach { classInfo ->
            if(moduleJavaFiles.contains(classInfo.getPath())) {
                moduleContext.classInfoContext.addClassInfo(classInfo)
            }else{
                srcBaseContext.classInfoContext.addClassInfo(classInfo)
            }
        }
    }

    private fun initModuleAndSrcBaseCustomConfig(splitModuleContext: SplitModuleContext){
        val appContext = splitModuleContext.appContext
        val moduleContext = splitModuleContext.moduleContext
        val srcBaseContext = splitModuleContext.srcBaseContext
        // 复制原应用的配置
        moduleContext.analyseConfig.clone(appContext.analyseConfig)
        srcBaseContext.analyseConfig.clone(appContext.analyseConfig)
    }

}
