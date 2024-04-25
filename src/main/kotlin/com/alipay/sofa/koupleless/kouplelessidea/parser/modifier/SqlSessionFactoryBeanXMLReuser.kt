package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.BuildJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.DEFAULT_REUSE_MYBATIS_FACTORY_XML_BEAN_METHOD
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/19 22:11
 */
class SqlSessionFactoryBeanXMLReuser(filePath: String, val sqlSessionFactoryInfo:DBContext.SqlSessionFactoryInfo, val contentPanel: ContentPanel): JavaFileModifier(filePath) {
    private var dataSource: DBContext.MybatisDataSource?=null
    private val pluginBeanInfoList = mutableListOf<BeanInfo>()
    fun setDataSource(myDataSource: DBContext.MybatisDataSource){
        dataSource = myDataSource
    }
    fun addPlugins(beans: List<BeanInfo>){
        pluginBeanInfoList.addAll(beans)
    }

    fun getPlugins(): List<BeanInfo> {
        return pluginBeanInfoList
    }

    fun getDataSource(): DBContext.MybatisDataSource? {
        return dataSource
    }

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        val templateCu = getTemplateCu()

        val method = copySqlSessionFactoryBeanMethod(compilationUnit,templateCu)

        setBeanName(method)

        setDataSource(method)

        setMapperLocations(method,compilationUnit,templateCu)

        setConfigLocation(method)

        setPlugins(compilationUnit,method)

        super.doParse(absolutePath, compilationUnit, arg)
    }

    private fun getTemplateCu(): CompilationUnit {
        val templateUrl = this.javaClass.classLoader.getResource(SplitConstants.MYBATIS_SQL_SESSION_FACTORY_CONFIG_TEMPLATE_RESOURCE)
        val inputStream = templateUrl!!.openStream()
        var tmpCu :CompilationUnit?
        inputStream.use { input->
            tmpCu = StaticJavaParser.parse(input)
        }

        if(tmpCu==null){
            contentPanel.printMavenErrorLog("解析模板文件失败:${SplitConstants.MYBATIS_SQL_SESSION_FACTORY_CONFIG_TEMPLATE_RESOURCE}")
        }
        return tmpCu!!
    }

    private fun setMapperLocations(method: MethodDeclaration,cu: CompilationUnit,templateCu: CompilationUnit) {
        if(sqlSessionFactoryInfo.mapperLocationExists){
            // 添加方法
            setResolveMapperLocationMethod(cu,templateCu)
        }else{
            // 删除设置语句
            val setMapperLocationStat = JavaParserUtil.filterMethodCallStatInMethodBody(method, "setMapperLocations").firstOrNull()
            setMapperLocationStat?.remove()
        }
    }


    private fun setResolveMapperLocationMethod(cu: CompilationUnit,templateCu: CompilationUnit){
        // 添加方法
        val resolveMapperLocationMethod = JavaParserUtil.filterMethodByName(templateCu.getType(0),"resolveMapperLocations").first()
        val mapperLocationStat = StaticJavaParser.parseStatement("ArrayList<String> mapperLocations = new ArrayList<>(Arrays.asList(\"${SplitConstants.DEFAULT_XML_MAPPER_LOCATION}\"));")
        resolveMapperLocationMethod.body.get().addStatement(0,mapperLocationStat)

        methodModifier.addMethodNow(cu,resolveMapperLocationMethod)
    }

    private fun setPlugins(cu: CompilationUnit, method: MethodDeclaration) {
        if(pluginBeanInfoList.isEmpty()) return

        createPluginMethodBeans(cu)

        addPlugins(method)

        importModifier.addImport(ImportDeclaration("org.apache.ibatis.plugin.Interceptor",false,false))
    }

    private fun addPlugins(method: MethodDeclaration) {
        if(pluginBeanInfoList.isEmpty()) return

        // 添加入参
        val params = pluginBeanInfoList.mapIndexed { _, beanInfo ->
            buildBeanParam(simpleType="Interceptor",name= beanInfo.beanName!!,beanName=beanInfo.beanName)
        }
        method.parameters.addAll(params)

        // 添加设置语句
        val paramNames = pluginBeanInfoList.map{it.beanName }.toList()
        val stat1 = StaticJavaParser.parseStatement("ArrayList<Interceptor> pluginList = new ArrayList<>(Arrays.asList(${paramNames.joinToString(",")}));")
        val stat2 = StaticJavaParser.parseStatement("Interceptor[] plugins = pluginList.toArray(new Interceptor[0]);")
        val stat3 = StaticJavaParser.parseStatement("mysqlSqlFactory.setPlugins(plugins);")

        val statements = method.body.get().statements
        statements.addAll(statements.size-1, listOf(stat1,stat2,stat3))
    }

    private fun createPluginMethodBeans(cu: CompilationUnit) {
        if(pluginBeanInfoList.isEmpty()) return

        val pluginsInBase = pluginBeanInfoList.filterNot { it.parentContext!=null && it.parentContext!!.parentContext is ModuleContext }.toList()
        val pluginMethods = pluginsInBase.map { pluginBeanInfo ->
            val pluginBeanName = pluginBeanInfo.beanName!!
            BuildJavaService.buildReuseBeanMethod(methodName=pluginBeanName,"Interceptor",beanName=pluginBeanName,pluginBeanInfo.getModularName())
        }

        methodModifier.addMethodsNow(cu,pluginMethods)
    }

    private fun buildBeanParam(simpleType:String,name:String,beanName:String):Parameter{
        val type = StaticJavaParser.parseClassOrInterfaceType(simpleType)
        val param = Parameter(type,name).apply {
            this.addAnnotation(SingleMemberAnnotationExpr(Name("Qualifier"),StringLiteralExpr(beanName)))
        }
        return param
    }

    private fun setConfigLocation(method:MethodDeclaration) {
        sqlSessionFactoryInfo.configLocation?.let {
            val stat = StaticJavaParser.parseStatement("bean.setConfigLocation(new ClassPathResource(\"${SplitConstants.DEFAULT_MYBATIS_CONFIG_CLASSPATH_LOCATION}\"));")
            val statSize = method.body.get().statements.size
            method.body.get().addStatement(statSize-1,stat)

            importModifier.addImport(ImportDeclaration("org.springframework.core.io.ClassPathResource",false,false))
        }
    }

    private fun setDataSource(method: MethodDeclaration) {
        if(dataSource==null){
            contentPanel.printMavenLog("数据源复用：无法配置数据源，请根据文档自行配置")
            return
        }

        val dataSourceBean = dataSource!!.beanInfo
        val dataSourceBeanId = dataSourceBean.beanName!!
        val getBaseDataSourceStat = StaticJavaParser.parseStatement("DataSource dataSource = (DataSource) SpringBeanFinder.getBaseBean(\"${dataSourceBeanId}\");")


        method.body.get().addStatement(0,getBaseDataSourceStat)
    }

    private fun setBeanName(method: MethodDeclaration){
        val beanName = sqlSessionFactoryInfo.beanInfo.beanName!!
        val beanAnno = JavaParserUtil.filterAnnotation(method, SplitConstants.BEAN_ANNOTATION)!!
        val namePair = JavaParserUtil.filterAnnoAttributePair(beanAnno,"name")!!
        namePair.setValue(StringLiteralExpr(beanName))
    }

    private fun copySqlSessionFactoryBeanMethod(cu:CompilationUnit,templateCu: CompilationUnit):MethodDeclaration{
        val type = cu.getType(0)
        methodModifier.addMethodNow(cu,JavaParserUtil.filterMethodByName(templateCu.getType(0),DEFAULT_REUSE_MYBATIS_FACTORY_XML_BEAN_METHOD).first())
        return JavaParserUtil.filterMethodByName(type,DEFAULT_REUSE_MYBATIS_FACTORY_XML_BEAN_METHOD).first()
    }
}
