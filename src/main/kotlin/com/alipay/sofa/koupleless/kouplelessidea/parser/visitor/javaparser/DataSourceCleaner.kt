package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.modifier.JavaFileModifier
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/19 11:07
 */
class DataSourceCleaner(filePath: String,private val dataSource: DBContext.MybatisDataSource): JavaFileModifier(filePath) {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        val beanInfo = dataSource.beanInfo
        if(beanInfo.definedByMethod()){
            val beanMethodSignature = beanInfo.getAttribute(SplitConstants.METHOD_BEAN) as String
            val method = JavaParserUtil.filterMethodBySignature(compilationUnit.getType(0),beanMethodSignature).first()
            methodModifier.removeMethodNow(method)
        }
    }
}
