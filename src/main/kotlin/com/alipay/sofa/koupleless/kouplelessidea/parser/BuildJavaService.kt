package com.alipay.sofa.koupleless.kouplelessidea.parser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/29 10:58
 */
object BuildJavaService {
    /**
     * 从基座获取 bean
     * @param
     * @return
     */
    fun buildReuseBeanMethod(methodName:String, returnType:String, beanName:String,modularName:String?=null): MethodDeclaration {
        val name = SimpleName(methodName)
        val type = StaticJavaParser.parseClassOrInterfaceType(returnType)
        val method = MethodDeclaration().apply {
            this.modifiers = NodeList(Modifier.publicModifier())

            this.type = type

            this.name = name

            val beanAnno = NormalAnnotationExpr(Name(SplitConstants.BEAN_ANNOTATION),NodeList(MemberValuePair("name", StringLiteralExpr(beanName))))
            this.addAnnotation(beanAnno)

            this.createBody()
        }

        val stat = StaticJavaParser.parseStatement("return (${type}) SpringBeanFinder.getBaseBean(\"${beanName}\");")


        method.body.get().addStatement(0,stat)

        return method
    }

    fun buildAutowiredFromBaseAnno(beanInfo: BeanInfo): AnnotationExpr {
        val name = Name(SplitConstants.AUTOWIRED_FROM_BASE_ANNOTATION)
        beanInfo.beanName?.let {
            val pairs = NodeList(MemberValuePair("name", StringLiteralExpr(beanInfo.beanName)))
            return NormalAnnotationExpr(name, pairs)
        }

        return MarkerAnnotationExpr(name)
    }
}
