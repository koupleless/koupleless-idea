package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.getValueOfAnnotation
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.MAPPER_SCAN_ANNOTATION
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/4 21:17
 */
object MybatisConfigVisitor: JavaParserVisitor<ProjectContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: ProjectContext?) {
        // 解析 MapperScan
        parseMapperScan(absolutePath!!,compilationUnit, arg!!)
    }

    override fun checkPreCondition(
        absolutePath: Path?,
        compilationUnit: CompilationUnit,
        arg: ProjectContext?
    ): Boolean {

        val isValidJavaFile = JavaParserUtil.isValidJavaFile(compilationUnit)
        if(!isValidJavaFile) return false

        val isClass = JavaParserUtil.isClassDeclaration(compilationUnit)
        if(!isClass) return false

        val type = compilationUnit.getType(0)
        val mapperScanAnno = JavaParserUtil.filterAnnotations(type,setOf(MAPPER_SCAN_ANNOTATION))
        return mapperScanAnno.isNotEmpty()
    }

    /**
     * 扫描的MapperScan：
     * @MapperScan(basePackages = "com.xxx.mapper",
     *         sqlSessionFactoryRef = "sqlSessionFactoryBeanForSingle")
     * 或者
     * @MapperScan(basePackages = {"com.xxx.mapper"},
     *         sqlSessionFactoryRef = "sqlSessionFactoryBeanForSingle")
     * @param
     * @return
     */
    private fun parseMapperScan(absolutePath: Path,compilationUnit: CompilationUnit, arg: ProjectContext){
        val type = compilationUnit.getType(0)
        val mapperScanAnno = JavaParserUtil.filterAnnotations(type,setOf(MAPPER_SCAN_ANNOTATION)).first()
        if(mapperScanAnno is NormalAnnotationExpr){
            val basePackagesStr = getValueOfAnnotation(mapperScanAnno, "basePackages")
            val basePackages = basePackagesStr?.removePrefix("{")?.removeSuffix("}")?.replace(("\"|\\s").toRegex(),"")?.split(",")?: emptyList()
            val sqlSessionFactoryRef = getValueOfAnnotation(mapperScanAnno, "sqlSessionFactoryRef")
            val sqlSessionTemplateRef = getValueOfAnnotation(mapperScanAnno, "sqlSessionTemplateRef")

            // 配置模块化的信息
            val beanName = ParseBeanService.getBeanName(type)
            val beanInfo = arg.beanContext.getBeanByName(beanName)!!
            val mybatisConfig = DBContext.MybatisConfig(beanInfo,basePackages.toSet(),sqlSessionFactoryRef,sqlSessionTemplateRef)

            val dbContext = arg.configContext.dbContext
            dbContext.registerMybatisConfig(mybatisConfig)
        }
    }
}
