package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/15 10:13
 */
class SqlSessionFactoryBeanReuser(filePath: String, private val contentPanel:ContentPanel): JavaFileModifier(filePath) {
    private var dataSource: DBContext.MybatisDataSource?=null
    fun setDataSource(myDataSource: DBContext.MybatisDataSource){
        dataSource = myDataSource
    }

    fun getDataSource(): DBContext.MybatisDataSource?{
        return dataSource
    }
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        val sqlSessionFactoryBeanMethods = JavaParserUtil.filterBeanMethodsByReturnType(compilationUnit.getType(0), SplitConstants.SQL_SESSION_FACTORY_BEAN)
        sqlSessionFactoryBeanMethods.forEach {method->
            setDataSource(method)

            setMapperLocations(method,compilationUnit)
        }

        super.doParse(absolutePath, compilationUnit, arg)
    }

    private fun setMapperLocations(method: MethodDeclaration,cu: CompilationUnit) {
        if(!setMapperLocationStatExists(method)) return

        setResolveMapperLocationMethod(cu)

        // 修改 mapperLocations
        modifyMapperLocation(method)
    }
    private fun modifyMapperLocation(method: MethodDeclaration){
        val setMapperLocationStat = JavaParserUtil.filterMethodCallStatInMethodBody(method, "setMapperLocations").firstOrNull()
        setMapperLocationStat?:return

        setMapperLocationStat.arguments.clear()
        setMapperLocationStat.arguments.add(StaticJavaParser.parseExpression("resolveMapperLocations()"))
    }

    private fun setMapperLocationStatExists(method: MethodDeclaration): Boolean {
        val setMapperLocationStat = JavaParserUtil.filterMethodCallStatInMethodBody(method, "setMapperLocations").firstOrNull()
        return setMapperLocationStat!=null
    }

    private fun setResolveMapperLocationMethod(cu: CompilationUnit){
        // 添加 resolveMapperLocations 方法
        val resolveMapperLocationsMethod = JavaParserUtil.filterMethodByName(cu.getType(0),"resolveMapperLocations")
        if(resolveMapperLocationsMethod.isNotEmpty()) return

        val templateUrl = this.javaClass.classLoader.getResource(SplitConstants.MYBATIS_SQL_SESSION_FACTORY_CONFIG_TEMPLATE_RESOURCE)
        val inputStream = templateUrl!!.openStream()
        inputStream.use { input->
            val tmpType = StaticJavaParser.parse(input).getType(0)
            val methodToAdd = JavaParserUtil.filterMethodByName(tmpType,"resolveMapperLocations").first().clone()
            val mapperLocationStat = StaticJavaParser.parseStatement("ArrayList<String> mapperLocations = new ArrayList<>(Arrays.asList(\"${SplitConstants.DEFAULT_XML_MAPPER_LOCATION}\"));")
            methodToAdd.body.get().addStatement(0,mapperLocationStat)

            methodModifier.addMethodNow(cu, methodToAdd)
        }

        importModifier.addImport(ImportDeclaration("java.util.ArrayList",false,false))
        importModifier.addImport(ImportDeclaration("java.util.Arrays",false,false))
        importModifier.addImport(ImportDeclaration("org.springframework.core.io.Resource",false,false))
    }

    private fun setDataSource(method: MethodDeclaration) {
        dataSource?.let {
            // 1. 清理方法参数中的 DataSource
            clearDataSourceParam(method)

            // 2. 方法内部引入基座 dataSource
            importDataSource(method)

            // 3. 清理 field 中的 dataSource
            clearDataSourceField(method)
        }
    }

    private fun clearDataSourceField(method: MethodDeclaration) {
        val dataSourceVar = parseDataSourceVarName(method)
        dataSourceVar?.let {
            fieldModifier.removeField(dataSourceVar)
        }
    }

    private fun clearDataSourceParam(method: MethodDeclaration){
        val dataSourceVar = parseDataSourceVarName(method)
        dataSourceVar?.let {
            method.parameters.removeIf { param -> param.nameAsString == dataSourceVar  }
        }
    }

    private fun importDataSource(method: MethodDeclaration) {
        dataSource?.let {
            val dataSourceVar = parseDataSourceVarName(method)
            val dataSourceType = it.beanInfo.fullClassName!!.substringAfterLast(".")
            val dataSourceBeanId = it.beanInfo.beanName!!
            val getBaseDataSourceStat = StaticJavaParser.parseStatement("$dataSourceType $dataSourceVar = (${dataSourceType}) SpringBeanFinder.getBaseBean(\"${dataSourceBeanId}\");")

            method.body.get().addStatement(0,getBaseDataSourceStat)

            importModifier.addImport(ImportDeclaration("com.alipay.zdal.client.jdbc.ZdalDataSource",false,false))
            importModifier.addImport(ImportDeclaration("com.alipay.sofa.koupleless.common.api.SpringBeanFinder",false,false))
        }
    }

    private fun parseDataSourceVarName(method: MethodDeclaration): String? {
        val setDataSourceStat = JavaParserUtil.filterMethodCallStatInMethodBody(method, "setDataSource").firstOrNull()
        setDataSourceStat ?: return null

        val dataSourceVar = setDataSourceStat.arguments.first()
        if (!dataSourceVar.isNameExpr) return null

        return dataSourceVar.asNameExpr().nameAsString
    }
}
