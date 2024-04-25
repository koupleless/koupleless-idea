package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.expr.ArrayInitializerExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.StringLiteralExpr
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/26 11:01
 */
class MybatisConfigXMLReusier(filePath: String,private val contentPanel: ContentPanel,private val mybatisConfig: DBContext.MybatisConfig): JavaFileModifier(filePath) {
    val newBasePackages = mutableListOf<String>()

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        configBasePackages(compilationUnit)

        configSqlSessionFactoryRef(compilationUnit)
    }

    private fun configSqlSessionFactoryRef(cu: CompilationUnit) {
        val sqlSessionFactoryRef = mybatisConfig.sqlSessionFactoryRef
        sqlSessionFactoryRef?:return

        val type = cu.getType(0)
        val mapperScanAnno = JavaParserUtil.filterAnnotations(type,setOf(SplitConstants.MAPPER_SCAN_ANNOTATION)).first()
        val sqlSessionFactoryRefPair = JavaParserUtil.filterAnnoAttributePair(mapperScanAnno,"sqlSessionFactoryRef")
        if(sqlSessionFactoryRefPair==null){
            val pair = MemberValuePair("sqlSessionFactoryRef",StringLiteralExpr(sqlSessionFactoryRef))
            mapperScanAnno.asNormalAnnotationExpr().pairs.add(pair)
        }else{
            sqlSessionFactoryRefPair.setValue(StringLiteralExpr(sqlSessionFactoryRef))
        }
    }

    private fun configBasePackages(cu: CompilationUnit) {
        contentPanel.printMavenLog("数据库配置：将自动配置模块 MapperScan 中的 basePackages 为：${newBasePackages.joinToString(",")}")

        val type = cu.getType(0)

        // 修改 MapperScan 的 basePackages
        val mapperScanAnno = JavaParserUtil.filterAnnotations(type,setOf(SplitConstants.MAPPER_SCAN_ANNOTATION)).first()
        val basePackageNode = JavaParserUtil.filterAnnoAttributePair(mapperScanAnno,"basePackages")
        basePackageNode?.let{
            val basePackagesLiteralExprs = newBasePackages.map { StringLiteralExpr(it) }
            basePackageNode.setValue(ArrayInitializerExpr(NodeList(basePackagesLiteralExprs)))
        }
    }
}
