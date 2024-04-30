package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/13 11:04
 */
object ClassInfoVisitor: JavaParserVisitor<ProjectContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: ProjectContext?) {
        val file = absolutePath!!.toFile()
        val classInfo = ClassInfo(file).apply {
            packageName = JavaParserUtil.parsePackageName(compilationUnit)
            fullName = JavaParserUtil.parseFullName(compilationUnit)
            className = JavaParserUtil.parseClassName(compilationUnit)
            isAnnotation = JavaParserUtil.isAnnotation(compilationUnit)
            isInterface = JavaParserUtil.isInterface(compilationUnit)
            annotations.addAll(JavaParserUtil.parseAnnotation(compilationUnit))
            implements.addAll(JavaParserUtil.parseImplements(compilationUnit))
            extendClass.addAll(JavaParserUtil.parseExtendClass(compilationUnit))
        }
//        arg!![absolutePath.toString()] = classInfo
        arg!!.classInfoContext.addClassInfo(classInfo)
    }

    override fun checkPreCondition(absolutePath: Path?,compilationUnit: CompilationUnit,arg:ProjectContext?):Boolean{
        return JavaParserUtil.isValidJavaFile(compilationUnit)
    }
}
