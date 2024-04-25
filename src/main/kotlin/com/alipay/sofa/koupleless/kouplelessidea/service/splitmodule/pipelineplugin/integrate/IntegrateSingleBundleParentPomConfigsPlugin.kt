package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 16:15
 */
object IntegrateSingleBundleParentPomConfigsPlugin: ConfigParentPomPlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val modulePath = splitModuleContext.moduleContext.getModulePath()
        val modulePom = MavenPomUtil.buildPomModel(FileParseUtil.parsePomByBundle(modulePath))

        // 1. 合并模块中原有java文件所在bundle，及原应用根目录中的所有的 properties, profiles, dependencyManagement
        val integratedPom = integratePomForParentPom(splitModuleContext)

        // 2. 配置 profiles 和 properties
        modulePom.profiles = MavenPomUtil.mergeProfiles(modulePom.profiles,integratedPom.profiles)
        modulePom.properties = MavenPomUtil.mergeProperties(modulePom.properties,integratedPom.properties)

        // 3. 过滤出在模块中真实使用到的 dependency 在 dependencyManagement 中的信息，并添加 dependency 中的版本
        val dependencyIds = parseAllDependencyIds(modulePath)
        val validDependenciesInDependencyManagement = integratedPom.dependencyManagement?.dependencies?.filter { d->dependencyIds.contains("${d.groupId}:${d.artifactId}") }
            ?.associateBy { d->"${d.groupId}:${d.artifactId}" }
        validDependenciesInDependencyManagement?.let { validDependencyMap->
            modulePom.dependencies.forEach {d->
                if(validDependencyMap.containsKey("${d.groupId}:${d.artifactId}")){
                    d.version = validDependencyMap["${d.groupId}:${d.artifactId}"]!!.version
                }
            }
        }

        // 4. 过滤出模块中真实使用到的 dependency 在 dependency 中的信息，并添加 dependency 中的版本
        val validDependenciesInDependencies = integratedPom.dependencies.filter { d->dependencyIds.contains("${d.groupId}:${d.artifactId}") && StrUtil.isNotEmpty(d.version) }
            .associateBy { d->"${d.groupId}:${d.artifactId}" }
        if(validDependenciesInDependencies.isNotEmpty()){
            modulePom.dependencies.forEach {d->
                if(validDependenciesInDependencies.containsKey("${d.groupId}:${d.artifactId}")){
                    d.version = validDependenciesInDependencies["${d.groupId}:${d.artifactId}"]!!.version
                }
            }
        }

        // 5. 保存
        MavenPomUtil.writePomModel(FileParseUtil.parsePomByBundle(modulePath),modulePom)
    }

    override fun getName(): String {
        return "整合单bundle的父pom配置的插件"
    }

    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return !splitModuleContext.moduleContext.isMono
    }
}
