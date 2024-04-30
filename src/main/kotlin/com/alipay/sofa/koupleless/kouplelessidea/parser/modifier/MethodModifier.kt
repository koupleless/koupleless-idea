package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/26 16:01
 */
class MethodModifier: JavaParserVisitor<Void>() {
    private val methodsToAddLater:MutableList<MethodDeclaration> = mutableListOf()
    private val methodsToRemoveLater:MutableSet<MethodDeclaration> = mutableSetOf()

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        methodsToRemoveLater.forEach {
            it.remove()
        }

        methodsToAddLater.forEach {
            addMethodNow(compilationUnit,it)
        }
    }

    fun addMethodLater(methodDeclaration: MethodDeclaration){
        methodsToAddLater.add(methodDeclaration)
    }

    fun removeMethodLater(methodDeclaration: MethodDeclaration){
        methodsToRemoveLater.add(methodDeclaration)
    }

    fun removeMethodNow(methodDeclaration: MethodDeclaration){
        methodDeclaration.remove()
    }

    fun addMethodNow(cu: CompilationUnit,methodDeclaration: MethodDeclaration){
        cu.getType(0).members.add(methodDeclaration)
    }

    fun addMethodsNow(cu: CompilationUnit,methodDeclarations: List<MethodDeclaration>){
        cu.getType(0).members.addAll(methodDeclarations)
    }
}
