package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ParserConfiguration
import org.junit.Test
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import java.io.File
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/8 11:32
 */
class MethodBeanVisitorTest {
    @Test
    fun testDoParse() {
        val url = this.javaClass.classLoader.getResource("parser/MybatisConfigurationDemo.java")!!
        val file = File(url.toURI())
        val projContext = MockKUtil.mockProjectContext()
        ParseJavaService.parseOnly(listOf(file), ParserConfiguration(),listOf(MethodBeanVisitor),projContext)
        assertTrue(projContext.beanContext.containsBeanName("sqlSessionFactoryBeanForSingle"))

        val sqlSessionFactoryBeanForSingle = projContext.beanContext.getBeanByName("sqlSessionFactoryBeanForSingle")
        assertTrue(sqlSessionFactoryBeanForSingle!= null)
        assertTrue(sqlSessionFactoryBeanForSingle.beanName == "sqlSessionFactoryBeanForSingle")
        assertTrue(sqlSessionFactoryBeanForSingle.interfaceTypes.contains("org.mybatis.spring.SqlSessionFactoryBean"))
        assertTrue(sqlSessionFactoryBeanForSingle.getAttribute(SplitConstants.METHOD_BEAN)=="sqlSessionFactoryBeanForSingle(javax.sql.DataSource)")
        assertTrue(sqlSessionFactoryBeanForSingle.beanDependOn.containsKey("dataSource"))
    }
}
