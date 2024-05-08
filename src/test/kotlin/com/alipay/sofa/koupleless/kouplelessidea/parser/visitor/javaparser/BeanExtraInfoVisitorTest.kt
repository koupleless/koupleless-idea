package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.ParserConfiguration
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/3 15:13
 */
class BeanExtraInfoVisitorTest {

    @Test
    fun testDoParseComponent(){
        val url = this.javaClass.classLoader.getResource("parser/ClassInfoDemo.java")!!
        val file = File(url.toURI())
        val projContext = MockKUtil.mockProjectContext()
        val beanInfo = MockKUtil.spyBeanInfo("classInfoDemo","com.demo.ClassInfoDemo")
        projContext.beanContext.addBeanInfo(beanInfo)
        ParseJavaService.parseOnly(listOf(file), ParserConfiguration(),listOf(BeanExtraInfoVisitor),projContext)

        // 验证属性
        assertTrue(beanInfo.filePath.isNotEmpty())
        assertEquals(beanInfo.interfaceTypes.count(),2)

        // 验证是否正确加入到 beanContext 中
        assertEquals(beanInfo.parentContext,projContext.beanContext)
        assertTrue(projContext.beanContext.containsBeanType("com.mock.MockProduct"))
        assertTrue(projContext.beanContext.containsBeanType("com.mock.MockInfo"))
    }
}
