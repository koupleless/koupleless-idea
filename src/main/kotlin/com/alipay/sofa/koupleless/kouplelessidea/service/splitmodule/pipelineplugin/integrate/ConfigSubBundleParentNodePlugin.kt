package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import org.apache.maven.model.Parent


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 17:19
 */
object ConfigSubBundleParentNodePlugin:PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        configSubBundleParentNode(splitModuleContext)
    }

    override fun getName(): String {
        return "配置子bundle父节点 插件"
    }

    private fun configSubBundleParentNode(splitModuleContext: SplitModuleContext) {
        val moduleContext = splitModuleContext.moduleContext
        val parentPomFile = FileParseUtil.parsePomByBundle(moduleContext.getModulePath())
        val parentPom = MavenPomUtil.buildPomModel(parentPomFile)


        val allPomFiles = FileParseUtil.parseAllSubPoms(moduleContext.getModulePath())
        allPomFiles.forEach {
            val parent = Parent()
            parent.groupId = parentPom.groupId
            parent.artifactId = parentPom.artifactId
            parent.version = parentPom.version
            parent.relativePath = FileParseUtil.parseRelativePath(FileParseUtil.parseBundlePath(it.absolutePath),parentPomFile.absolutePath)
            val pom = MavenPomUtil.buildPomModel(it)
            pom.parent = parent
            MavenPomUtil.writePomModel(it,pom)
        }
    }
}
