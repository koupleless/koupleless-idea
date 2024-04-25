package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/18 22:32
 */
class TransactionTemplateBeanReuser(filePath: String, val transactionTemplate: DBContext.TransactionTemplate): JavaFileModifier(filePath) {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        val beanInfo = transactionTemplate.beanInfo
        if(beanInfo.definedByMethod()){
            val beanMethodSignature = beanInfo.getAttribute(SplitConstants.METHOD_BEAN) as String
            val method = JavaParserUtil.filterMethodBySignature(compilationUnit.getType(0),beanMethodSignature).first()

            // 清空 transactionTemplate 方法内部
            method.body.get().statements.clear()

            // 清空 transactionTemplate 方法参数
            method.parameters.clear()

            // 字段中的 sqlFactoryBean 可以不清理

            // 添加 transactionTemplate 复用
            val returnType = JavaParserUtil.parseFullName(method.type)

            val returnStat = StaticJavaParser.parseStatement("return (${returnType}) SpringBeanFinder.getBaseBean(\"${beanInfo.beanName}\");")


            method.body.get().addStatement(returnStat)
        }
    }
}
