package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier


import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/18 15:09
 */
class TransactionManagerBeanReuser(filePath: String, val transactionManagerInfo: DBContext.PlatformTransactionManager): JavaFileModifier(filePath) {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        val beanInfo = transactionManagerInfo.beanInfo
        if(beanInfo.definedByMethod()){
            val beanMethodSignature = beanInfo.getAttribute(SplitConstants.METHOD_BEAN) as String
            val method = JavaParserUtil.filterMethodBySignature(compilationUnit.getType(0),beanMethodSignature).first()
            // 清空 transactionManager 方法内部
            method.body.get().statements.clear()

            // 清空 transactionManager 方法参数
            method.parameters.clear()

            // 清理 dataSource 字段
            clearDataSourceField(method)

            // 添加 transactionManager复用
            val returnType = JavaParserUtil.parseFullName(method.type)

            val returnStat = StaticJavaParser.parseStatement("return (${returnType}) SpringBeanFinder.getBaseBean(\"${beanInfo.beanName}\");")


            method.body.get().addStatement(returnStat)

            super.doParse(absolutePath, compilationUnit, arg)
        }
    }

    private fun clearDataSourceField(method: MethodDeclaration){
        val dataSourceVar = parseDataSourceVar(method)
        dataSourceVar?.let {
            fieldModifier.removeField(dataSourceVar)
        }
    }

    private fun parseDataSourceVar(method:MethodDeclaration):String?{
        val setDataSourceStat = JavaParserUtil.filterMethodCallStatInMethodBody(method,"setDataSource").firstOrNull()
        setDataSourceStat?:return null

        val dataSourceParam = setDataSourceStat.arguments.first()
        if(!dataSourceParam.isNameExpr) return null
        return dataSourceParam.asNameExpr().nameAsString
    }
}
