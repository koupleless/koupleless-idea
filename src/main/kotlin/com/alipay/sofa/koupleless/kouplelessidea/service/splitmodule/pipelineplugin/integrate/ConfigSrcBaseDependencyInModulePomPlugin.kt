package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 16:27
 */
class ConfigSrcBaseDependencyInModulePomPlugin(val contentPanel: ContentPanel): PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        if(splitModuleContext.toNewBase()){
            hintSrcBaseDependencyProvided(splitModuleContext)
        }else{
            setSrcBaseDependencyProvided(splitModuleContext)
        }
    }

    override fun getName(): String {
        return "配置模块pom中的原应用依赖"
    }


    private fun getSrcBaseBundleGAV(splitModuleContext: SplitModuleContext):Set<String> {
        val srcBasePath = splitModuleContext.srcBaseContext.projectPath
        val srcBaseBundleId = FileParseUtil.parseAllPoms(srcBasePath)
            .map { f -> MavenPomUtil.buildPomModel(f.absolutePath) }
            .map{ m ->
                val groupId = m.groupId?:m.parent.groupId
                "${groupId}:${m.artifactId}"
            }.toSet()
     return srcBaseBundleId
    }

    private fun setSrcBaseDependencyProvided(splitModuleContext: SplitModuleContext){
        val modulePath = splitModuleContext.moduleContext.getModulePath()
        val modulePoms = FileParseUtil.parseAllPoms(modulePath).associateWith { MavenPomUtil.buildPomModel(it.absolutePath) }
        val srcBaseBundleGAV = getSrcBaseBundleGAV(splitModuleContext)

        modulePoms.forEach {(_,pom)->
            val srcBundles = pom.dependencies.filter{ d->srcBaseBundleGAV.contains("${d.groupId}:${d.artifactId}")}.toMutableList()
            srcBundles.forEach {d->
                d.scope = "provided"
            }
        }

        modulePoms.forEach {(file,pom)->
            MavenPomUtil.writePomModel(file,pom)
        }
    }

    private fun hintSrcBaseDependencyProvided(splitModuleContext: SplitModuleContext){
        val modulePath = splitModuleContext.moduleContext.getModulePath()
        val modulePoms = FileParseUtil.parseAllPoms(modulePath).associateWith { MavenPomUtil.buildPomModel(it.absolutePath) }
        val srcBaseBundleGAV = getSrcBaseBundleGAV(splitModuleContext)

        val dependenciesToRemove = mutableMapOf<String,List<String>>()
        modulePoms.forEach {(file,pom)->
            val bundleIdToRemove = pom.dependencies.filter { d->srcBaseBundleGAV.contains("${d.groupId}:${d.artifactId}") }.map {d -> "${d.groupId}:${d.artifactId}" }.toMutableList()
            if(bundleIdToRemove.isNotEmpty()){
                dependenciesToRemove[file.absolutePath] = bundleIdToRemove
            }
        }

        // 告知用户将删除以下依赖
        if(dependenciesToRemove.isNotEmpty()){
            contentPanel.printMavenErrorLog("检测到模块依赖原应用的bundle，可自行清理：")
            var tip = ""
            dependenciesToRemove.forEach { (pomPath, dependencies) -> tip+="pom路径：${pomPath} 有以下依赖: ${dependencies.joinToString(",")}\n" }
            contentPanel.printMavenLog(tip)
        }
    }
}
