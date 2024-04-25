package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parseFullName
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description: 抽取实现类名与其接口的关系
 * @author lipeng
 * @date 2023/8/8 22:03
 */
object BeanExtraInfoVisitor: JavaParserVisitor<ProjectContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit,arg: ProjectContext?) {
        // 0. 获取 beanInfo
        val beanContext = arg!!.beanContext
        val className = parseFullName(compilationUnit)
        val beanInfo = beanContext.getBeanInfoByClassName(className)!!

        // 1. 配置 filePath
        beanInfo.filePath = absolutePath!!.toString()

        // 2. 配置 interface
        val interfaceSet = JavaParserUtil.parseImplements(compilationUnit)
        if(interfaceSet.isNotEmpty()){
            beanInfo.interfaceTypes.addAll(interfaceSet)
            interfaceSet.forEach {
                beanContext.putInterfaceToBeanInfo(it,beanInfo)
            }
        }
    }

    /**
     * 是合法的 java 文件，而且该projectContext 包含这个 bean
     * @param
     * @return
     */
    override fun checkPreCondition(absolutePath: Path?,compilationUnit: CompilationUnit,arg: ProjectContext?):Boolean{
        val isValidJavaFile = JavaParserUtil.isValidJavaFile(compilationUnit)
        if(!isValidJavaFile){
            return false
        }

        val className = parseFullName(compilationUnit)
        return arg!!.beanContext.containsClassName(className)
    }
}
