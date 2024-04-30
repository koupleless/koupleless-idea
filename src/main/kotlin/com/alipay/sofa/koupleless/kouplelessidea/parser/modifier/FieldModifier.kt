package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parseFieldName
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil.addOrPutMap
import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil.addOrPutSet
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.AnnotationExpr
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/28 13:35
 */
class FieldModifier: JavaParserVisitor<Void>() {
    val annotationsToAdd:MutableMap<String,MutableMap<String,AnnotationExpr>> = mutableMapOf()
    val annotationsToRemove:MutableMap<String,MutableSet<String>> = mutableMapOf()
    private val fieldsToRemove:MutableSet<String> = mutableSetOf()

    fun addAnnotation(fieldName:String,anno:AnnotationExpr){
        addOrPutMap(annotationsToAdd,fieldName,anno.nameAsString,anno)
    }

    private fun removeAnnotation(fieldName: String, annoName:String){
        addOrPutSet(annotationsToRemove,fieldName,annoName)
    }

    fun removeAnnotation(fieldName: String, annoNames:Set<String>){
        annoNames.forEach {
            removeAnnotation(fieldName,it)
        }
    }

    fun removeField(fieldName:String){
        fieldsToRemove.add(fieldName)
    }

    fun getAllAnnoToAdd():Map<String,Map<String,AnnotationExpr>>{
        return annotationsToAdd
    }

    fun getAllAnnoToRemove():Map<String,Set<String>>{
        return annotationsToRemove
    }

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        if(compilationUnit.types.isEmpty()){
            // TODO:log!!
            return
        }

        val type = compilationUnit.getType(0)

        // 配置字段
        val fieldsToRemove = type.fields.filter { fieldDeclaration -> fieldsToRemove.contains(parseFieldName(fieldDeclaration)) }
        fieldsToRemove.forEach {
            it.remove()
        }

        // 配置注解
        type.fields.forEach {fieldDeclaration ->
            val fieldName = parseFieldName(fieldDeclaration)
            if(annotationsToRemove.containsKey(fieldName)){
                annotationsToRemove[fieldName]!!.forEach { annoName ->
                    fieldDeclaration.annotations.removeIf {
                        anno->anno.nameAsString.equals(annoName)
                    }
                }
            }

            if(annotationsToAdd.containsKey(fieldName)){
                annotationsToAdd[fieldName]!!.values.forEach { anno ->
                    fieldDeclaration.addAnnotation(anno)
                }
            }
        }
    }
}
