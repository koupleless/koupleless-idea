package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.analyse

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.IntegrationStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.modifier.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.XmlUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.BeanVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse.AnalyseToReuseMybatisConfigPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanSrcBaseMybatisConfigXmlPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:08
 */
class AnalyseToReuseMybatisConfigPluginTest {
    @Test
    fun testDoProcessWhenAllMybatisConfigInXml(){
        val splitModuleContext = mockSplitModuleContextWhenAllMybatisConfigInXml()
        AnalyseToReuseMybatisConfigPlugin(MockKUtil.mockContentPanel()).doProcess(splitModuleContext)

        // 验证 mybatisConfig
        val mybatisConfigTgtPath = getDefaultMybatisConfigPathInModule(splitModuleContext)
        val mybatisConfigJavaModifiers = splitModuleContext.integrationStageContext.integrateContext.getJavaFileModifier(mybatisConfigTgtPath)
        assertEquals(SplitConstants.MYBATIS_CONFIG_TEMPLATE_RESOURCE,mybatisConfigJavaModifiers.first().resourceToCopy)

        val mybatisConfigReusier = mybatisConfigJavaModifiers.first { it is MybatisConfigXMLReusier } as MybatisConfigXMLReusier
        assertTrue(mybatisConfigReusier.newBasePackages.contains("mock.module.dal"))

        val mybatisClassInfo = splitModuleContext.moduleContext.classInfoContext.getClassInfoByName(SplitConstants.DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME+"."+SplitConstants.MYBATIS_CONFIGURATION.substringBefore("."))!!
        assertEquals(mybatisConfigTgtPath,mybatisClassInfo.getPath())

        val generatedClassInfo = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_GENERATED_MYBATIS_CLASS) as MutableSet<ClassInfo>
        assertTrue(generatedClassInfo.contains(mybatisClassInfo))

        // 验证对应的 sqlSessionFactoryBean
        val mybatisExtraConfigTgtPath = getDefaultMybatisExtraConfigPathInModule(splitModuleContext)
        val mybatisExtraConfigJavaModifiers = splitModuleContext.integrationStageContext.integrateContext.getJavaFileModifier(mybatisExtraConfigTgtPath)
        assertEquals(SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE,mybatisExtraConfigJavaModifiers.first().resourceToCopy)

        val sqlSessionFactoryBeanXMLReuser = mybatisExtraConfigJavaModifiers.first { it is SqlSessionFactoryBeanXMLReuser } as SqlSessionFactoryBeanXMLReuser
        val matchedDataSource = sqlSessionFactoryBeanXMLReuser.getDataSource()!!
        assertEquals("mockDataSource",matchedDataSource.beanInfo.beanName)
        assertEquals(sqlSessionFactoryBeanXMLReuser.sqlSessionFactoryInfo.dataSourceBeanRef!!.beanNameToParse,matchedDataSource.beanInfo.beanName)
        assertEquals(1,sqlSessionFactoryBeanXMLReuser.getPlugins().size)

        val mybatisExtraClassInfo = splitModuleContext.moduleContext.classInfoContext.getClassInfoByName(SplitConstants.DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME+"."+SplitConstants.MYBATIS_EXTRA_CONFIGURATION.substringBefore("."))!!
        assertEquals(mybatisExtraConfigTgtPath,mybatisExtraClassInfo.getPath())

        val generatedExtraClassInfo = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_GENERATED_MYBATIS_CLASS) as MutableSet<ClassInfo>
        assertTrue(generatedExtraClassInfo.contains(mybatisExtraClassInfo))

        // 验证对应的 platformTransactionManager
        val transactionManagerBeanXMLReuser = mybatisExtraConfigJavaModifiers.first { it is TransactionManagerBeanXMLReuser } as TransactionManagerBeanXMLReuser
        assertEquals("mockDataSource",transactionManagerBeanXMLReuser.transactionManagerInfo.dataSourceBeanRef!!.beanNameToParse)

        // 验证对应的 transactionTemplate
        val transactionTemplateXMLReuser = mybatisExtraConfigJavaModifiers.first { it is TransactionTemplateBeanXMLReuser } as TransactionTemplateBeanXMLReuser
        assertEquals("transactionManager",transactionTemplateXMLReuser.transactionTemplate.transactionManagerBeanRef!!.beanNameToParse)

        // 验证对应的 sqlSessionTemplate
        val sqlSessionTemplateBeanXMLReuser = mybatisExtraConfigJavaModifiers.first { it is SqlSessionTemplateBeanXMLReuser } as SqlSessionTemplateBeanXMLReuser
        assertEquals("mockSqlSessionFactoryBean",sqlSessionTemplateBeanXMLReuser.sqlSessionTemplate.sqlSessionFactoryBeanRef!!.beanNameToParse)

        // 验证对应的 dataSource
        assertTrue(mybatisExtraConfigJavaModifiers.none { it is DataSourceCleaner })

    }

    @Test
    fun testDoProcessWhenAllMybatisConfigInJava(){
        val splitModuleContext = mockSplitModuleContextWhenAllMybatisConfigInJava()

        AnalyseToReuseMybatisConfigPlugin(MockKUtil.mockContentPanel()).doProcess(splitModuleContext)

        // 验证 mybatisConfig
        val mybatisConfigSrcPath = StrUtil.join(
            FileUtil.FILE_SEPARATOR,"app","dal","src","main","java","mock","module","dal","MybatisMethodConfigDemo.java")
        val mybatisConfigTgtPath = getMybatisPathInModule(splitModuleContext,mybatisConfigSrcPath)
        val mybatisConfigJavaModifiers = splitModuleContext.integrationStageContext.integrateContext.getJavaFileModifier(mybatisConfigTgtPath)
        assertEquals(mybatisConfigSrcPath,mybatisConfigJavaModifiers.first().absolutePathToCopy)

        val mybatisConfigReusier = mybatisConfigJavaModifiers.first { it is MybatisConfigReusier } as MybatisConfigReusier
        assertTrue(mybatisConfigReusier.newBasePackages.contains("com.xxx.mapper"))

        val mybatisClassInfo = splitModuleContext.moduleContext.classInfoContext.getClassInfoByName("com.mock.config.MybatisMethodConfigDemo")!!
        assertEquals(mybatisConfigTgtPath,mybatisClassInfo.getPath())
        assertEquals("com.mock.config.MybatisMethodConfigDemo",mybatisClassInfo.fullName)

        val generatedClassInfo = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_GENERATED_MYBATIS_CLASS) as MutableSet<ClassInfo>
        assertTrue(generatedClassInfo.contains(mybatisClassInfo))

        // 验证对应的 sqlSessionFactoryBean
        val sqlSessionFactoryBeanReuser = mybatisConfigJavaModifiers.first { it is SqlSessionFactoryBeanReuser } as SqlSessionFactoryBeanReuser
        val matchedDataSource = sqlSessionFactoryBeanReuser.getDataSource()!!
        assertEquals("vehDataSource",matchedDataSource.beanInfo.beanName)

        // 验证对应的 platformTransactionManager
        val transactionManagerBeanReuser = mybatisConfigJavaModifiers.first { it is TransactionManagerBeanReuser } as TransactionManagerBeanReuser
        assertEquals("vehDataSource",transactionManagerBeanReuser.transactionManagerInfo.dataSourceBeanRef!!.beanNameToParse)
        assertEquals("javax.sql.DataSource",transactionManagerBeanReuser.transactionManagerInfo.dataSourceBeanRef!!.beanTypeToParse)

        // 验证对应的 transactionTemplate
        val transactionTemplateReuser = mybatisConfigJavaModifiers.first { it is TransactionTemplateBeanReuser } as TransactionTemplateBeanReuser
        assertEquals("mockTransactionManager",transactionTemplateReuser.transactionTemplate.transactionManagerBeanRef!!.beanNameToParse)

        // 验证对应的 sqlSessionTemplate
        val sqlSessionTemplateBeanReuser = mybatisConfigJavaModifiers.first { it is SqlSessionTemplateBeanReuser } as SqlSessionTemplateBeanReuser
        assertEquals("sqlSessionFactory",sqlSessionTemplateBeanReuser.sqlSessionTemplate.sqlSessionFactoryBeanRef!!.beanNameToParse)
        assertEquals("com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean",sqlSessionTemplateBeanReuser.sqlSessionTemplate.sqlSessionFactoryBeanRef!!.beanTypeToParse)

        // 验证对应的 dataSource
        val tgtDataSourcePath = StrUtil.join(FileUtil.FILE_SEPARATOR,
            splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
            ,"MybatisMethodConfigDemo.java")
        val datasourceModifiers = splitModuleContext.integrationStageContext.integrateContext.getJavaFileModifier(tgtDataSourcePath)
        assertTrue(datasourceModifiers.any { it is DataSourceCleaner })
    }

    private fun mockSplitModuleContextWhenAllMybatisConfigInXml(): SplitModuleContext {
        // 数据源相关信息都在基座的xml中
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
            every { srcBaseContext } returns BaseContext(this)
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getTgtPackageName(any()) } returns "mock.module.dal"
            }
            every { integrationStageContext } returns IntegrationStageContext(this)
        }
        // 配置参数
        splitModuleContext.integrationStageContext.setConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG,StrUtil.join(
            FileUtil.FILE_SEPARATOR,"app","dal","src","main","java","mock","module","dal"))

        // 配置模块 mapper
        splitModuleContext.moduleContext.configContext.dbContext.registerMapperInterface(MockKUtil.spyClassInfo("com.mock.dao.ModuleMockDAO","moduleMockDAOClassPath"))

        // 读取mybatis_xml 配置至基座
        val xmlFiles = listOf(MockKUtil.readFile("parser/mybatis_config.xml"))
        XmlUtil.parseDefaultXml(xmlFiles, listOf(BeanVisitor),splitModuleContext.appContext)
        ScanSrcBaseMybatisConfigXmlPlugin.doProcess(splitModuleContext)
        return splitModuleContext
    }

    private fun mockSplitModuleContextWhenAllMybatisConfigInJava(): SplitModuleContext {
        // 数据源相关信息都在基座的java中
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
            every { srcBaseContext } returns BaseContext(this)
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getTgtPackageName(any()) } returns "com.xxx.mapper"
                every { getTgtPath(any()) } returns "mockDatasourceJavaTgtPath"
            }
            every { integrationStageContext } returns IntegrationStageContext(this)
        }

        // 配置参数
        splitModuleContext.integrationStageContext.setConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG,StrUtil.join(
            FileUtil.FILE_SEPARATOR,"app","dal","src","main","java","mock","module","dal"))

        // 配置模块 mapper
        splitModuleContext.moduleContext.configContext.dbContext.registerMapperInterface(MockKUtil.spyClassInfo("com.xxx.mapper.ModuleMockDAO","moduleMockDAOClassPath"))

        // 读取mybatis_java 配置至基座
        val myBatisDirPath = StrUtil.join(
            FileUtil.FILE_SEPARATOR,"app","dal","src","main","java","mock","module","dal")
        val cu = MockKUtil.readCu("parser/MybatisMethodConfigDemo.java")
        val visitors = listOf(ClassInfoVisitor, DefaultBeanVisitor, BeanExtraInfoVisitor, MethodBeanVisitor,BeanDependedOnVisitor,MybatisConfigVisitor,MybatisMethodConfigVisitor)
        visitors.forEach {
            it.parse(Path.of(StrUtil.join(FileUtil.FILE_SEPARATOR,myBatisDirPath,"MybatisMethodConfigDemo.java")),cu,splitModuleContext.srcBaseContext)
        }

        return splitModuleContext
    }

    private fun getDefaultMybatisExtraConfigPathInModule(splitModuleContext: SplitModuleContext): String {
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        return StrUtil.join(FileUtil.FILE_SEPARATOR, moduleDalDir, SplitConstants.MYBATIS_EXTRA_CONFIGURATION)
    }

    private fun getDefaultMybatisConfigPathInModule(splitModuleContext: SplitModuleContext): String {
        val moduleDalDir = splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        return StrUtil.join(FileUtil.FILE_SEPARATOR, moduleDalDir, SplitConstants.MYBATIS_CONFIGURATION)
    }

    private fun getMybatisPathInModule(splitModuleContext: SplitModuleContext, srcPath: String): String {
        val configFileName = srcPath.substringAfterLast(FileUtil.FILE_SEPARATOR)
        val moduleDalDir =
            splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_DIR_CONFIG) as String
        return StrUtil.join(FileUtil.FILE_SEPARATOR, moduleDalDir, configFileName)
    }
}
