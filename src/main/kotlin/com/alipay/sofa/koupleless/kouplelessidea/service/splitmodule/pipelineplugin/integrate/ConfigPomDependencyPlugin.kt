package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import org.apache.maven.model.Dependency
import java.io.File
import java.util.ArrayList


/**
 * @description: 配置模块的每个bundle的pom的依赖，但不配置依赖的版本
 * @author lipeng
 * @date 2023/11/23 16:01
 */
abstract class ConfigPomDependencyPlugin: PipelinePlugin() {

    protected fun configSubBundleReferDependency(bundle:File, splitModuleContext: SplitModuleContext) {
        val bundlePath = bundle.absolutePath
        val javaFiles = FileParseUtil.parseJavaFiles(bundle)
        val moduleContext = splitModuleContext.moduleContext
        val pomFile = FileParseUtil.parsePomByBundle(bundlePath)

        val dependencyIds = mutableSetOf<String>()
        javaFiles.forEach {javaFile->
            val classInfo = moduleContext.classInfoContext.getClassInfoByPath(javaFile.absolutePath)
            classInfo?.let {
                val referJavaPaths = classInfo.referClass.map { (_,referClassInfo)->referClassInfo.getPath() }
                val referPomPaths = referJavaPaths.map { FileParseUtil.parsePomByFile(it) }.toMutableSet()
                // 需要把自己排除掉
                referPomPaths.remove(pomFile.absolutePath)

                referPomPaths.forEach {
                    val file = File(it)
                    if(file.exists()){
                        val pom = MavenPomUtil.buildPomModel(file)
                        val groupId = pom.groupId?:pom.parent.groupId
                        dependencyIds.add("${groupId}:${pom.artifactId}:${pom.version}")
                    }
                }
            }
        }
        val dependencies = dependencyIds.map {
            val d = Dependency()
            val dGAV = it.split(":")
            d.groupId = dGAV[0]
            d.artifactId = dGAV[1]
            d
        }

        val pom =  MavenPomUtil.buildPomModel(pomFile)
        MavenPomUtil.addAllDependencyIfAbsent(pom.dependencies as ArrayList<Dependency>,dependencies)
        MavenPomUtil.writePomModel(pomFile,pom)
    }
}
