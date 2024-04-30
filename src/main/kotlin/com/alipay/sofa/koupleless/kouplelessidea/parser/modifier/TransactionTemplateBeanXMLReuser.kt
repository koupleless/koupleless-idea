package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.BuildJavaService
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/24 17:37
 */
class TransactionTemplateBeanXMLReuser(filePath: String, val transactionTemplate: DBContext.TransactionTemplate, val contentPanel: ContentPanel): JavaFileModifier(filePath) {

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        val beanInfo =  transactionTemplate.beanInfo
        if(beanInfo.beanName==null){
            contentPanel.printMavenLog("数据源复用：无法配置 TransactionTemplate，因为 beanName 为空，请根据文档自行配置")
            return
        }

        // 添加 bean 方法
        val beanMethod = BuildJavaService.buildReuseBeanMethod(methodName = beanInfo.beanName,"TransactionTemplate",beanName = beanInfo.beanName,beanInfo.getModularName())
        methodModifier.addMethodNow(compilationUnit,beanMethod)

        // 添加 import
        importModifier.addImport(ImportDeclaration("org.springframework.transaction.support.TransactionTemplate",false,false))
        importModifier.addImport(ImportDeclaration("org.springframework.context.annotation.Bean",false,false))

        super.doParse(absolutePath, compilationUnit, arg)
    }
}
