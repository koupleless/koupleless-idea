package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.XmlUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.BeanVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanSrcBaseMybatisConfigXmlPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:07
 */
class ScanSrcBaseMybatisConfigXmlPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        ScanSrcBaseMybatisConfigXmlPlugin.doProcess(splitModuleContext)

        // 验证mybatisConfig
        val dbContext = splitModuleContext.srcBaseContext.configContext.dbContext
        val mybatisConfig = dbContext.mybatisConfigs.first()
        assertTrue(mybatisConfig.basePackages.contains("com.mock.dao"))
        assertEquals("mockSqlSessionFactoryBean",mybatisConfig.sqlSessionFactoryRef)

        // 验证sqlSessionFactory
        val sqlSessionFactoryBean = dbContext.sqlSessionFactories["mockSqlSessionFactoryBean"]!!.first()
        assertEquals("mockDataSource",sqlSessionFactoryBean.dataSourceBeanRef!!.beanNameToParse)
        assertEquals("classpath:mybatisconfig/mybatis-config.xml",sqlSessionFactoryBean.configLocation)
        assertTrue(sqlSessionFactoryBean.pluginBeanNames.contains("pageInterceptor"))
        assertNull(sqlSessionFactoryBean.beanInfo.getModularName())

        // 验证dataSource
        assertNotNull(dbContext.getDataSourceByName("mockDataSource"))

        // 验证transactionTemplate
        val transactionTemplate = dbContext.transactionTemplates.first()
        assertEquals("transactionManager",transactionTemplate.transactionManagerBeanRef!!.beanNameToParse)

        // 验证transactionManager
        val transactionManager = dbContext.platformTransactionManagers["transactionManager"]!!.first()
        assertEquals("mockDataSource",transactionManager.dataSourceBeanRef!!.beanNameToParse)

        // 验证sqlSession
        val sqlSessionTemplate = dbContext.sqlSessionTemplates.first()
        assertEquals("mockSqlSessionFactoryBean",sqlSessionTemplate.sqlSessionFactoryBeanRef!!.beanNameToParse)
        assertEquals("mockSqlSession",sqlSessionTemplate.beanInfo.beanName)
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every {appContext} returns MockKUtil.spyApplicationContext(this)
            every { srcBaseContext } returns BaseContext(this)
        }

        val xmlFiles = listOf(MockKUtil.readFile("parser/mybatis_config.xml"))
        XmlUtil.parseDefaultXml(xmlFiles, listOf(BeanVisitor),splitModuleContext.appContext)

        return splitModuleContext
    }
}
