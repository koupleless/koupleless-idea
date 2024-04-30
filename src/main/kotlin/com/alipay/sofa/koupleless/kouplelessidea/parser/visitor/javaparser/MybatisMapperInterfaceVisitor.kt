package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description:
 * 所有在basePackages指定的包下的类都会被扫描，但并不是所有的类都会被识别为Mapper。
 * 1. 接口类使用了@Mapper注解或者使用了@MapperScan指定的包下。
 * 2. 接口类包含了MyBatis的SQL映射语句（如@Select、@Insert、@Update等注解）。
 * 这里只扫描没有对应的 xml 的 mapper 接口类；有对应 xml 的 mapper 已经扫描过了。后续可以再优化成先读 basePackages 的内容，再扫描
 * @author lipeng
 * @date 2023/12/28 09:59
 */
object MybatisMapperInterfaceVisitor: JavaParserVisitor<ProjectContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: ProjectContext?) {
        val type = compilationUnit.types[0]
        val imports = JavaParserUtil.filterImports(compilationUnit,SplitConstants.MYBATIS_SQL_ANNOTATION_IMPORTS)
        if(imports.isNotEmpty()){
            val classInfo = arg!!.classInfoContext.getClassInfoByPath(absolutePath!!.toString())!!
            arg.configContext.dbContext.registerMapperInterface(classInfo)

            // 注册为 bean
            val beanName  =  ParseBeanService.defaultBeanNameOfClass(type.nameAsString)
            arg.beanContext.addBeanInfo(BeanInfo(beanName,classInfo.fullName).apply {
                interfaceTypes.add(classInfo.fullName)
            })
        }
    }

    override fun checkPreCondition(
        absolutePath: Path?,
        compilationUnit: CompilationUnit,
        arg: ProjectContext?
    ): Boolean {
        val isValidJavaFile = JavaParserUtil.isValidJavaFile(compilationUnit)
        if (!isValidJavaFile) return false

        return JavaParserUtil.isInterface(compilationUnit)
    }
}
