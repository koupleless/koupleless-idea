package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.github.javaparser.ParserConfiguration
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Test
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/28 17:19
 */
class MybatisConfigVisitorTest {

    @Test
    fun testDoParse(){
        val url = this.javaClass.classLoader.getResource("parser/MybatisConfigurationDemo.java")!!
        val file = File(url.toURI())
        val projContext = MockKUtil.mockProjectContext()
        projContext.beanContext.addBeanInfo(BeanInfo("mybatisConfiguration",null))

        ParseJavaService.parseOnly(listOf(file), ParserConfiguration(),listOf(MybatisConfigVisitor),projContext)

        val dbContext = projContext.configContext.dbContext
        assertTrue(dbContext.mybatisConfigs.isNotEmpty())

        val mybatisConfig = dbContext.mybatisConfigs[0]
        assertEquals(mybatisConfig.basePackages.first(),"com.xxx.mapper")
        assertEquals(mybatisConfig.sqlSessionFactoryRef,"sqlSessionFactoryBeanForSingle")
    }
}
