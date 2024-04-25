package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import com.alipay.sofa.koupleless.kouplelessidea.model.BaseData
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.utils.SourceRoot
import com.intellij.openapi.project.Project

/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/19 16:29
 */
class BaseContext (parent: SplitModuleContext): ProjectContext(parent) {

    private var isProjBase = false

    fun update(baseInfo: BaseData?){
        baseInfo?.let { name = it.app }

        // 如果项目路径发生变化，则重新初始化解析配置
        if(parentContext.project.basePath!=projectPath){
            parserConfiguration = null
        }
    }

    override fun initWithProject(proj:Project){
        isProjBase = true
        super.initWithProject(proj)
    }

    // todo: 原子化
    override fun getParserConfig(): ParserConfiguration? {
        return parserConfiguration?: run {
            initParserConfig()
            parserConfiguration
        }
    }

    private fun initParserConfig(){
        parserConfiguration = if (isProjBase) {
            parentContext.appContext.getParserConfig()
        } else {
            ParseJavaService.initParserConfiguration(projectPath)
        }
    }

    override fun getBundleSourceRoots(): MutableMap<String, SourceRoot> {
        if(sourceRoots.isEmpty()){
            initBundleRoots()
        }
        return sourceRoots
    }

    private fun initBundleRoots(){
        if(isProjBase){
            sourceRoots = parentContext.appContext.getBundleSourceRoots()
        }else{
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

    override fun reset() {
        if(isProjBase){
            val projPath = projectPath
            classInfoContext.clear()
            super.reset()
            updateProjectPath(projPath)
        }else{
            super.reset()
        }
    }

}
