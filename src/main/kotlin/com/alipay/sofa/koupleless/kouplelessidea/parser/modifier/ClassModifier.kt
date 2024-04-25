package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.AnnotationExpr
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/28 13:48
 */
class ClassModifier: JavaParserVisitor<Void>() {
    val annotationsToAdd:MutableMap<String,AnnotationExpr> = mutableMapOf()
    private val annotationsToRemove:MutableSet<String> = mutableSetOf()

    fun addAnnotation(anno:AnnotationExpr){
        annotationsToAdd[anno.nameAsString] = anno
    }

    fun removeAnnotation(annoName:String){
        annotationsToRemove.add(annoName)
    }

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        if(compilationUnit.types.isEmpty()){
            // TODO:log!!
            return
        }

        val type = compilationUnit.getType(0)
        type.annotations.removeIf { anno->
            annotationsToRemove.contains(anno.nameAsString)
        }

        annotationsToAdd.values.forEach {
            type.addAnnotation(it)
        }
    }
}
