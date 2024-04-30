package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil


/**
 * @description: 拆为原基座的模块时，依赖可以都设置为provided。因为拆分前在同一项目中，使用的依赖版本都相同
 * @author lipeng
 * @date 2023/11/23 16:55
 */
object ConfigDependencyProvidedPlugin:PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        setDependencyProvided(splitModuleContext.moduleContext.getModulePath())
    }

    override fun getName(): String {
        return "配置依赖为provided"
    }

    private fun setDependencyProvided(modulePath: String) {
        val bootstrapPomFile = FileParseUtil.parseBootstrapPom(modulePath)
        val bootstrapPom = MavenPomUtil.buildPomModel(bootstrapPomFile)
        bootstrapPom.dependencies.forEach {
            it.scope = "provided"
        }
        MavenPomUtil.writePomModel(bootstrapPomFile,bootstrapPom)
    }

    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return !splitModuleContext.toNewBase()
    }
}
