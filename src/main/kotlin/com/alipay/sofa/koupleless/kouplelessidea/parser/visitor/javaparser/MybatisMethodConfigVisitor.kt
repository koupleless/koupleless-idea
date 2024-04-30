package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/12 15:39
 */
object MybatisMethodConfigVisitor: JavaParserVisitor<ProjectContext>() {
    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: ProjectContext?) {
        // 解析 dataSource
        parseDataSource(absolutePath!!,compilationUnit, arg!!)

        // 解析 SqlSessionFactoryBean
        parseSqlSessionFactoryBean(absolutePath,compilationUnit, arg)

        // 解析 PlatformTransactionManager
        parsePlatformTransactionManager(absolutePath,compilationUnit, arg)

        // 解析 TransactionTemplate
        parseTransactionTemplate(absolutePath,compilationUnit, arg)

        // 解析 SqlSessionTemplate
        parseSqlSessionTemplate(absolutePath,compilationUnit, arg)
    }

    private fun parseSqlSessionTemplate(absolutePath:Path,compilationUnit: CompilationUnit, arg: ProjectContext) {
        val sqlSessionTemplateMethods = JavaParserUtil.filterBeanMethodsByReturnType(compilationUnit.getType(0), SplitConstants.SQL_SESSION_TEMPLATE)
        sqlSessionTemplateMethods.forEach {
            val beanNames = ParseBeanService.parseBeanName(it)
            val beanInfo = arg.beanContext.getBeanByName(beanNames.first())!!

            val dbContext = arg.configContext.dbContext
            val sqlSessionFactoryBeanRef = parseSqlSessionFactoryBeanRefForSqlSessionTemplate(compilationUnit,it, beanInfo)

            dbContext.registerSqlSessionTemplate(DBContext.SqlSessionTemplate(beanInfo,sqlSessionFactoryBeanRef))
        }
    }

    private fun parseSqlSessionFactoryBeanRefForSqlSessionTemplate(cu: CompilationUnit, method: MethodDeclaration, methodBeanInfo: BeanInfo): BeanRef? {
        // 1. 根据入参解析
        val sqlSessionFactoryBeanRefFromParam = parseSqlFactoryBeanRefFromParamForSqlTemplate(method,methodBeanInfo)
        if(sqlSessionFactoryBeanRefFromParam!=null) return sqlSessionFactoryBeanRefFromParam

        // 2. 根据字段解析
        val sqlSessionFactoryBeanRefFromField = parseSqlFactoryBeanRefFromFieldForSqlTemplate(cu,method,methodBeanInfo)
        if(sqlSessionFactoryBeanRefFromField!=null) return sqlSessionFactoryBeanRefFromField

        return null
    }

    private fun parseSqlFactoryBeanRefFromFieldForSqlTemplate(cu: CompilationUnit, method: MethodDeclaration,methodBeanInfo: BeanInfo): BeanRef? {
        val sqlSessionTemplateCreationStat = JavaParserUtil.filterObjectCreationStatInMethodBody(method,"SqlSessionTemplate").firstOrNull()
        sqlSessionTemplateCreationStat?:return null

        val sqlSessionFactoryVar = sqlSessionTemplateCreationStat.arguments.first()
        if(!sqlSessionFactoryVar.isNameExpr) return null

        val fieldName = sqlSessionFactoryVar.asNameExpr().nameAsString
        val fieldDeclaration = JavaParserUtil.filterFieldByName(cu.getType(0),fieldName)
        fieldDeclaration?:return null

        val annotations = JavaParserUtil.filterAnnotations(fieldDeclaration, SplitConstants.AUTOWIRED_ANNOTATIONS)
        if(annotations.isEmpty()) return null

        return ParseBeanService.parseBeanRef(fieldDeclaration,methodBeanInfo)
    }

    private fun parseDataSource(absolutePath:Path,compilationUnit: CompilationUnit, arg: ProjectContext) {
        val customDataSource = arg.analyseConfig.getCustomDataSourceClasses()
        val dataSourceMethods = JavaParserUtil.filterBeanMethodsByReturnType(compilationUnit.getType(0), SplitConstants.DATA_SOURCE + customDataSource)
        dataSourceMethods.forEach {
            val beanName = ParseBeanService.parseBeanName(it).first()
            val beanInfo = arg.beanContext.getBeanByName(beanName)!!

            val dbContext = arg.configContext.dbContext
            dbContext.registerDataSource(DBContext.MybatisDataSource(beanInfo))
        }
    }

    private fun parseTransactionTemplate(absolutePath:Path, compilationUnit: CompilationUnit, arg: ProjectContext) {
        val transactionTemplateMethods = JavaParserUtil.filterBeanMethodsByReturnType(compilationUnit.getType(0), SplitConstants.TRANSACTION_TEMPLATE)
        transactionTemplateMethods.forEach {
            val beanNames = ParseBeanService.parseBeanName(it)
            val beanInfo = arg.beanContext.getBeanByName(beanNames.first())!!

            val dbContext = arg.configContext.dbContext
            val transactionManagerRef = parseTransactionManagerBeanRef(compilationUnit,it, beanInfo)
            dbContext.registerTransactionTemplate(DBContext.TransactionTemplate(beanInfo,transactionManagerRef))
        }
    }

    private fun parseTransactionManagerBeanRef(cu: CompilationUnit, method: MethodDeclaration,methodBeanInfo: BeanInfo): BeanRef? {
        // 1. 根据入参解析
        val transactionManagerBeanRefFromParam = parseTransactionManagerBeanRefFromParam(method,methodBeanInfo)
        if(transactionManagerBeanRefFromParam!=null) return transactionManagerBeanRefFromParam

        // 2. 根据字段解析
        val transactionManagerBeanRefFromField = parseTransactionManagerBeanRefFromField(cu,method,methodBeanInfo)
        if(transactionManagerBeanRefFromField!=null) return transactionManagerBeanRefFromField

        // 3. 根据方法解析
        val transactionManagerBeanRefFromMethod = parseTransactionManagerBeanRefFromMethod(cu,method,methodBeanInfo)
        if(transactionManagerBeanRefFromMethod != null) return transactionManagerBeanRefFromMethod

        return null
    }

    private fun parseTransactionManagerBeanRefFromMethod(cu: CompilationUnit, method: MethodDeclaration,methodBeanInfo: BeanInfo): BeanRef? {
        val transactionManagerExpression = parseTransactionManagerExpression(method)
        val methodName = if(transactionManagerExpression!=null && transactionManagerExpression.isMethodCallExpr){
            transactionManagerExpression.asMethodCallExpr().nameAsString
        }else{
            null
        }
        methodName?:return null

        val transactionManagerMethod = JavaParserUtil.filterMethodByName(cu.getType(0),methodName).firstOrNull()
        transactionManagerMethod?:return null
        return ParseBeanService.parseBeanRef(transactionManagerMethod,methodBeanInfo)
    }

    private fun parseTransactionManagerBeanRefFromField(cu: CompilationUnit, method: MethodDeclaration,methodBeanInfo: BeanInfo): BeanRef? {
        val transactionManagerExpression = parseTransactionManagerExpression(method)
        val fieldName = if(transactionManagerExpression!=null && transactionManagerExpression.isNameExpr){
            transactionManagerExpression.asNameExpr().nameAsString
        }else{
            null
        }
        fieldName?:return null

        val fieldDeclaration = JavaParserUtil.filterFieldByName(cu.getType(0),fieldName)
        fieldDeclaration?:return null

        val annotations = JavaParserUtil.filterAnnotations(fieldDeclaration, SplitConstants.AUTOWIRED_ANNOTATIONS)
        if(annotations.isEmpty()) return null

        return ParseBeanService.parseBeanRef(fieldDeclaration,methodBeanInfo)
    }

    private fun parseTransactionManagerBeanRefFromParam(method: MethodDeclaration,methodBeanInfo:BeanInfo): BeanRef? {
        val transactionManagerExpression = parseTransactionManagerExpression(method)
        val paramName = if(transactionManagerExpression!=null && transactionManagerExpression.isNameExpr){
            transactionManagerExpression.asNameExpr().nameAsString
        }else{
            null
        }
        paramName?:return null

        val transactionManagerParam = method.parameters.firstOrNull { param -> param.nameAsString == paramName }
        transactionManagerParam?:return null
        return ParseBeanService.parseBeanRef(transactionManagerParam,methodBeanInfo)
    }

    private fun parseTransactionManagerExpression(method: MethodDeclaration): Expression?{
        // 1. 查找初始化语句
        val templateCreationStat = JavaParserUtil.filterObjectCreationStatInMethodBody(method,"TransactionTemplate").firstOrNull()
        templateCreationStat?:return null

        var transactionManagerExpression = templateCreationStat.arguments.firstOrNull()
        if(transactionManagerExpression!=null){
            return transactionManagerExpression
        }

        // 2. 查找 set 语句
        val setStat = JavaParserUtil.filterMethodCallStatInMethodBody(method,"setTransactionManager").firstOrNull()
        setStat?:return null

        transactionManagerExpression = setStat.arguments.firstOrNull()
        if(transactionManagerExpression!=null ){
            return transactionManagerExpression
        }

        return null
    }

    private fun parsePlatformTransactionManager(absolutePath:Path,compilationUnit: CompilationUnit, arg: ProjectContext) {
        val platformTransactionManagerMethods = JavaParserUtil.filterBeanMethodsByReturnType(compilationUnit.getType(0), SplitConstants.PLATFORM_TRANSACTION_MANAGER)
        platformTransactionManagerMethods.forEach {
            val beanNames = ParseBeanService.parseBeanName(it)
            val beanInfo = arg.beanContext.getBeanByName(beanNames.first())!!

            val dbContext = arg.configContext.dbContext
            val dataSourceBeanRef = parseDataSourceBeanRef(compilationUnit,it,beanInfo)
            dbContext.registerPlatformTransactionManager(DBContext.PlatformTransactionManager(beanInfo,dataSourceBeanRef))

        }
    }

    /**
     * 解析出
     * @param
     * @return
     */
    private fun parseSqlSessionFactoryBean(absolutePath:Path,compilationUnit: CompilationUnit, arg: ProjectContext) {
        val sqlSessionFactoryBeanMethods = JavaParserUtil.filterBeanMethodsByReturnType(compilationUnit.getType(0), SplitConstants.SQL_SESSION_FACTORY_BEAN)

        sqlSessionFactoryBeanMethods.forEach {
            val beanNames = ParseBeanService.parseBeanName(it)
            val beanInfo = arg.beanContext.getBeanByName(beanNames.first())!!

            // 补充接口信息
            beanInfo.interfaceTypes.addAll(setOf("org.apache.ibatis.session.SqlSessionFactory"))
            arg.beanContext.associateBeanInfoWithInterfaces(setOf("org.apache.ibatis.session.SqlSessionFactory"),beanInfo)

            // 注册
            val dataSourceBeanRef = parseDataSourceBeanRef(compilationUnit,it,beanInfo)
            val mapperLocationExists = existsMapperLocation(it)
            val sqlSessionFactory = DBContext.SqlSessionFactoryInfo(beanInfo,dataSourceBeanRef,mapperLocationExists)
            val dbContext = arg.configContext.dbContext
            dbContext.registerSqlSessionFactory(sqlSessionFactory)
        }
    }


    private fun existsMapperLocation(method: MethodDeclaration):Boolean{
        return JavaParserUtil.filterMethodCallStatInMethodBody(method,"setMapperLocations").isNotEmpty()
    }

    private fun parseSqlFactoryBeanRefFromParamForSqlTemplate(method: MethodDeclaration,methodBeanInfo: BeanInfo):BeanRef?{
        val sqlSessionTemplateCreationStat = JavaParserUtil.filterObjectCreationStatInMethodBody(method,"SqlSessionTemplate").firstOrNull()
        sqlSessionTemplateCreationStat?:return null

        val sqlSessionFactoryVar = sqlSessionTemplateCreationStat.arguments.first()
        if(!sqlSessionFactoryVar.isNameExpr) return null

        val paramName = sqlSessionFactoryVar.asNameExpr().nameAsString
        val sqlSessionFactoryParam = method.parameters.firstOrNull { param -> param.nameAsString == paramName }
        sqlSessionFactoryParam?:return null
        return ParseBeanService.parseBeanRef(sqlSessionFactoryParam,methodBeanInfo)
    }

    private fun parseDataSourceBeanRefFromParam(method:MethodDeclaration,methodBeanInfo:BeanInfo):BeanRef?{
        val setDataSourceStat = JavaParserUtil.filterMethodCallStatInMethodBody(method,"setDataSource").firstOrNull()
        setDataSourceStat?:return null

        val dataSourceVar = setDataSourceStat.arguments.first()
        if(!dataSourceVar.isNameExpr) return null

        val paramName = dataSourceVar.asNameExpr().nameAsString
        val dataSourceParam = method.parameters.firstOrNull { param -> param.nameAsString == paramName }
        dataSourceParam?:return null

        return ParseBeanService.parseBeanRef(dataSourceParam,methodBeanInfo)
    }

    private fun parseDataSourceBeanRefFromField(compilationUnit: CompilationUnit,method:MethodDeclaration,methodBeanInfo: BeanInfo):BeanRef?{
        // 2. 根据字段解析：即从这个类的字段中找到对应类型的 bean
        // 2.1 解析需要的字段名称
        val setDataSourceStat = JavaParserUtil.filterMethodCallStatInMethodBody(method,"setDataSource").firstOrNull()
        setDataSourceStat?:return null

        val dataSourceParam = setDataSourceStat.arguments.first()
        if(!dataSourceParam.isNameExpr) return null

        // 2.2 解析字段的beanName
        val fieldName = dataSourceParam.asNameExpr().nameAsString
        val fieldDeclaration = JavaParserUtil.filterFieldByName(compilationUnit.getType(0),fieldName)
        fieldDeclaration?:return null

        val annotations = JavaParserUtil.filterAnnotations(fieldDeclaration, SplitConstants.AUTOWIRED_ANNOTATIONS)
        if(annotations.isEmpty()) return null

        return ParseBeanService.parseBeanRef(fieldDeclaration,methodBeanInfo)
    }

    private fun parseDataSourceBeanRef(compilationUnit: CompilationUnit,method:MethodDeclaration,methodBeanInfo: BeanInfo):BeanRef?{
        // 1. 根据入参解析
        val dataSourceBeanRefFromParam = parseDataSourceBeanRefFromParam(method,methodBeanInfo)
        if(dataSourceBeanRefFromParam!=null) return dataSourceBeanRefFromParam

        // 2. 根据字段解析
        val dataSourceBeanRefFromField = parseDataSourceBeanRefFromField(compilationUnit,method,methodBeanInfo)
        return dataSourceBeanRefFromField
    }

    override fun checkPreCondition(
        absolutePath: Path?,
        compilationUnit: CompilationUnit,
        arg: ProjectContext?
    ): Boolean {
        if(!JavaParserUtil.isClassDeclaration(compilationUnit)){
            return false
        }

        val type = compilationUnit.getType(0)
        val beanAnnotationToDetect = arg!!.analyseConfig.getCustomBeanAnnotations().toMutableSet()+ SplitConstants.BEAN_ANNOTATIONS
        val beanAnnotations = JavaParserUtil.filterAnnotations(type, beanAnnotationToDetect)
        return beanAnnotations.isNotEmpty()
    }
}
