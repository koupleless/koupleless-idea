package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.modifier.JavaFileModifier
import com.alipay.sofa.koupleless.kouplelessidea.parser.modifier.XMLFileModifier
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.XmlUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import org.apache.commons.configuration2.tree.ImmutableNode
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/28 10:51
 */
class ModifyContext {
    private val javaFileToOperator = mutableMapOf<String, MutableList<JavaFileModifier>>()
    private val xmlFileToOperator = mutableMapOf<String, MutableList<XMLFileModifier>>()

    fun addSpringXMLNode(filePath:String, node: ImmutableNode){
        val xmlFileModifier = getDefaultXMLFileModifier(filePath)
        xmlFileModifier.nodesToAdd.add(node)
    }

    fun setXMLResource(filePath:String,source:String){
        val xmlFileModifier = getDefaultXMLFileModifier(filePath)
        xmlFileModifier.resourceToCopy = source
    }

    fun setXMLPathToCopy(filePath:String,path:String){
        val xmlFileModifier = getDefaultXMLFileModifier(filePath)
        xmlFileModifier.absolutePathToCopy = path
    }

    fun setJavaResource(filePath:String,source:String){
        val javaFileModifier = getDefaultJavaFileModifier(filePath)
        javaFileModifier.resourceToCopy = source
    }

    fun setJavaCopyPath(filePath:String,path:String){
        val javaFileModifier = getDefaultJavaFileModifier(filePath)
        javaFileModifier.absolutePathToCopy = path
    }

    fun setXMLNode(xmlPropertyPos: XMLPropertyPos, value:Any){
        val xmlFileModifier = getDefaultXMLFileModifier(xmlPropertyPos.filePath)
        val key = xmlPropertyPos.nodeXPath+"/@"+xmlPropertyPos.propertyName
        xmlFileModifier.propertiesToSet[key] = value
    }

    fun copyXMLFromAbsolutePath(tgtPath:String,srcPath:String){
        val xmlFileModifier = getDefaultXMLFileModifier(tgtPath)
        xmlFileModifier.absolutePathToCopy = srcPath
    }

    fun removeXMLNode(filePath:String, nodeXPath:String){
        val xmlFileModifier = getDefaultXMLFileModifier(filePath)
        xmlFileModifier.nodesToRemove.add(nodeXPath)
    }

    fun getAllJavaFileToOperator():Map<String, List<JavaFileModifier>>{
        return javaFileToOperator
    }

    fun addModifier(filePath:String,modifier: JavaFileModifier){
        if(!javaFileToOperator.containsKey(filePath)){
            initDefaultJavaFileModifier(filePath)
        }

        javaFileToOperator[filePath]!!.add(modifier)
    }

    fun addModifier(filePath:String,modifier: XMLFileModifier){
        if(!xmlFileToOperator.containsKey(filePath)){
            initDefaultXMLFileModifier(filePath)
        }
        xmlFileToOperator[filePath]!!.add(modifier)
    }

    fun getXMLFileModifier(filePath:String):List<XMLFileModifier>{
        return xmlFileToOperator[filePath]?: emptyList()
    }

    fun getJavaFileModifier(filePath:String):List<JavaFileModifier>{
        return javaFileToOperator[filePath]?: emptyList()
    }

    private fun getDefaultXMLFileModifier(filePath: String): XMLFileModifier {
        if(!xmlFileToOperator.containsKey(filePath)){
            initDefaultXMLFileModifier(filePath)
        }
        return xmlFileToOperator[filePath]!!.first()
    }

    private fun initDefaultXMLFileModifier(filePath:String):XMLFileModifier{
        val xmlFileModifier = XMLFileModifier(filePath)
        xmlFileToOperator[filePath] = mutableListOf(xmlFileModifier)
        return xmlFileModifier
    }

    private fun initDefaultJavaFileModifier(filePath:String):JavaFileModifier{
        val javaFileModifier = JavaFileModifier(filePath)
        javaFileToOperator[filePath] = mutableListOf(javaFileModifier)
        return javaFileModifier
    }

    fun addClassAnnotation(filePath:String, anno:AnnotationExpr){
        val fileModifier = getDefaultJavaFileModifier(filePath)
        fileModifier.classModifier.addAnnotation(anno)
    }

    fun setPackageName(filePath:String,packageName:String){
        val fileModifier = getDefaultJavaFileModifier(filePath)
        fileModifier.packageModifier.packageName = packageName
    }

    fun removeImport(filePath: String,importName:String){
        val fileModifier = getDefaultJavaFileModifier(filePath)
        fileModifier.importModifier.removeImport(importName)
    }

    fun replacePartImportName(filePath:String,srcStr:String,tgtStr:String){
        val fileModifier = getDefaultJavaFileModifier(filePath)
        fileModifier.importModifier.replacePartImportName(srcStr, tgtStr)
    }

    fun addFieldAnnotation(filePath:String,fieldName:String,anno:AnnotationExpr){
        val fileModifier = getDefaultJavaFileModifier(filePath)
        fileModifier.fieldModifier.addAnnotation(fieldName,anno)
    }

    fun removeFieldAnnotation(filePath: String,fieldName: String,annoNames:Set<String>){
        val fileModifier = getDefaultJavaFileModifier(filePath)
        fileModifier.fieldModifier.removeAnnotation(fieldName,annoNames)
    }

    fun addImport(filePath: String,importDeclaration: ImportDeclaration){
        val fileModifier = getDefaultJavaFileModifier(filePath)
        fileModifier.importModifier.addImport(importDeclaration)
    }

    private fun getDefaultJavaFileModifier(filePath:String):JavaFileModifier{
        if(!javaFileToOperator.containsKey(filePath)){
            val javaFileModifier = JavaFileModifier(filePath)
            javaFileToOperator[filePath] = mutableListOf(javaFileModifier)
            return javaFileModifier
        }
        return javaFileToOperator[filePath]!!.first()
    }

    fun modifyAndSave(parser: JavaParser){
        javaFileToOperator.forEach { (filePath, modifiers) ->
            if(filePath.isEmpty()) return@forEach
            // 1. 激活
            modifiers.forEach {modifier ->
                modifier.activate()
            }

            // 2. 解析并保存
            val file = File(filePath)
            ParseJavaService.parseAndSave(file, parser, modifiers, null)
        }

        xmlFileToOperator.forEach { (filePath, modifiers) ->
            if(filePath.isEmpty()) return@forEach
            // 1. 激活
            modifiers.forEach {modifier ->
                modifier.activate()
            }

            // 2. 解析并保存
            XmlUtil.parseAndSave(File(filePath), modifiers,null)
        }
    }

    fun getAllXmlPaths():Set<String>{
        return xmlFileToOperator.keys
    }

    fun contains(path:String):Boolean{
        return xmlFileToOperator.containsKey(path) || javaFileToOperator.containsKey(path)
    }

    fun clear(){
        javaFileToOperator.clear()
        xmlFileToOperator.clear()
    }

    fun updateNewPath(srcPath:String,tgtPath:String){
        if(javaFileToOperator.containsKey(srcPath)){
            javaFileToOperator[tgtPath] = javaFileToOperator[srcPath]!!
            javaFileToOperator.remove(srcPath)
        }

        if(xmlFileToOperator.containsKey(srcPath)){
            xmlFileToOperator[tgtPath] = xmlFileToOperator[srcPath]!!
            xmlFileToOperator.remove(srcPath)
        }
    }
}
