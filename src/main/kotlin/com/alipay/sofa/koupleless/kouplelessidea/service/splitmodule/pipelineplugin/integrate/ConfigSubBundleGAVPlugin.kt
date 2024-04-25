package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 17:17
 */
object ConfigSubBundleGAVPlugin:PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        configSubBundleGAV(splitModuleContext)
    }

    override fun getName(): String {
        return "配置子bundle 的GAV 插件"
    }

    private fun configSubBundleGAV(splitModuleContext: SplitModuleContext) {
        val moduleContext = splitModuleContext.moduleContext
        val subBundles = FileParseUtil.parseAllSubBundles(moduleContext.getModulePath())
        subBundles.forEach {subBundle->
            val pomFile = FileParseUtil.parsePomByBundle(subBundle.absolutePath)
            val pom = MavenPomUtil.buildPomModel(pomFile)

            // 修改 artifactId 和 groupId，artifactId 默认为：模块artifactId-bundle名
            pom.groupId = moduleContext.groupId
            pom.artifactId = (moduleContext.artifactId + "-" + subBundle.name)
            pom.version = "1.0.0"

            MavenPomUtil.writePomModel(pomFile,pom)
        }
    }
}
