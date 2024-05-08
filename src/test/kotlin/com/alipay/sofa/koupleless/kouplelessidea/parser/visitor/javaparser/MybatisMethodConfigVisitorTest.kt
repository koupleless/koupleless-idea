package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.ParserConfiguration
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/28 18:42
 */
class MybatisMethodConfigVisitorTest {
    @Test
    fun testDoParse(){
        val url = this.javaClass.classLoader.getResource("parser/MybatisMethodConfigDemo.java")!!
        val file = File(url.toURI())
        val projContext = MockKUtil.mockProjectContext()
        ParseJavaService.parseOnly(listOf(file), ParserConfiguration(),listOf(MethodBeanVisitor,MybatisMethodConfigVisitor),projContext)

        // 验证扫描 datasource
        val dataSource = projContext.configContext.dbContext.getDataSourceByRef("vehDataSource")
        assertNotNull(dataSource)

        // 验证以 beanName 查找 SqlSessionFactoryBean
        val sqlSessionFactory = projContext.configContext.dbContext.getSqlSessionFactoryInfoByRef("mockSqlSessionFactory")
        assertNotNull(sqlSessionFactory)

        // 验证查找 SqlSessionFactoryBean 中的 datasourceBeanRef: 会以类型(DataSource)找到 bean (vehDataSource)
        val dataSourceBeanInSqlSessionFactoryBean = projContext.configContext.dbContext.getDataSourceByRef(sqlSessionFactory.dataSourceBeanRef)
        assertNotNull(dataSourceBeanInSqlSessionFactoryBean)
        assertEquals(dataSourceBeanInSqlSessionFactoryBean.beanInfo.beanName,"vehDataSource")

        // 验证以 dataSource 查找 PlatformTransactionManager
        val transactionManagers = projContext.configContext.dbContext.getTransactionManagersByDataSourceBeanInfo(dataSource.beanInfo)
        assertTrue(transactionManagers.isNotEmpty())

        // 验证以 transactionManager 查找 TransactionTemplate
        val transactionManagerBeans = transactionManagers.map { it.beanInfo }.toList()
        val transactionTemplates = projContext.configContext.dbContext.getTransactionTemplatesByTransactionManagerRef(transactionManagerBeans)
        assertTrue(transactionTemplates.isNotEmpty())

        // 验证以 sqlSessionFactory 查找 SqlSessionTemplate
        val sqlSessionFactoryBean = sqlSessionFactory.beanInfo
        val sqlSessionTemplates = projContext.configContext.dbContext.getSqlSessionTemplatesBySqlSessionFactoryRef(sqlSessionFactoryBean)
        assertTrue(sqlSessionTemplates.isNotEmpty())
    }
}
