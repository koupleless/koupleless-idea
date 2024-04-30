package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
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
 * @description: TODO
 * @author lipeng
 * @date 2023/11/10 16:31
 */
object ClassRefVisitor: JavaParserVisitor<SplitModuleContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: SplitModuleContext?) {

        val referClass = parseDependentClass(compilationUnit,arg!!)

        val fullName = JavaParserUtil.parseFullName(compilationUnit)
        val searchClassInfoContext = listOf(arg.srcBaseContext.classInfoContext,arg.moduleContext.classInfoContext)
        val classInfo = ClassInfoUtil.getClassInfoByName(fullName,searchClassInfoContext)!!

        classInfo.referClass.putAll(referClass)
        referClass.forEach { (_, v) ->
            v.referByClass[fullName] = classInfo
        }
    }

    private fun parseDependentClass(cu:CompilationUnit,splitModuleContext: SplitModuleContext):MutableMap<String, ClassInfo>{
        val srcBaseClassInfoContext = splitModuleContext.srcBaseContext.classInfoContext
        val moduleClassInfoContext = splitModuleContext.moduleContext.classInfoContext
        val dependentClass = mutableMapOf<String, ClassInfo>()
        cu.accept(object : VoidVisitorAdapter<MutableMap<String, ClassInfo>>(){
            /**
             * 类声明分析: 解析类和内部类, 若这些类继承或实现了target，记录下来
             */
            override fun visit(n: ClassOrInterfaceDeclaration, arg: MutableMap<String, ClassInfo>){

                val referByExtend = mutableMapOf<String, ClassInfo>()
                val referByInterface = mutableMapOf<String, ClassInfo>()
                val referByAnnotations = mutableMapOf<String, ClassInfo>()

                n.extendedTypes.forEach {
                    referByExtend.putAll(referredTarget(it))
                }
                n.implementedTypes.forEach {
                    referByInterface.putAll(referredTarget(it))
                }
                n.annotations.forEach{annotationExpr ->
                    referByAnnotations.putAll(referredTarget(annotationExpr))
                }

                arg.putAll(referByExtend)
                arg.putAll(referByInterface)
                arg.putAll(referByAnnotations)

                super.visit(n, arg)
            }


            /**
             * 字段声明分析：解析字段类，若字段类是 arg，记录下来;
             */
            override fun visit(n: FieldDeclaration, arg: MutableMap<String, ClassInfo>){
                val fieldType = n.getVariable(0).type
                val referByFieldDeclared = referredTarget(fieldType)
                arg.putAll(referByFieldDeclared)

                super.visit(n, arg)
            }

            /**
             * 变量声明分析：解析变量名，若变量类是 arg，记录下来;
             */
            override fun visit(n: VariableDeclarationExpr, arg: MutableMap<String, ClassInfo>){

                val variableType = n.getVariable(0).type
                val referByVariableDeclared = referredTarget(variableType)
                arg.putAll(referByVariableDeclared)

                super.visit(n, arg)
            }


            /**
             * 方法声明分析：分析方法的返回类型、入参类型、抛出类型及注解, 若类型是 arg，记录下来;
             */
            override fun visit(n: MethodDeclaration, arg: MutableMap<String, ClassInfo>){

                val referByOut = referredTarget(n.type)
                val referByIn = mutableMapOf<String, ClassInfo>()
                val referByThrownException = mutableMapOf<String, ClassInfo>()
                val referByAnnotations = mutableMapOf<String, ClassInfo>()

                n.parameters.forEach {inParam ->
                    referByIn.putAll(referredTarget(inParam.type))
                }
                n.thrownExceptions.forEach{thrownException ->
                    referByThrownException.putAll(referredTarget(thrownException))
                }
                n.annotations.forEach{annotationExpr ->
                    referByAnnotations.putAll(referredTarget(annotationExpr))
                }

                arg.putAll(referByOut)
                arg.putAll(referByIn)
                arg.putAll(referByThrownException)
                arg.putAll(referByAnnotations)

                super.visit(n, arg)
            }


            // 特定语句分析：类对象创建语句、catch 的条件语句，若类型是 arg，记录下来；
            /**
             * 类对象创建语句：分析类对象创建语句的对象类型, 若类型是 arg，记录下来;
             */
            override fun visit(n: ObjectCreationExpr, arg: MutableMap<String, ClassInfo>){

                val objectType = n.type
                val referByObjectCreated = referredTarget(objectType)
                arg.putAll(referByObjectCreated)

                super.visit(n, arg)
            }

            /**
             * catch语句：分析 catch 语句的对象类型, 若类型是 arg，记录下来;
             */
            override fun visit(n: CatchClause, arg: MutableMap<String, ClassInfo>){

                val catchCauseType = n.parameter.type
                val referByCatchCauseType = referredTarget(catchCauseType)
                arg.putAll(referByCatchCauseType)

                super.visit(n, arg)

            }

            /**
             * foreach语句：分析类对象创建语句的对象类型, 若类型是 arg，记录下来;
             * 如：for(String str: list) -> String
             */
            override fun visit(n: ForEachStmt, arg: MutableMap<String, ClassInfo>) {

                val forVariableType  = n.variable.variables[0].type
                val referByForVariableType = referredTarget(forVariableType)
                arg.putAll(referByForVariableType)

                super.visit(n, arg)

            }

            /**
             * for语句：分析类对象创建语句的对象类型, 若类型是 arg，记录下来;
             * 如：for(int i = 0;i<length;i++) -> int
             */
            override fun visit(n: ForStmt, arg: MutableMap<String, ClassInfo>) {
                if(!n.initialization.isEmpty()){
                    val initializationExpr = n.initialization[0]
                    if(initializationExpr is VariableDeclarationExpr){
                        val forVariableType = initializationExpr.getVariable(0).type
                        val referByForVariableType = referredTarget(forVariableType)
                        arg.putAll(referByForVariableType)

                    }
                }
                super.visit(n, arg)

            }

            /**
             * 解析 type 引用了 arg 的哪些类
             * @param
             * @return
             */
            private fun referredTarget(type: Type):Map<String, ClassInfo>{
                val res = mutableMapOf<String, ClassInfo>()
                val parsedFullNames = JavaParserUtil.parseSplitFullNames(type)
                parsedFullNames.forEach {
                    if(moduleClassInfoContext.containsClassName(it)){
                        res[it] = moduleClassInfoContext.getClassInfoByName(it)!!
                        return@forEach
                    }
                    if(srcBaseClassInfoContext.containsClassName(it)){
                        res[it] = srcBaseClassInfoContext.getClassInfoByName(it)!!
                        return@forEach
                    }
                }
                return res
            }

            private fun referredTarget(annotationExpr: AnnotationExpr):Map<String, ClassInfo>{
                val res = mutableMapOf<String, ClassInfo>()
                val parsedFullName = JavaParserUtil.parseFullName(annotationExpr)

                if(moduleClassInfoContext.containsClassName(parsedFullName)){
                    res[parsedFullName] = moduleClassInfoContext.getClassInfoByName(parsedFullName)!!
                }
                if(srcBaseClassInfoContext.containsClassName(parsedFullName)){
                    res[parsedFullName] = srcBaseClassInfoContext.getClassInfoByName(parsedFullName)!!
                }
                return res
            }


        },dependentClass)

        // 排除自己
        val fullName = JavaParserUtil.parseFullName(cu)
        dependentClass.remove(fullName)
        return dependentClass
    }

    override fun checkPreCondition(absolutePath: Path?,compilationUnit: CompilationUnit,arg: SplitModuleContext?):Boolean{
        return JavaParserUtil.isValidJavaFile(compilationUnit)
    }

}
