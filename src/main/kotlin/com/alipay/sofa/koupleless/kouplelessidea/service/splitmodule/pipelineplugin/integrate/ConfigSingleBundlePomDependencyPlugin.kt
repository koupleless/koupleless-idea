package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 16:04
 */
object ConfigSingleBundlePomDependencyPlugin:ConfigPomDependencyPlugin(){
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val bundlePath = splitModuleContext.moduleContext.getModulePath()
        val bundle = File(bundlePath)
        configSubBundleReferDependency(bundle, splitModuleContext)
    }

    override fun getName(): String {
        return "配置单bundle的pom依赖插件"
    }
}
