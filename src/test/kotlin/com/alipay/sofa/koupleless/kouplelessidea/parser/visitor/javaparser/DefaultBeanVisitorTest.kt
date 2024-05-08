package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.github.javaparser.ParserConfiguration
import org.junit.Test
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/28 17:09
 */
class DefaultBeanVisitorTest {

    @Test
    fun testDoParseComponent(){
        val url = this.javaClass.classLoader.getResource("parser/ClassInfoDemo.java")!!
        val file = File(url.toURI())
        val projContext = MockKUtil.mockProjectContext()
        ParseJavaService.parseOnly(listOf(file), ParserConfiguration(),listOf(DefaultBeanVisitor),projContext)

        val beanInfo = projContext.beanContext.getBeanByName("classInfoDemo")
        assertNotNull(beanInfo)
        assertEquals(beanInfo.fullClassName,"com.demo.ClassInfoDemo")
        assertEquals(beanInfo.parentContext,projContext.beanContext)
        assertTrue(beanInfo.filePath.isNotEmpty())
    }
}
