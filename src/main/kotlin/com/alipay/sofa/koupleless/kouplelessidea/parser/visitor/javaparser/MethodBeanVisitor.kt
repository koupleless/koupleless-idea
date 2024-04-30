package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/6 13:35
 */
object MethodBeanVisitor: JavaParserVisitor<ProjectContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: ProjectContext?) {
        val type = compilationUnit.getType(0)

        val beanContext = arg!!.beanContext
        type.methods.forEach{method->
            val beanAnno = JavaParserUtil.filterAnnotation(method, SplitConstants.BEAN_ANNOTATION)
            beanAnno?: return@forEach

            val beanType = JavaParserUtil.parseFullName(method.type)
            val beanNames = ParseBeanService.parseBeanName(method)
            val methodSignature = JavaParserUtil.parseMethodSignature(method)
            val beanInfo = createBeanInfo(beanType, beanNames.first(),absolutePath!!, methodSignature)

            // 解析bean依赖
            registerBeanRefs(beanInfo,method)

            // 注册bean
            beanContext.addBeanInfo(beanInfo)

            // 注册所有的 beanName
            beanNames.forEach { beanName ->
                if(!beanContext.containsBeanName(beanName)){
                    beanContext.putBeanNameToBeanInfo(beanName, beanInfo)
                }
            }
        }
    }

    private fun createBeanInfo(beanType: String, beanName: String,absolutePath:Path,methodSignature:String): BeanInfo {
        // 解析beanType：因为无法判断是ClassName 还是 InterfaceType，所以把 beanType 配置为接口类型，className 配置为空
        return BeanInfo(beanName, null).apply{
            defineByMethod(methodSignature)
            interfaceTypes.add(beanType)
            filePath = absolutePath.toString()
        }
    }

    private fun registerBeanRefs(beanInfo: BeanInfo,method: MethodDeclaration){
        method.parameters.forEach {param ->
            val beanRef = ParseBeanService.parseBeanRef(param,beanInfo)
            val paramName = param.nameAsString
            beanInfo.beanDependOn[paramName] = beanRef
        }
    }

    override fun checkPreCondition(
        absolutePath: Path?,
        compilationUnit: CompilationUnit,
        arg: ProjectContext?
    ): Boolean {
        if(!JavaParserUtil.isClassDeclaration(compilationUnit)){
            return false
        }

        val type = compilationUnit.getType(0)
        val beanAnnotationToDetect = arg!!.analyseConfig.getCustomBeanAnnotations().toMutableSet()+ SplitConstants.BEAN_ANNOTATIONS
        val beanAnnotations = JavaParserUtil.filterAnnotations(type, beanAnnotationToDetect)
        return beanAnnotations.isNotEmpty()
    }
}
