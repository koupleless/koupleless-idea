package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.modifier.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.DataSourceCleaner
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/5 15:40
 */
class AnalyseToReuseMybatisConfigPlugin(private val contentPanel: ContentPanel): PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        configureMybatisConfigInModule(splitModuleContext)
    }

    private fun configureMybatisConfigInModule(splitModuleContext: SplitModuleContext){
        val moduleContext = splitModuleContext.moduleContext
        val mapperInterfaces = moduleContext.configContext.dbContext.mapperInterfaces
        val mybatisConfigs = mutableMapOf<DBContext.MybatisConfig,MutableList<ClassInfo>>()

        val mappersWithoutMybatisConfig = mutableListOf<ClassInfo>()
        mapperInterfaces.forEach { (_,mapperInterface) ->
            // 记录需要配置基座数据源
            val mybatisConfig = parseMybatisConfig(splitModuleContext,mapperInterface)
            if(mybatisConfig==null){
                mappersWithoutMybatisConfig.add(mapperInterface)
            }else{
                CollectionUtil.addOrPutList(mybatisConfigs,mybatisConfig,mapperInterface)
            }

        }

        // 配置数据源
        configureMybatisConfigs(splitModuleContext,mybatisConfigs)

        // 未找到 Mapper 对应的 MyBatis 配置文件，则提示用户请查看复用数据源文档进行配置
        if(mappersWithoutMybatisConfig.isNotEmpty()){
            contentPanel.printMavenErrorLog("未检测到 以下 mapper 的 MyBatis 配置文件，请查看复用数据源文档手动配置： https://koupleless.io/docs/tutorials/module-development/reuse-base-datasource/ \n")
            val mapperClassInfoPaths = mappersWithoutMybatisConfig.map { it.getPath() }
            val tips = mapperClassInfoPaths.joinToString(separator = ",", prefix = "(", postfix = ")")
            contentPanel.printMavenLog(tips)
        }
    }

    private fun configureMybatisConfigs(splitModuleContext: SplitModuleContext, mybatisConfigs: Map<DBContext.MybatisConfig,List<ClassInfo>>) {
        val moduleDBContext = splitModuleContext.moduleContext.configContext.dbContext
        val srcBaseDBContext = splitModuleContext.srcBaseContext.configContext.dbContext

        val srcBaseMybatisConfigToReuse = mutableMapOf<DBContext.MybatisConfig,List<ClassInfo>>()
        mybatisConfigs.forEach { (config,mapperInterfaces)->
            if(moduleDBContext.mybatisConfigs.contains(config)){
                // TODO: 如果该数据库配置属于模块，那么不修改
            }else if(srcBaseDBContext.mybatisConfigs.contains(config)){
                // 如果该数据库配置属于基座，那么复用基座数据源
                srcBaseMybatisConfigToReuse[config] = mapperInterfaces
            }
        }

        reuseBaseMybatisBeans(splitModuleContext,srcBaseMybatisConfigToReuse)
    }

    /**
     * 根据 Mapper 的接口包名解析需要的 MybatisConfigurer
     */
    private fun parseMybatisConfig(splitModuleContext: SplitModuleContext, mapperInterface: ClassInfo):DBContext.MybatisConfig?{
        val interfaceType = mapperInterface.fullName
        val dbContextsToSearch = setOf(splitModuleContext.srcBaseContext.configContext.dbContext,splitModuleContext.moduleContext.configContext.dbContext)
        dbContextsToSearch.forEach { dbContext ->
            dbContext.mybatisConfigs.forEach {mybatisConfig ->
                if(mybatisConfig.matchBasePackage(interfaceType)){
                    return mybatisConfig
                }
            }
        }
        return null
    }

    private fun registerGeneratedClassInfo(splitModuleContext: SplitModuleContext,classInfo: ClassInfo){
        val moduleGeneratedClassInfoSet = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_GENERATED_MYBATIS_CLASS) as MutableSet<ClassInfo>? ?: mutableSetOf()
        moduleGeneratedClassInfoSet.add(classInfo)
        splitModuleContext.integrationStageContext.setConfig(SplitConstants.MODULE_GENERATED_MYBATIS_CLASS,moduleGeneratedClassInfoSet)
    }

    private fun reuseBaseMybatisBeans(splitModuleContext: SplitModuleContext, mybatisConfigs: Map<DBContext.MybatisConfig,List<ClassInfo>>) {
        val srcBaseDBContext = splitModuleContext.srcBaseContext.configContext.dbContext

        mybatisConfigs.forEach {(mybatisConfig,mapperInterfaces)->
            val modularName = mybatisConfig.beanInfo.getModularName()
            contentPanel.printMavenLog("数据库配置：模块将自动复用基座数据库相关配置 className= ${mybatisConfig.beanInfo.fullClassName} , beanName= ${mybatisConfig.beanInfo.beanName} , modularName = $modularName")
            // 1. 配置 mybatisConfig
            reuseBaseMybatisConfig(splitModuleContext,mybatisConfig,mapperInterfaces)

            // 2. 配置对应的 sqlSessionFactoryBean
            val sqlSessionFactoryInfo = srcBaseDBContext.getSqlSessionFactoryInfoByRef(mybatisConfig.sqlSessionFactoryRef!!,modularName)
            reuseSqlSessionFactoryBean(splitModuleContext,sqlSessionFactoryInfo)

            // 3. 配置对应的 platformTransactionManager
            val mybatisDataSource = srcBaseDBContext.getDataSourceByRef(sqlSessionFactoryInfo?.dataSourceBeanRef,modularName)
            val transactionManagerInfoList = srcBaseDBContext.getTransactionManagersByDataSourceBeanInfo(mybatisDataSource?.beanInfo,modularName)
            reuseTransactionManager(splitModuleContext,transactionManagerInfoList)

            // 4. 配置对应的 transactionTemplate
            val transactionManagerBeans = transactionManagerInfoList.map{it.beanInfo}
            val transactionTemplates =srcBaseDBContext.getTransactionTemplatesByTransactionManagerRef(transactionManagerBeans,modularName)
            reuseTransactionTemplate(splitModuleContext,transactionTemplates)

            // 5. 配置对应的 sqlSessionTemplate
            val sqlSessionTemplates = srcBaseDBContext.getSqlSessionTemplatesBySqlSessionFactoryRef(sqlSessionFactoryInfo?.beanInfo,modularName)
            reuseSqlSessionTemplates(splitModuleContext,sqlSessionTemplates)

            // 6. 清理对应的 dataSource
            clearDataSource(splitModuleContext,mybatisDataSource)
        }
    }

    private fun clearDataSource(splitModuleContext: SplitModuleContext, dataSource: DBContext.MybatisDataSource?) {
        if(null==dataSource){
            contentPanel.printMavenLog("未找到对应的 dataSource, 请自行清理")
            return
        }

        // 1. 如果在 xml 文件中，那么不用处理
        if(dataSource.beanInfo.definedByXML) return

        // 2. 如果文件在已选的模块文件里
        val srcPath = dataSource.beanInfo.filePath
        val moduleContext = splitModuleContext.moduleContext
        val existsInModule = moduleContext.containsFile(srcPath)
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        if(existsInModule){
            val tgtPath = moduleContext.getTgtPath(srcPath)
            tgtPath?.let {
                integrationContext.addModifier(tgtPath,DataSourceCleaner(tgtPath,dataSource))
            }
        }

        // 3. 如果文件在 integrateContext 里
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        val configFileName = dataSource.beanInfo.filePath.substringAfterLast(FileUtil.FILE_SEPARATOR)
        val tgtPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleDalDir,configFileName)
        val existsInIntegration = integrationContext.contains(tgtPath)
        if(existsInIntegration){
            integrationContext.addModifier(tgtPath,DataSourceCleaner(tgtPath,dataSource))
        }
    }

    private fun reuseTransactionTemplate(splitModuleContext: SplitModuleContext, transactionTemplates: List<DBContext.TransactionTemplate>) {
        if(transactionTemplates.isEmpty()){
            contentPanel.printMavenLog("未找到对应的 transactionTemplate, 请自行配置")
            return
        }

        transactionTemplates.forEach {transactionTemplate ->
            if(transactionTemplate.beanInfo.definedByXML){
                reuseTransactionTemplateInXML(splitModuleContext,transactionTemplate,contentPanel)
            }else{
                reuseTransactionTemplateInJava(splitModuleContext,transactionTemplate)
            }
        }
    }

    private fun reuseTransactionTemplateInXML(splitModuleContext: SplitModuleContext, transactionTemplate: DBContext.TransactionTemplate, contentPanel: ContentPanel) {
        val tgtPath = getDefaultMybatisExtraConfigPathInModule(splitModuleContext)
        val reusier = TransactionTemplateBeanXMLReuser(tgtPath,transactionTemplate,contentPanel)

        // 复制一份默认数据库配置至模块里
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.setJavaResource(tgtPath,SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE)
        integrationContext.addModifier(tgtPath,reusier)

        // 4. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val srcXMLPath = transactionTemplate.beanInfo.getAbsolutePathWhenDefined()!!
        val classInfo = ClassInfo(File(srcXMLPath)).apply {
            packageName = SplitConstants.DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME
            className = SplitConstants.MYBATIS_EXTRA_CONFIGURATION.substringBefore(".")
            fullName = "$packageName.$className"
            this.move(tgtPath) // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }

    private fun reuseTransactionTemplateInJava(splitModuleContext: SplitModuleContext, transactionTemplate: DBContext.TransactionTemplate) {
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        val srcPath = transactionTemplate.beanInfo.filePath
        val configFileName = srcPath.substringAfterLast(FileUtil.FILE_SEPARATOR)
        val tgtPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleDalDir,configFileName)

        val transactionTemplateBeanReuser = TransactionTemplateBeanReuser(tgtPath,transactionTemplate)

        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.addModifier(tgtPath,transactionTemplateBeanReuser)
        integrationContext.setJavaCopyPath(tgtPath,srcPath)

        // 4. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val classInfo = ClassInfo(File(srcPath)).apply {
            className = configFileName.substringBefore(".")
            packageName = transactionTemplate.beanInfo.fullClassName?.substringBefore(className)?.removeSuffix(".")?:FileParseUtil.parsePackageName(srcPath)!!
            fullName = "$packageName.$className"
            this.move(tgtPath)  // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }

    private fun reuseSqlSessionTemplates(splitModuleContext: SplitModuleContext, sqlSessionTemplates: List<DBContext.SqlSessionTemplate>) {
        if(sqlSessionTemplates.isEmpty()){
            contentPanel.printMavenLog("未找到对应的 sqlSessionTemplate, 请自行配置")
            return
        }
        sqlSessionTemplates.forEach {sqlSessionTemplate ->
            // TODO: 如果是以 xml 的形式配置的 sqlSessionTemplate
            if(sqlSessionTemplate.beanInfo.definedByXML){
                reuseSqlSessionTemplateInXML(splitModuleContext,sqlSessionTemplate)
            }else{
                reuseSqlSessionTemplateInJava(splitModuleContext,sqlSessionTemplate)
            }
        }
    }

    private fun reuseSqlSessionTemplateInXML(splitModuleContext: SplitModuleContext, sqlSessionTemplate: DBContext.SqlSessionTemplate) {
        val tgtPath = getDefaultMybatisExtraConfigPathInModule(splitModuleContext)
        val reusier = SqlSessionTemplateBeanXMLReuser(tgtPath,sqlSessionTemplate,contentPanel)

        // 1. 复制一份默认数据库配置至模块里
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.setJavaResource(tgtPath,SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE)
        integrationContext.addModifier(tgtPath,reusier)

        // 2. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中, 添加 sqlSessionTemplate 至模块上下文中
        val srcXMLPath = sqlSessionTemplate.beanInfo.getAbsolutePathWhenDefined()!!
        val classInfo = ClassInfo(File(srcXMLPath)).apply {
            packageName = SplitConstants.DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME
            className = SplitConstants.MYBATIS_EXTRA_CONFIGURATION.substringBefore(".")
            fullName = "$packageName.$className"
            this.move(tgtPath)
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }

    private fun reuseSqlSessionTemplateInJava(splitModuleContext: SplitModuleContext, sqlSessionTemplate: DBContext.SqlSessionTemplate) {
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        val srcPath = sqlSessionTemplate.beanInfo.filePath
        val configFileName = srcPath.substringAfterLast(FileUtil.FILE_SEPARATOR)
        val tgtPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleDalDir,configFileName)

        val sqlSessionTemplateBeanReuser = SqlSessionTemplateBeanReuser(tgtPath,sqlSessionTemplate)

        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.addModifier(tgtPath,sqlSessionTemplateBeanReuser)

        // 4. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val classInfo = ClassInfo(File(srcPath)).apply {
            className = configFileName.substringBefore(".")
            packageName = sqlSessionTemplate.beanInfo.fullClassName?.substringBefore(className)?.removeSuffix(".")?:FileParseUtil.parsePackageName(srcPath)!!
            fullName = "$packageName.$className"
            this.move(tgtPath)  // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext, classInfo)
    }

    private fun reuseTransactionManager(splitModuleContext: SplitModuleContext, transactionManagerInfoList: List<DBContext.PlatformTransactionManager>) {
        if(transactionManagerInfoList.isEmpty()){
            contentPanel.printMavenLog("未找到对应的 transactionManager, 请自行配置")
            return
        }
        transactionManagerInfoList.forEach {transactionManagerInfo->
            if(transactionManagerInfo.beanInfo.definedByXML){
                // 1. 复制 MyBatisConfig 节点到模块的 xml 里
                reuseBaseTransactionManagerInfoInXML(splitModuleContext,transactionManagerInfo)
            }else{ // 如果以 java 的形式配置的 MybatisConfigurer
                reuseBaseTransactionManagerInfoInJava(splitModuleContext,transactionManagerInfo)
            }

        }
    }

    private fun reuseBaseTransactionManagerInfoInXML(splitModuleContext: SplitModuleContext, transactionManagerInfo: DBContext.PlatformTransactionManager) {
        val tgtPath = getDefaultMybatisExtraConfigPathInModule(splitModuleContext)
        val reusier = TransactionManagerBeanXMLReuser(tgtPath,transactionManagerInfo,contentPanel)

        // 复制一份默认数据库配置至模块里
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.setJavaResource(tgtPath,SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE)
        integrationContext.addModifier(tgtPath,reusier)

        // 4. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val srcXMLPath = transactionManagerInfo.beanInfo.getAbsolutePathWhenDefined()!!
        val classInfo = ClassInfo(File(srcXMLPath)).apply {
            packageName = SplitConstants.DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME
            className = SplitConstants.MYBATIS_EXTRA_CONFIGURATION.substringBefore(".")
            fullName = "$packageName.$className"
            this.move(tgtPath) // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }

    private fun reuseBaseTransactionManagerInfoInJava(splitModuleContext: SplitModuleContext, transactionManagerInfo: DBContext.PlatformTransactionManager) {
        val srcPath = transactionManagerInfo.beanInfo.filePath
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        val configFileName = srcPath.substringAfterLast(FileUtil.FILE_SEPARATOR)
        val tgtPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleDalDir,configFileName)

        // 1. 创建复用器
        val transactionManagerBeanReuser = TransactionManagerBeanReuser(tgtPath,transactionManagerInfo)

        // 2. 配置到 integrationContext 中，并设置文件复制源
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.addModifier(tgtPath,transactionManagerBeanReuser)
        integrationContext.setJavaCopyPath(tgtPath,srcPath)

        // 2. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val classInfo = ClassInfo(File(srcPath)).apply {
            className = configFileName.substringBefore(".")
            packageName = transactionManagerInfo.beanInfo.fullClassName?.substringBefore(className)?.removeSuffix(".")?:FileParseUtil.parsePackageName(srcPath)!!
            fullName = "$packageName.$className"
            this.move(tgtPath) // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }


    private fun reuseBaseMybatisConfig(splitModuleContext: SplitModuleContext, mybatisConfig: DBContext.MybatisConfig, mapperInterfaces: List<ClassInfo>){
        if(mybatisConfig.beanInfo.definedByXML){
            reuseBaseMybatisConfigInXml(splitModuleContext,mybatisConfig,mapperInterfaces)
        }else{ // 如果以 java 的形式配置的 MybatisConfigurer
            reuseBaseMybatisConfigInJava(splitModuleContext,mybatisConfig,mapperInterfaces)
        }
    }

    private fun reuseBaseMybatisConfigInXml(splitModuleContext: SplitModuleContext, mybatisConfig: DBContext.MybatisConfig, mapperInterfaces: List<ClassInfo>) {
        val tgtPath = getDefaultMybatisConfigPathInModule(splitModuleContext)
        val mybatisConfigReusier = MybatisConfigXMLReusier(tgtPath,contentPanel,mybatisConfig)

        // 1. 复制一份至模块的 java文件里
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.setJavaResource(tgtPath,SplitConstants.MYBATIS_CONFIG_TEMPLATE_RESOURCE)

        // 2. 配置 basePackages
        val moduleContext = splitModuleContext.moduleContext
        val basePackages = mapperInterfaces.mapNotNull { moduleContext.getTgtPackageName(it.getPath()) }.toSet()
        mybatisConfigReusier.newBasePackages.addAll(basePackages)

        // 3. 配置到 integrationContext 中
        integrationContext.addModifier(tgtPath,mybatisConfigReusier)

        // 4. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val srcXMLPath = mybatisConfig.beanInfo.getAbsolutePathWhenDefined()!!
        val classInfo = ClassInfo(File(srcXMLPath)).apply {
            packageName = SplitConstants.DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME
            className = SplitConstants.MYBATIS_CONFIGURATION.substringBefore(".")
            fullName = "$packageName.$className"
            this.move(tgtPath) // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }

    private fun getDefaultMybatisConfigPathInModule(splitModuleContext: SplitModuleContext): String {
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        return StrUtil.join(FileUtil.FILE_SEPARATOR, moduleDalDir, SplitConstants.MYBATIS_CONFIGURATION)
    }

    private fun getDefaultMybatisExtraConfigPathInModule(splitModuleContext: SplitModuleContext): String {
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        return StrUtil.join(FileUtil.FILE_SEPARATOR, moduleDalDir, SplitConstants.MYBATIS_EXTRA_CONFIGURATION)
    }

    private fun getDefaultConfigLocationPathInModule(splitModuleContext: SplitModuleContext):String{
        val modulePath = splitModuleContext.moduleContext.getModulePath()
        val resourceDir = StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"app","service","src","main","resources")
        val mybatisLoc = SplitConstants.DEFAULT_MYBATIS_CONFIG_CLASSPATH_LOCATION.replace("/",FileUtil.FILE_SEPARATOR)
        return StrUtil.join(FileUtil.FILE_SEPARATOR, resourceDir, mybatisLoc)
    }

    /**
     * configLocation 属性如：<property name="configLocation" value="classpath:mybatisconfig/mybatis-config.xml"/>
     * 暂不支持通配符，仅在 sqlFactoryBean定义的 bundle查找
     */
    private fun getConfigLocationPathInBase(bundlePath:String,configLocation:String):String?{
        // 在 resources 中查找符合 configLocation 的文件
        val resourceRoot = FileParseUtil.parseResourceRoot(bundlePath)
        val relativePath = configLocation.substringAfter("classpath:")
        val locationPath = StrUtil.join(FileUtil.FILE_SEPARATOR,resourceRoot,relativePath.replace("/",FileUtil.FILE_SEPARATOR))
        if(FileUtil.exist(locationPath)){
            return locationPath
        }

        return null
    }

    private fun reuseSqlSessionFactoryBean(splitModuleContext: SplitModuleContext, sqlSessionFactoryInfo: DBContext.SqlSessionFactoryInfo?) {
        if(sqlSessionFactoryInfo==null){
            contentPanel.printMavenLog("未找到对应的 sqlSessionFactoryBean, 请自行配置")
            return
        }

        val srcBaseDBContext = splitModuleContext.srcBaseContext.configContext.dbContext
        val modularName = sqlSessionFactoryInfo.beanInfo.getModularName()
        if(sqlSessionFactoryInfo.dataSourceBeanRef==null || srcBaseDBContext.getDataSourceByRef(sqlSessionFactoryInfo.dataSourceBeanRef,modularName) == null){
            contentPanel.printMavenLog("未找到 sqlSessionFactory-${sqlSessionFactoryInfo.beanInfo.beanName} 对应的数据源，请自行配置")
            return
        }

        if(sqlSessionFactoryInfo.beanInfo.definedByXML){
            // 配置为 java 形式的 SqlSessionFactoryBean
            reuseSqlSessionFactoryBeanInXML(splitModuleContext,sqlSessionFactoryInfo)
        }else{
            reuseSqlSessionFactoryBeanInJava(splitModuleContext,sqlSessionFactoryInfo)
        }
    }

    private fun reuseSqlSessionFactoryBeanInXML(splitModuleContext: SplitModuleContext, sqlSessionFactoryInfo: DBContext.SqlSessionFactoryInfo) {
        val tgtPath = getDefaultMybatisExtraConfigPathInModule(splitModuleContext)
        val sqlSessionFactoryBeanReusier = SqlSessionFactoryBeanXMLReuser(tgtPath,sqlSessionFactoryInfo,contentPanel)

        // 设置数据源信息
        val srcBaseDBContext = splitModuleContext.srcBaseContext.configContext.dbContext
        val modularName = sqlSessionFactoryInfo.beanInfo.getModularName()
        val mybatisDataSource = srcBaseDBContext.getDataSourceByRef(sqlSessionFactoryInfo.dataSourceBeanRef!!,modularName)!!
        sqlSessionFactoryBeanReusier.setDataSource(mybatisDataSource)

        // 设置插件信息：从模块和基座中查找该bean，如果找不到，则默认在基座中
        val pluginBeanInfoList = mutableListOf<BeanInfo>()
        sqlSessionFactoryInfo.pluginBeanNames.forEach {
            val moduleBeanInfo = splitModuleContext.moduleContext.beanContext.getBeanByName(it)
            if(moduleBeanInfo!=null) {
                pluginBeanInfoList.add(moduleBeanInfo)
                return@forEach
            }
            val baseBeanInfo = splitModuleContext.srcBaseContext.beanContext.getBeanByName(it,modularName)
            if(baseBeanInfo!=null) {
                pluginBeanInfoList.add(baseBeanInfo)
                return@forEach
            }
            val appBeanInfo = splitModuleContext.appContext.beanContext.getBeanByName(it,modularName)
            if(appBeanInfo!=null) {
                pluginBeanInfoList.add(appBeanInfo)
            }else{
                // 找不到，则默认在基座中，设置 modularName 为 SqlSessionFactoryBean 的 modularName
                pluginBeanInfoList.add(BeanInfo(beanName = it,fullClassName = "org.apache.ibatis.plugin.Interceptor").apply {
                    this.registerModularName(modularName)
                })
            }
        }
        sqlSessionFactoryBeanReusier.addPlugins(pluginBeanInfoList)

        // 设置 config
        sqlSessionFactoryInfo.configLocation?.let {
            // 复制一份基座 config 至默认位置：SplitConstants.DEFAULT_MAPPER_CONFIG_CLASSPATH_LOCATION
            val integrationContext = splitModuleContext.integrationStageContext.integrateContext
            val configLocationTgtPath = getDefaultConfigLocationPathInModule(splitModuleContext)
            val bundlePath = FileParseUtil.parseBundlePath(sqlSessionFactoryInfo.beanInfo.filePath)
            val configLocationSrcPath =getConfigLocationPathInBase(bundlePath,it)
            if(configLocationSrcPath==null){
                contentPanel.printMavenLog("未找到对应的 configLocation 对应的文件，请自行配置")
            }else{
                integrationContext.setXMLPathToCopy(configLocationTgtPath,configLocationSrcPath)
            }

        }

        // 1. 复制一份默认数据库配置至模块里
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.setJavaResource(tgtPath,SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE)

        // 2. 配置到 integrationContext 中
        integrationContext.addModifier(tgtPath,sqlSessionFactoryBeanReusier)

        // 4. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val srcXMLPath = sqlSessionFactoryInfo.beanInfo.getAbsolutePathWhenDefined()!!
        val classInfo = ClassInfo(File(srcXMLPath)).apply {
            packageName = SplitConstants.DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME
            className = SplitConstants.MYBATIS_EXTRA_CONFIGURATION.substringBefore(".")
            fullName = "$packageName.$className"
            move(tgtPath)
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }

    private fun reuseSqlSessionFactoryBeanInJava(splitModuleContext: SplitModuleContext, sqlSessionFactoryInfo: DBContext.SqlSessionFactoryInfo) {
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        val srcPath = sqlSessionFactoryInfo.beanInfo.filePath
        val configFileName = srcPath.substringAfterLast(FileUtil.FILE_SEPARATOR)
        val tgtPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleDalDir,configFileName)

        val srcBaseDBContext = splitModuleContext.srcBaseContext.configContext.dbContext
        val modularName = sqlSessionFactoryInfo.beanInfo.getModularName()
        val mybatisDataSource = srcBaseDBContext.getDataSourceByRef(sqlSessionFactoryInfo.dataSourceBeanRef!!,modularName)!!

        // 1. 创建复用器
        val sqlSessionFactoryBeanReuser = SqlSessionFactoryBeanReuser(tgtPath,contentPanel)
        sqlSessionFactoryBeanReuser.setDataSource(mybatisDataSource)

        // 2. 配置到 integrationContext 中，并设置文件复制源
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.addModifier(tgtPath,sqlSessionFactoryBeanReuser)
        integrationContext.setJavaCopyPath(tgtPath,srcPath)

        // 3. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val classInfo = ClassInfo(File(srcPath)).apply {
            className = configFileName.substringBefore(".")
            packageName = sqlSessionFactoryInfo.beanInfo.fullClassName?.substringBefore(className)?.removeSuffix(".")?:FileParseUtil.parsePackageName(srcPath)!!
            fullName = "$packageName.$className"

            this.move(tgtPath)  // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }

    private fun reuseBaseMybatisConfigInJava(splitModuleContext: SplitModuleContext, mybatisConfig: DBContext.MybatisConfig, mapperInterfaces: List<ClassInfo>){
        val moduleContext = splitModuleContext.moduleContext

        val srcPath = mybatisConfig.beanInfo.filePath
        val configFileName = srcPath.substringAfterLast(FileUtil.FILE_SEPARATOR)
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        val tgtPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleDalDir,configFileName)

        // 1. 创建复用器
        val mybatisConfigReusier = MybatisConfigReusier(tgtPath,contentPanel)

        // 2. 配置 basePackages
        val basePackages = mapperInterfaces.mapNotNull { moduleContext.getTgtPackageName(it.getPath()) }.toSet()
        mybatisConfigReusier.newBasePackages.addAll(basePackages)

        // 3. 配置到 integrationContext 中，并设置文件复制源
        val integrationContext = splitModuleContext.integrationStageContext.integrateContext
        integrationContext.addModifier(tgtPath,mybatisConfigReusier)
        integrationContext.setJavaCopyPath(tgtPath,srcPath)

        // 4. 仅添加 classInfo 模块上下文中，TODO：添加 beanInfo 至模块上下文中
        val classInfo = ClassInfo(File(srcPath)).apply {
            className = configFileName.substringBefore(".")
            packageName = mybatisConfig.beanInfo.fullClassName?.substringBefore(className)?.removeSuffix(".")?:FileParseUtil.parsePackageName(srcPath)!!
            fullName = "$packageName.$className"
            this.move(tgtPath)  // 注意：这里需要配置 srcPath 和 tgtPath，以正确添加 pom
        }
        moduleContext.classInfoContext.addClassInfo(classInfo)
        registerGeneratedClassInfo(splitModuleContext,classInfo)
    }


    override fun getName(): String {
        return "分析添加mybatis配置"
    }

    // 原基座拆分时，自动复用数据源
    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return !splitModuleContext.toNewBase()
    }
}
