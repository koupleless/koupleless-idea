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
 * @date 2023/12/24 17:46
 */
class SqlSessionTemplateBeanXMLReuser(filePath: String, val sqlSessionTemplate: DBContext.SqlSessionTemplate, val contentPanel: ContentPanel): JavaFileModifier(filePath) {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        val beanInfo =  sqlSessionTemplate.beanInfo
        if(beanInfo.beanName==null){
            contentPanel.printMavenLog("数据源复用：无法配置 sqlSessionTemplate，因为 beanName 为空，请根据文档自行配置")
            return
        }

        val beanMethod = BuildJavaService.buildReuseBeanMethod(methodName = beanInfo.beanName,"SqlSessionTemplate",beanName = beanInfo.beanName,beanInfo.getModularName())
        methodModifier.addMethodNow(compilationUnit,beanMethod)

        importModifier.addImport(ImportDeclaration("org.mybatis.spring.SqlSessionTemplate",false,false))
        importModifier.addImport(ImportDeclaration("org.springframework.context.annotation.Bean",false,false))
        super.doParse(absolutePath, compilationUnit, arg)
    }
}
