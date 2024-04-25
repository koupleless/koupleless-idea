package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.utils.SourceRoot
import com.intellij.openapi.project.Project
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/4 21:23
 */
class ApplicationContext(parent:SplitModuleContext):ProjectContext(parent) {
    var project:Project?=null


    override fun initWithProject(proj: Project){
        project = proj
        super.initWithProject(proj)
    }

    override fun getParserConfig(): ParserConfiguration? {
        return parserConfiguration?: run {
            initParserConfig()
            parserConfiguration
        }
    }

    override fun getBundleSourceRoots(): MutableMap<String, SourceRoot> {
        if(sourceRoots.isEmpty()){
            initBundleRoots()
        }
        return sourceRoots
    }

    private fun initParserConfig(){
        parserConfiguration = if (project!=null) {
            ParseJavaService.initParserConfiguration(project!!)
        } else {
            ParseJavaService.initParserConfiguration(projectPath)
        }
    }

    private fun initBundleRoots(){
        val jarBundles = MavenPomUtil.parseAllJarBundles(projectPath)

        jarBundles.mapNotNull {bundlePath ->
            ParseJavaService.getSourceRoot(bundlePath)
        }.map {sourceRootFile ->
            SourceRoot(sourceRootFile.toPath(),getParserConfig())
        }.associateByTo(sourceRoots){
            it.root.toAbsolutePath().toString()
        }

    }
}
