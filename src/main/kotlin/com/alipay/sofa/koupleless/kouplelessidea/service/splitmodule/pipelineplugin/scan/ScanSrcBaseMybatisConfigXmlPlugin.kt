package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import org.apache.commons.configuration2.tree.ImmutableNode


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/12 17:18
 */
object ScanSrcBaseMybatisConfigXmlPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {

        scanMapperScanInXml(splitModuleContext)

        scanSqlSessionFactoryBeanInXml(splitModuleContext)

        scanDataSourceInXml(splitModuleContext)

        scanTransactionTemplate(splitModuleContext)

        scanTransactionManager(splitModuleContext)

        scanSqlSessionTemplate(splitModuleContext)
    }

    private fun scanSqlSessionTemplate(splitModuleContext: SplitModuleContext) {
        /**
         * 形如：
         * <bean id="vehownerprodSqlSession" class="org.mybatis.spring.SqlSessionTemplate">
         *         <constructor-arg index="0" ref="vehownerprodSqlSessionFactoryBean"/>
         * </bean>
         */

        val xmlContext = splitModuleContext.appContext.xmlContext
        val sqlSessionTemplates = xmlContext.getBeanNodes().filter { SplitConstants.SQL_SESSION_TEMPLATE.contains(it.fullClassName)}
        if(sqlSessionTemplates.isEmpty()) return

        val dbContext = splitModuleContext.srcBaseContext.configContext.dbContext
        sqlSessionTemplates.forEach {beanXmlNode ->
            val node = beanXmlNode.xmlNode.node
            node?:return@forEach

            // sqlSessionFactoryBeanRef:
            val argNode = node.children.firstOrNull { it.nodeName.equals("constructor-arg") && it.attributes["index"]=="0" }
            argNode?:return@forEach

            val sqlSessionFactoryRef = argNode.attributes["ref"]
            sqlSessionFactoryRef?:return@forEach

            val sqlSessionFactoryBeanRef = BeanRef("sqlSessionFactory",null,beanXmlNode.beanInfo,sqlSessionFactoryRef as String,beanTypeToParse=null,BeanRef.AutowiredMode.BY_NAME)
            val sqlSessionTemplate = DBContext.SqlSessionTemplate(beanXmlNode.beanInfo, sqlSessionFactoryBeanRef)
            dbContext.registerSqlSessionTemplate(sqlSessionTemplate)
        }
    }

    private fun scanTransactionManager(splitModuleContext: SplitModuleContext) {
        /**
         * 形如：
         * <bean id="transactionManager"
         *           class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
         *         <property name="dataSource" ref="dynamicDataSource"/>
         *     </bean>
         *
         * <bean id="transactionManager"
         * 		  class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
         * 		<property name="dataSource">
         * 			<ref bean="dataSource" />
         * 		</property>
         * 	</bean>
         */

        val xmlContext = splitModuleContext.appContext.xmlContext
        val transactionManagers = xmlContext.getBeanNodes().filter { SplitConstants.PLATFORM_TRANSACTION_MANAGER.contains(it.fullClassName)}
        if(transactionManagers.isEmpty()) return

        val dbContext = splitModuleContext.srcBaseContext.configContext.dbContext
        transactionManagers.forEach {beanXmlNode ->
            val node = beanXmlNode.xmlNode.node
            node?:return@forEach

            // dataSourceRef
            val argNode = node.children.firstOrNull { it.nodeName.equals("property") && it.attributes["name"]=="dataSource" }
            argNode?:return@forEach

            val dataSourceRefFromAttr = argNode.attributes["ref"]
            val dataSourceRefFromChild = argNode.children.firstOrNull{it.nodeName=="ref"}?.attributes?.get("bean")
            val dataSourceRef = dataSourceRefFromAttr?:dataSourceRefFromChild
            dataSourceRef?:return@forEach

            val dataSourceBeanRef = BeanRef("dataSource",null,beanXmlNode.beanInfo,dataSourceRef as String,beanTypeToParse=null,BeanRef.AutowiredMode.BY_NAME)
            dataSourceBeanRef.definedInXML = true
            val transactionManager = DBContext.PlatformTransactionManager(beanXmlNode.beanInfo, dataSourceBeanRef)
            dbContext.registerPlatformTransactionManager(transactionManager)
        }
    }

    private fun scanTransactionTemplate(splitModuleContext: SplitModuleContext) {
        /**
         * 形如：
         * <bean id="vehTransactionTemplate"
         *           class="org.springframework.transaction.support.TransactionTemplate">
         *         <property name="transactionManager" ref="transactionManager"/>
         *         <property name="propagationBehaviorName" value="PROPAGATION_REQUIRES_NEW"/>
         *     </bean>
         *
         * <bean id="transactionTemplate"
         * 		  class="org.springframework.transaction.support.TransactionTemplate">
         * 		<property name="transactionManager">
         * 			<ref bean="transactionManager" />
         * 		</property>
         * 	</bean>
         */

        val xmlContext = splitModuleContext.appContext.xmlContext
        val transactionTemplates = xmlContext.getBeanNodes().filter { SplitConstants.TRANSACTION_TEMPLATE.contains(it.fullClassName)}
        if(transactionTemplates.isEmpty()) return

        val dbContext = splitModuleContext.srcBaseContext.configContext.dbContext
        transactionTemplates.forEach {beanXmlNode ->
            val node = beanXmlNode.xmlNode.node
            node?:return@forEach

            // transactionManagerRef
            val argNode = node.children.firstOrNull { it.nodeName.equals("property") && it.attributes["name"]=="transactionManager" }
            argNode?:return@forEach

            val transactionManagerRefFromAttr = argNode.attributes["ref"]
            val transactionManagerRefFromChild = argNode.children.firstOrNull{it.nodeName=="ref"}?.attributes?.get("bean")
            val transactionManagerRef = transactionManagerRefFromAttr?:transactionManagerRefFromChild
            transactionManagerRef?:return@forEach

            val transactionManagerBeanRef = BeanRef("transactionManager",null,beanXmlNode.beanInfo,transactionManagerRef as String,beanTypeToParse=null,BeanRef.AutowiredMode.BY_NAME)
            val transactionTemplate = DBContext.TransactionTemplate(beanXmlNode.beanInfo, transactionManagerBeanRef)

            dbContext.registerTransactionTemplate(transactionTemplate)
        }
    }

    private fun scanDataSourceInXml(splitModuleContext: SplitModuleContext) {
        /**
         * 形如：
         * 自定义数据源
         * <bean id="dynamicDataSource" class="com.alipay.vehownerprod.common.dal.etc.config.mybatis.DynamicDataSource">
         *     ...
         *     </bean>
         * 数据源
         * <bean id="vehDataSource" class="com.alipay.zdal.client.jdbc.ZDataSource" init-method="init" destroy-method="close">
         *         ...
         *     </bean>
         */

        val xmlContext = splitModuleContext.appContext.xmlContext
        val customDataSourceClasses = splitModuleContext.srcBaseContext.analyseConfig.getCustomDataSourceClasses()
        val dataSourceClassNames = SplitConstants.DATA_SOURCE + customDataSourceClasses
        val dataSources = xmlContext.getBeanNodes().filter { dataSourceClassNames.contains(it.fullClassName)}
        if(dataSources.isEmpty()) return

        val dbContext = splitModuleContext.srcBaseContext.configContext.dbContext
        dataSources.forEach {beanXmlNode ->
            val node = beanXmlNode.xmlNode.node
            node?:return@forEach

            val dataSource = DBContext.MybatisDataSource(beanXmlNode.beanInfo)
            dbContext.registerDataSource(dataSource)
        }
    }

    private fun scanSqlSessionFactoryBeanInXml(splitModuleContext: SplitModuleContext) {
        /**
         * 形如：
         *     <bean id="sqlSessionFactoryBean" class="org.mybatis.spring.SqlSessionFactoryBean">
         *         <property name="dataSource" ref="dynamicDataSource"/>
         *         <property name="configLocation" value="classpath:mybatisconfig/mybatis-config.xml"/>
         *         <property name="mapperLocations">
         *             <array>
         *                 <value>classpath*:/sqlmap/etc/sku/\*.xml</value>
         *                 <value>classpath*:/sqlmap/etc/order/\*.xml</value>
         *             </array>
         *         </property>
         *         <property name="plugins">
         *             <array>
         *                 <bean id="pageInterceptor" class="com.github.pagehelper.PageInterceptor">
         *                     <property name="properties">
         *                         <value>
         *                             helperDialect=mysql
         *                             rowBoundsWithCount=true
         *                             reasonable=false
         *                             supportMethodsArguments=true
         *                             pageSizeZero=true
         *                             params=pageNum=pageNum;pageSize=pageSize;orderBy=orderBy
         *                             offsetAsPageNum=true
         *                         </value>
         *                     </property>
         *                 </bean>
         *                 <ref bean="asd"/>
         *             </array>
         *         </property>
         *     </bean>
         *
         * 记录 dataSource,configLocation, plugins的id；plugins 以方法方式定义bean
         */

        val xmlContext = splitModuleContext.appContext.xmlContext
        val sqlSessionFactories = xmlContext.getBeanNodes().filter { SplitConstants.SQL_SESSION_FACTORY_BEAN.contains(it.fullClassName)}
        if(sqlSessionFactories.isEmpty()) return

        val dbContext = splitModuleContext.srcBaseContext.configContext.dbContext
        sqlSessionFactories.forEach { beanXmlNode ->
            val node = beanXmlNode.xmlNode.node
            node?:return@forEach

            // dataSourceId:
            val dataSourceNode = node.children.firstOrNull { it.nodeName.equals("property")&& "dataSource" == it.attributes["name"] }
            dataSourceNode?:return@forEach
            val dataSourceIdFromAttr = dataSourceNode.attributes["ref"]
            val dataSourceIdFromChild = dataSourceNode.children.firstOrNull{it.nodeName=="ref"}?.attributes?.get("bean")
            val dataSourceId = dataSourceIdFromAttr?:dataSourceIdFromChild
            dataSourceId?:return@forEach

            // configLocation:
            val configLocationNode = node.children.firstOrNull { it.nodeName.equals("property")&& "configLocation" == it.attributes["name"] }
            val configLocation = configLocationNode?.attributes?.getValue("value") as String?

            // mapperLocations:
            val existMapperLocation = node.children.any { it.nodeName.equals("property")&& "mapperLocations" == it.attributes["name"] }

            // plugins:
            val pluginNode = node.children.firstOrNull { it.nodeName.equals("property")&& "plugins" == it.attributes["name"]}
            val pluginBeanNames = getPluginBeanNames(pluginNode)

            val dataSourceBeanRef = BeanRef("dataSource",null,beanXmlNode.beanInfo,dataSourceId as String,beanTypeToParse=null,BeanRef.AutowiredMode.BY_NAME)
            dataSourceBeanRef.definedInXML = true
            val sqlSessionFactory = DBContext.SqlSessionFactoryInfo(beanXmlNode.beanInfo, dataSourceBeanRef,existMapperLocation, configLocation, pluginBeanNames)
            dbContext.registerSqlSessionFactory(sqlSessionFactory)
        }

    }

    private fun getPluginBeanNames(pluginNode:ImmutableNode?):List<String>{
        /**
         * 形如：
         * <property name="plugins">
         *     <array>
         *         <bean id="pageInterceptor" class="com.github.pagehelper.PageInterceptor">
         *             <property name="properties">
         *                 <value>
         *                     helperDialect=mysql
         *                     rowBoundsWithCount=true
         *                     reasonable=false
         *                     supportMethodsArguments=true
         *                     pageSizeZero=true
         *                     params=pageNum=pageNum;pageSize=pageSize;orderBy=orderBy
         *                     offsetAsPageNum=true
         *                 </value>
         *             </property>
         *         </bean>
         *         <ref bean="asd"/>
         *     </array>
         * </property>
         */
        pluginNode?:return emptyList()

        val arrayNode = pluginNode.getChildren("array").firstOrNull()
        arrayNode?:return emptyList()

        val pluginBeanNameFromRefElement = arrayNode.filter {
            SplitConstants.BEAN_REF_XML_ELEMENTS.contains(it.nodeName)
        }.mapNotNull {
            it.attributes[SplitConstants.BEAN_REF_XML_ATTRIBUTE_IN_ELEMENT] as String?
        }.toList()

        val pluginBeanNameFromBeanElement = arrayNode.getChildren("bean").mapNotNull {
            it.attributes["id"]  as String?
        }.toList()

        return pluginBeanNameFromRefElement + pluginBeanNameFromBeanElement
    }


    override fun getName(): String {
        return "扫描基座在 xml 中的 mybatis 配置"
    }

    private fun scanMapperScanInXml(splitModuleContext: SplitModuleContext){
        // 配置 mybatis信息 至上下文中
        val xmlContext = splitModuleContext.appContext.xmlContext
        val mapperScanConfigs = xmlContext.getBeanNodes().filter { it.fullClassName=="org.mybatis.spring.mapper.MapperScannerConfigurer" }
        if(mapperScanConfigs.isEmpty()) return

        /**
         * 形如：
         * <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
         *         <property name="basePackage" value="
         *         com.alipay.vehownerprod.common.dal.etc.mapper.order,
         *         com.alipay.vehownerprod.common.dal.etc.mapper.sku
         *         "/>
         *         <property name="sqlSessionFactoryBeanName" value="sqlSessionFactoryBean"/>
         * </bean>
         */
        val dbContext = splitModuleContext.srcBaseContext.configContext.dbContext
        mapperScanConfigs.forEach {beanXmlNode ->
            val node = beanXmlNode.xmlNode.node

            // basePackage:
            val basePackageStr = node!!.children.filter { child -> child.attributes["name"] == "basePackage" }
                .firstNotNullOfOrNull { child -> child.attributes["value"] }
            val basePackages = basePackageStr?.let{
                (it as String).replace(" ","").split(",")
            }?:emptyList()

            // sqlSessionFactoryBeanName:
            val sqlSessionFactoryBeanName  =
                node.children.filter { child -> child.attributes["name"] == "sqlSessionFactoryBeanName" }
                    .firstNotNullOfOrNull { child -> child.attributes["value"] }

            val config = DBContext.MybatisConfig(beanXmlNode.beanInfo,basePackages.toSet(),sqlSessionFactoryBeanName as String?)
            dbContext.registerMybatisConfig(config)
        }
    }
}
