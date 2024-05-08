package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.DEFAULT_REUSE_MYBATIS_FACTORY_XML_BEAN_METHOD
import com.github.javaparser.ast.CompilationUnit
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 17:50
 */
class SqlSessionFactoryBeanXMLReuserTest {
    private val cu = getCu()

    private fun getCu(): CompilationUnit {
        return MockKUtil.readCu(SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE)
    }

    @Test
    fun testDoParseWithAllConfigs(){
        // case1: 有 mapperLocations, 有 configLocation, 有 plugins
        val mockedSqlSessionFactoryBeanInfo = MockKUtil.spyBeanInfo("mockSqlSessionFactoryBean","com.mock.MockSqlSessionFactoryBean")
        val mockedDataSourceBeanRef = BeanRef(null,null,mockedSqlSessionFactoryBeanInfo)
        val sqlSessionFactoryInfo = mockk<DBContext.SqlSessionFactoryInfo>{
            every { beanInfo } returns mockedSqlSessionFactoryBeanInfo
            every { dataSourceBeanRef } returns mockedDataSourceBeanRef
            every { mapperLocationExists } returns true
            every { configLocation } returns "classpath:sqlmap/mock/mock-sqlmap.xml"
            every { pluginBeanNames } returns listOf("mockInterceptor")
        }

        val mockedDataSourceBean = MockKUtil.spyBeanInfo("mockDataSource","com.mock.MockDataSource")
        val dbSource = DBContext.MybatisDataSource(mockedDataSourceBean)
        val pluginBean = MockKUtil.spyBeanInfo("mockInterceptor","com.mock.MockInterceptor")

        val modifier = SqlSessionFactoryBeanXMLReuser("mockPath",sqlSessionFactoryInfo,MockKUtil.mockContentPanel())
        modifier.setDataSource(dbSource)
        modifier.addPlugins(listOf(pluginBean))

        val cuWithAllConfigs = cu.clone()
        modifier.doParse(Path.of("mockPath"),cuWithAllConfigs,null)

        // 验证：cu 有以 DEFAULT_REUSE_MYBATIS_FACTORY_XML_BEAN_METHOD 为方法名的方法
        val mockSqlSessionFactoryBeanMethod = JavaParserUtil.filterMethodByName(cuWithAllConfigs.getType(0),DEFAULT_REUSE_MYBATIS_FACTORY_XML_BEAN_METHOD).firstOrNull()
        assertNotNull(mockSqlSessionFactoryBeanMethod)

        // 验证：mockSqlSessionFactoryBean 方法的 beanName 为 mockSqlSessionFactoryBean
        val beanNameOfSqlSessionFactoryBean = ParseBeanService.parseBeanName(mockSqlSessionFactoryBeanMethod).firstOrNull()
        assertEquals("mockSqlSessionFactoryBean",beanNameOfSqlSessionFactoryBean)

        // 验证：mockSqlSessionFactoryBean 方法有 getBean 语句
        val getBeanStat = JavaParserUtil.filterMethodCallStatInMethodBody(mockSqlSessionFactoryBeanMethod,"getBaseBean").firstOrNull()
        assertNotNull(getBeanStat)

        // 验证：cu 有以 resolveMapperLocations 为方法名的方法
        val resolveMapperLocationsMethod = JavaParserUtil.filterMethodByName(cuWithAllConfigs.getType(0),"resolveMapperLocations").firstOrNull()
        assertNotNull(resolveMapperLocationsMethod)

        // 验证：mockSqlSessionFactoryBean 方法有 setConfigLocation 语句
        val setConfigLocationStat = JavaParserUtil.filterMethodCallStatInMethodBody(mockSqlSessionFactoryBeanMethod,"setConfigLocation").firstOrNull()
        assertNotNull(setConfigLocationStat)

        // 验证：cu 有以 mockInterceptor 为方法名的方法
        val mockInterceptorMethod = JavaParserUtil.filterMethodByName(cuWithAllConfigs.getType(0),"mockInterceptor").firstOrNull()
        assertNotNull(mockInterceptorMethod)

        // 验证：mockInterceptor 方法的 beanName 为 mockInterceptor
        val beanNameOfInterceptor = ParseBeanService.parseBeanName(mockInterceptorMethod).firstOrNull()
        assertEquals("mockInterceptor",beanNameOfInterceptor)

        // 验证：cu 的 imports 有 org.apache.ibatis.plugin.Interceptor
        assertTrue(cuWithAllConfigs.imports.any { it.nameAsString == "org.apache.ibatis.plugin.Interceptor" })

        // 验证：mockSqlSessionFactoryBean 方法有 setPlugins 语句
        val setPluginsStat = JavaParserUtil.filterMethodCallStatInMethodBody(mockSqlSessionFactoryBeanMethod,"setPlugins").firstOrNull()
        assertNotNull(setPluginsStat)

        // 验证：mockSqlSessionFactoryBean 方法的参数中有 mockInterceptor
        assertTrue(mockSqlSessionFactoryBeanMethod.parameters.any { it.nameAsString == "mockInterceptor" })
    }
}
