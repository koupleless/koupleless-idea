package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.isClassDeclaration
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.BEAN_ANNOTATIONS
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description: 抽取 Bean名称 和 Bean类名
 * @author lipeng
 * @date 2023/8/8 21:58
 */
object DefaultBeanVisitor: JavaParserVisitor<ProjectContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit,arg: ProjectContext?) {
        val type = compilationUnit.getType(0)
        val beanAnnotationToDetect = arg!!.analyseConfig.getCustomBeanAnnotations().toMutableSet()+BEAN_ANNOTATIONS
        val beanAnnotations = JavaParserUtil.filterAnnotations(type, beanAnnotationToDetect)
        if(beanAnnotations.isNotEmpty()){
            val beanName = ParseBeanService.getBeanName(type)
            val className = type.fullyQualifiedName.get()
            val beanContext = arg.beanContext
            val beanInfo = createBeanInfo(beanName,className,absolutePath!!.toString(),beanContext)
            beanContext.addBeanInfo(beanInfo)
        }
    }

    private fun createBeanInfo(id:String, className:String, absolutePath: String,beanContext: BeanContext): BeanInfo {
        val beanInfo = BeanInfo(id,className,beanContext)
        beanInfo.filePath = absolutePath
        return beanInfo
    }

    override fun checkPreCondition(
        absolutePath: Path?,
        compilationUnit: CompilationUnit,
        arg: ProjectContext?
    ): Boolean {
        return isClassDeclaration(compilationUnit)
    }
}
