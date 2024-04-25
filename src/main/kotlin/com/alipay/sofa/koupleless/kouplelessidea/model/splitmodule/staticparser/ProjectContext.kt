package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.utils.SourceRoot
import com.intellij.openapi.project.Project
import org.apache.maven.model.Model


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/8/9 14:40
 */
open class ProjectContext(parent: SplitModuleContext) {
    lateinit var projectPath:String

    var name = ""

    var beanContext: BeanContext = BeanContext(this)

    val classInfoContext = ClassInfoContext(this)

    val configContext = ConfigContext(this)

    val xmlContext = XMLContext(this)

    val parentContext = parent

    var parserConfiguration: ParserConfiguration?=null

    protected var sourceRoots: MutableMap<String, SourceRoot> = mutableMapOf()

    private val bundles = mutableMapOf<String, Model>()

    private var encoding: String? = null

    private var SOFAFramework: SplitConstants.SOFAFrameworkEnum?=null

    val analyseConfig = AnalyseConfig()

    open fun getResourceRoot():String{
        return ""
    }

    fun getSpringResourceDir():String{
        return StrUtil.join(FileUtil.FILE_SEPARATOR,getResourceRoot(),"spring")
    }

    fun updateProjectPath(path:String){
        projectPath = path
        bundles.clear()
    }

    open fun initWithProject(proj: Project){
        name = proj.basePath!!.substringAfter(IDEConstants.SEPARATOR)
        updateProjectPath(proj.basePath!!)
    }

    // 不清理 parserConfiguration
    open fun reset(){
        name=""
        encoding = null
        clearContext()
    }

    open fun clearContext(){
        beanContext.clear()
        classInfoContext.clear()
        analyseConfig.clear()
        configContext.clear()
        xmlContext.clear()
        sourceRoots.clear()
    }

    open fun getParserConfig():ParserConfiguration? {
        return parserConfiguration
    }

    open fun getBundleSourceRoots():MutableMap<String,SourceRoot>{
        return sourceRoots
    }
}
