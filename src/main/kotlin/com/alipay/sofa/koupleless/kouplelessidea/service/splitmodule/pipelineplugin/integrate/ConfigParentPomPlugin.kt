package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import org.apache.maven.model.Model
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 16:18
 */
abstract class ConfigParentPomPlugin: PipelinePlugin() {
    protected fun parseAllDependencyIds(projPath:String):Set<String>{
        val allPomPaths = FileParseUtil.parseAllPoms(projPath)
        val dependencyIds = mutableSetOf<String>()
        val allPoms = allPomPaths.map{ MavenPomUtil.buildPomModel(it)}
        allPoms.forEach {m->
            m.dependencies.forEach {d->
                dependencyIds.add("${d.groupId}:${d.artifactId}")
            }
        }
        return dependencyIds.toSet()
    }

     /**
     * 合并模块中原java文件所在bundle、原项目及当前项目中，所有的 properties 和 profiles
     * @param
     * @return
     */
     protected fun integratePomForParentPom(splitModuleContext: SplitModuleContext): Model {
        // 1. 读取所有原有java文件所在bundle
        val classInfoInModule = splitModuleContext.moduleContext.classInfoContext.getAllClassInfo()
        val pomPaths = classInfoInModule.map { FileParseUtil.parsePomByFile(it.srcPath) }.toSet()
        val poms = mutableListOf<Model>()
        pomPaths.forEach {
            val file = File(it)
            if(file.exists()){
                poms.add(MavenPomUtil.buildPomModel(file))
            }
        }

        // 2. 读取原应用根目录
        val srcBasePath = splitModuleContext.srcBaseContext.projectPath
        val srcBasePom = MavenPomUtil.buildPomModel(FileParseUtil.parsePomByBundle(srcBasePath))
        poms.add(srcBasePom)

        // 3. 整合
        val integratedPom = srcBasePom.clone()
        poms.forEach {pom->
            integratedPom.dependencyManagement = MavenPomUtil.mergeDependencyManagement(integratedPom.dependencyManagement,pom.dependencyManagement)
            integratedPom.profiles = MavenPomUtil.mergeProfiles(integratedPom.profiles,pom.profiles)
            integratedPom.properties = MavenPomUtil.mergeProperties(integratedPom.properties,pom.properties)
            integratedPom.dependencies = MavenPomUtil.mergeDependencies(integratedPom.dependencies,pom.dependencies)
        }
        return integratedPom
    }
}
