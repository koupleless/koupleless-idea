package com.alipay.sofa.koupleless.kouplelessidea.util

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/28 11:07
 */
object MockKUtil {
    fun spyFile(path:String): File {
        return spyk(File(path)){
            every { absolutePath } returns path
            every { isFile } returns true
            every { isDirectory } returns false
            every { exists() } returns true
        }
    }

    fun spyDir(path: String,children:Array<File> = emptyArray()): File {
        return spyk(File(path)){
            every { absolutePath } returns path
            every { isFile } returns false
            every { isDirectory } returns true
            every { exists() } returns true
            every { listFiles() } returns children
        }
    }
    /**
     * 按照文件路径 spy 文件夹，如：paths=["bundleA","src","main","java","com","alipay","MockA.java"]
     */
    fun spyDir(parentDirName:String,paths:List<String>):File{
        return spyDir(parentDirName,paths, emptyArray())
    }
    private fun spyDir(parentDirName:String,paths:List<String>,children:Array<File>):File{
        if(paths.isEmpty()){
            return children.first()
        }
        val last = paths.last()
        if(last.endsWith(".xml")||last.endsWith(".java")){
            val curParentPaths = paths.subList(0,paths.size-1)
            val curPath = parentDirName + FileUtil.FILE_SEPARATOR + paths.joinToString(FileUtil.FILE_SEPARATOR)
            val file = spyFile(curPath)
            return spyDir(parentDirName,curParentPaths, arrayOf(file))
        }else{
            val curParentPaths = paths.subList(0,paths.size-1)
            val curPath = parentDirName + FileUtil.FILE_SEPARATOR +paths.joinToString(FileUtil.FILE_SEPARATOR)
            val dir = spyDir(curPath,children)
            return spyDir(parentDirName,curParentPaths,arrayOf(dir))
        }
    }

    fun spyBundle(parentDirName:String,bundleName:String,packageName:String):File{
        val packageNameList = packageName.split(".")
        val srcDir = spyDir(parentDirName,mutableListOf("src","main","java") + packageNameList + listOf("MockClass.java"))
        val pomFile = spyFile(StrUtil.join(FileUtil.FILE_SEPARATOR,parentDirName,bundleName,"pom.xml"))
        return spyDir(parentDirName+FileUtil.FILE_SEPARATOR+bundleName, arrayOf(pomFile,srcDir))
    }

    fun mockContentPanel(): ContentPanel {
        return mockk<ContentPanel>{
            every { printMavenLog(any()) } returns Unit
            every { printMavenErrorLog(any()) } returns Unit
            every { printLog(any()) } returns Unit
            every { printErrorLog(any()) } returns Unit
        }

    }
    fun spyClassInfo(fullClassName:String, path:String): ClassInfo {
        return spyk(ClassInfo(spyFile(path))){
            every { fullName } returns fullClassName
            every { packageName } returns fullClassName.substringBeforeLast(".")
            every { className } returns fullClassName.substringAfterLast(".")
        }
    }

    fun spyBeanInfo(id:String?=null, fullClassName:String?=null):BeanInfo{
        return spyk(BeanInfo(id,fullClassName)){
            every { beanName } returns id
            every { this@spyk.fullClassName } returns fullClassName
        }
    }

    fun mockProjectContext():ProjectContext{
        return mockk<ProjectContext>{
            every { projectPath } returns "mockProjPath"
            every { name }  returns "mockProjName"
            every { beanContext } returns BeanContext(this)
            every { classInfoContext } returns ClassInfoContext(this)
            every { analyseConfig } returns AnalyseConfig()
            every { configContext } returns ConfigContext(this)
            every { xmlContext } returns XMLContext(this)
        }
    }

    fun getTestResourcePath(resourcePath:String):String{
        return this.javaClass.classLoader.getResource(resourcePath)!!.path
    }

    fun spyApplicationContext(parent: SplitModuleContext?=null):ApplicationContext{
        val parentContext = parent?:mockk<SplitModuleContext>()

        return spyk(ApplicationContext(parentContext)){
            every { projectPath } returns getTestResourcePath("mockproj/mockbase")
            every { name }  returns "mockbase"
            every { beanContext } returns BeanContext(this)
            every { classInfoContext } returns ClassInfoContext(this)
            every { analyseConfig } returns AnalyseConfig()
            every { configContext } returns ConfigContext(this)
            every { xmlContext } returns XMLContext(this)
            every { parserConfiguration } returns ParserConfiguration()
        }
    }

    fun spyBaseContext(parent: SplitModuleContext?=null):BaseContext{
        val parentContext = parent?:mockk<SplitModuleContext>()

        return spyk(BaseContext(parentContext)){
            every { projectPath } returns getTestResourcePath("mockproj/mockbase")
            every { name }  returns "mockbase"
            every { beanContext } returns BeanContext(this)
            every { classInfoContext } returns ClassInfoContext(this)
            every { analyseConfig } returns AnalyseConfig()
            every { configContext } returns ConfigContext(this)
            every { xmlContext } returns XMLContext(this)
            every { parserConfiguration } returns ParserConfiguration()
        }
    }


    fun readCu(resourcePath:String): CompilationUnit {
        val url = this.javaClass.classLoader.getResource(resourcePath)!!
        val file = File(url.toURI())
        var res:CompilationUnit
        file.inputStream().use {
            res = StaticJavaParser.parse(it)
        }
        return res
    }

    fun readFile(resourcePath: String):File{
        val url = this.javaClass.classLoader.getResource(resourcePath)!!
        return File(url.toURI())
    }
}
