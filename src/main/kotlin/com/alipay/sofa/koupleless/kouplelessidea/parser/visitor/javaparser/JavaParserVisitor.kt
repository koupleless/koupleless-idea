package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description: java 解析Visitor
 * @author lipeng
 * @date 2023/8/8 21:56
 */
abstract class JavaParserVisitor<A> {
    fun parse(absolutePath: Path?, compilationUnit: CompilationUnit,arg:A?){
        try {
            if(!checkPreCondition(absolutePath, compilationUnit, arg)) return

            doParse(absolutePath,compilationUnit,arg)

        }catch (e:Exception){
            val visitorName = this.javaClass.name
            throw RuntimeException("ERROR, $visitorName for absolutePath: ${absolutePath!!.toAbsolutePath()}", e)
        }
    }

    abstract fun doParse(absolutePath: Path?,compilationUnit: CompilationUnit,arg:A?)

    /**
     * 符合前置条件才执行
     * @param
     * @return
     */
    open fun checkPreCondition(absolutePath: Path?,compilationUnit: CompilationUnit,arg:A?):Boolean{
        return true
    }
}
