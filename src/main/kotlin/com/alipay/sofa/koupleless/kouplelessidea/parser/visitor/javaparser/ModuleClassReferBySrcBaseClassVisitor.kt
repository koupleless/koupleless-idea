package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parsePackageName
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parseSplitFullNames
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ClassInfoUtil
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.CatchClause
import com.github.javaparser.ast.stmt.ForEachStmt
import com.github.javaparser.ast.stmt.ForStmt
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import java.nio.file.Path


/**
* @description: 扫描模块中的类被哪些基座类依赖
 * @author lipeng
 * @date 2023/9/11 17:08
 */
object ModuleClassReferBySrcBaseClassVisitor: JavaParserVisitor<SplitModuleContext>() {

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit,arg: SplitModuleContext?) {
        if(isInvalidJavaFile(compilationUnit)){
            return
        }

        val moduleContext = arg!!.moduleContext
        val classContext = moduleContext.classInfoContext
        val classInfoSet = classContext.getAllClassInfo().toMutableSet()

        val packageName = parsePackageName(compilationUnit)
        val classInSamePackage = classInfoSet.filter { it.packageName ==  packageName}.toSet()
        val classInDifferentPackage = classInfoSet subtract classInSamePackage
        val classNameToInfoInSamePackage = classInSamePackage.associateBy { it.fullName }
        val classNameToInfoInDifferentPackage = classInDifferentPackage.associateBy { it.fullName }


        val importedClassNameInDifPackage = parseFileInDifferentPackage(compilationUnit,classNameToInfoInDifferentPackage)
        val importedClassNameInSamePackage = parseFileInSamePackage(compilationUnit,classNameToInfoInSamePackage)
        val importedClassName = importedClassNameInDifPackage union importedClassNameInSamePackage
        val baseClassInfo = ClassInfoUtil.getClassInfoByPath(absolutePath!!.toString(), listOf(arg.srcBaseContext.classInfoContext))!!

        importedClassName.forEach {
            val referModuleClassInfo = ClassInfoUtil.getClassInfoByName(it,listOf(moduleContext.classInfoContext))!!
            baseClassInfo.addRefer(referModuleClassInfo)
            referModuleClassInfo.addReferBy(baseClassInfo)
        }

    }

    // 1. 在其它 package 的 import 中扫描
    private fun parseFileInDifferentPackage(cu:CompilationUnit,targets:Map<String, ClassInfo>):MutableSet<String>{
        if(targets.isEmpty()){
            return mutableSetOf()
        }

        val classNames = targets.keys
        val importedClassNames = mutableSetOf<String>()

        // 1. cu 中 import 了该类，如：import aaa.bbb.ccc.Dd
        val imports = cu.imports.map { it.nameAsString }
        val importedByFullNames = imports.filter { classNames.contains(it)}
        importedClassNames.addAll(importedByFullNames)

        // 2. cu 中 import 了该类的静态变量/静态方法，如：import static aaa.bbb.ccc.Dd.abcd;
        val staticImports = cu.imports.filter { it.isStatic }.map { it.nameAsString }
        val importedByStaticVariableOrMethod = classNames.filter { className -> staticImports.any { staticImport ->
            staticImport.equals(className)||staticImport.startsWith("$className.")} }
        importedClassNames.addAll(importedByStaticVariableOrMethod)

        // 3. cu 中 import 了该类的相关包，如：import aaa.bbb.**
        val maybeImportedByPackage = targets.filter { imports.contains(it.value.packageName) }
        val importedByPackage = parseDependentClass(cu,maybeImportedByPackage.keys)
        importedClassNames.addAll(importedByPackage)

        return importedClassNames
    }

    // 2. 扫描同一 package，则全部扫描
    private fun parseFileInSamePackage(cu:CompilationUnit,targets:Map<String, ClassInfo>):MutableSet<String>{
        return parseDependentClass(cu,targets.keys)
    }

    private fun parseDependentClass(cu:CompilationUnit,targets:Set<String>):MutableSet<String>{
        if(targets.isEmpty()){
            return mutableSetOf()
        }
        val classNames = targets.toMutableSet()
        val referByClassName = mutableSetOf<String>()

        cu.accept(object : VoidVisitorAdapter<MutableSet<String>>(){

            /**
             * 类声明分析: 解析类和内部类, 若这些类继承或实现了target，记录下来
             */
            override fun visit(n: ClassOrInterfaceDeclaration, arg: MutableSet<String>){
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }

                val referByExtend = mutableSetOf<String>()
                val referByInterface = mutableSetOf<String>()
                val referByAnno = mutableSetOf<String>()

                n.extendedTypes.forEach {
                    referByExtend.addAll(referredTarget(it,arg))
                }
                n.implementedTypes.forEach {
                    referByInterface.addAll(referredTarget(it,arg))
                }
                n.annotations.forEach {
                    referByAnno.addAll(referredTarget(it,arg))
                }

                referByClassName.addAll(referByExtend)
                referByClassName.addAll(referByInterface)
                referByClassName.addAll(referByAnno)
                arg.removeAll(referByExtend)
                arg.removeAll(referByInterface)
                arg.removeAll(referByAnno)

                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }


            /**
             * 字段声明分析：解析字段类，若字段类是 arg，记录下来;
             */
            override fun visit(n: FieldDeclaration, arg: MutableSet<String>){
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }

                val fieldType = n.getVariable(0).type
                val referByFieldDeclared = referredTarget(fieldType,arg)
                referByClassName.addAll(referByFieldDeclared)
                arg.removeAll(referByFieldDeclared)

                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }

            /**
             * 变量声明分析：解析变量名，若变量类是 arg，记录下来;
             */
            override fun visit(n: VariableDeclarationExpr, arg: MutableSet<String>){
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }

                val variableType = n.getVariable(0).type
                val referByVariableDeclared = referredTarget(variableType,arg)
                referByClassName.addAll(referByVariableDeclared)
                arg.removeAll(referByVariableDeclared)

                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }


            /**
             * 方法声明分析：分析方法的返回类型、入参类型以及抛出类型, 若类型是 arg，记录下来;
             */
            override fun visit(n: MethodDeclaration, arg: MutableSet<String>){
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }


                val referByOut = referredTarget(n.type,arg)
                val referByIn = mutableSetOf<String>()
                val referByThrownException = mutableSetOf<String>()

                
                n.parameters.forEach {inParam ->
                    referByIn.addAll(referredTarget(inParam.type,arg))
                }
                n.thrownExceptions.forEach{thrownException ->
                    referByThrownException.addAll(referredTarget(thrownException,arg))
                }
                
                referByClassName.addAll(referByOut)
                referByClassName.addAll(referByIn)
                referByClassName.addAll(referByThrownException)
                arg.removeAll(referByOut)
                arg.removeAll(referByIn)
                arg.removeAll(referByThrownException)


                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }


            // 特定语句分析：类对象创建语句、catch 的条件语句，若类型是 arg，记录下来；
            /**
             * 类对象创建语句：分析类对象创建语句的对象类型, 若类型是 arg，记录下来;
             */
            override fun visit(n: ObjectCreationExpr, arg: MutableSet<String>){
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }

                val objectType = n.type
                val referByObjectCreated = referredTarget(objectType,arg)
                referByClassName.addAll(referByObjectCreated)
                arg.removeAll(referByObjectCreated)

                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }

            /**
             * catch语句：分析 catch 语句的对象类型, 若类型是 arg，记录下来;
             */
            override fun visit(n: CatchClause, arg: MutableSet<String>){
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }

                val catchCauseType = n.parameter.type
                val referByCatchCauseType = referredTarget(catchCauseType,arg)
                referByClassName.addAll(referByCatchCauseType)
                arg.removeAll(referByCatchCauseType)

                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }

            /**
             * foreach语句：分析类对象创建语句的对象类型, 若类型是 arg，记录下来;
             * 如：for(String str: list) -> String
             */
            override fun visit(n: ForEachStmt, arg: MutableSet<String>) {
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }

                val forVariableType  = n.variable.variables[0].type
                val referByForVariableType = referredTarget(forVariableType,arg)
                referByClassName.addAll(referByForVariableType)
                arg.removeAll(referByForVariableType)

                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }

            /**
             * for语句：分析类对象创建语句的对象类型, 若类型是 arg，记录下来;
             * 如：for(int i = 0;i<length;i++) -> int
             */
            override fun visit(n: ForStmt, arg: MutableSet<String>) {
                // 加速解析：如果arg中没有要解析的类，则结束解析
                if(arg.isEmpty()){
                    return
                }

                val initializationExpr = n.initialization[0]
                if(initializationExpr is VariableDeclarationExpr){
                    val forVariableType = initializationExpr.getVariable(0).type
                    val referByForVariableType = referredTarget(forVariableType,arg)
                    referByClassName.addAll(referByForVariableType)
                    arg.removeAll(referByForVariableType)
                }

                if(arg.isNotEmpty()){
                    super.visit(n, arg)
                }
            }

            /**
             * 解析 type 引用了 arg 的哪些类
             * @param
             * @return
             */
            private fun referredTarget(type:Type,arg:MutableSet<String>):Set<String>{
                return arg intersect parseSplitFullNames(type)
            }

            private fun referredTarget(annotationExpr: AnnotationExpr, arg:MutableSet<String>):Set<String>{
                return arg intersect setOf(JavaParserUtil.parseFullName(annotationExpr))
            }


        },classNames)

        return referByClassName
    }

    private fun isInvalidJavaFile(cu:CompilationUnit):Boolean{
        return cu.packageDeclaration.isEmpty || cu.types.isEmpty()
    }


}
